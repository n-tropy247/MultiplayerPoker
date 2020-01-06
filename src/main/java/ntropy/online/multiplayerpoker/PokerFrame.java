/*
 * Copyright (C) 2019 Ryan Castelli
 * Copyright (C) 2019 Samantha Cole
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ntropy.online.multiplayerpoker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

/**
 * Multiplayer 5-card draw Poker game.
 *
 * @author NTropy
 * @author Sam Cole
 * @version 10.22.2019
 * @since 4.7.2019
 */
public final class PokerFrame {

    /**
     * Input from server.
     */
    private BufferedReader svrIn;

    /**
     * Client's number in game.
     */
    private int clientNum;

    /**
     * Output to server.
     */
    private PrintWriter svrOut;

    /**
     * Window dimensions. TODO make window scalable by user
     */
    private final int frameWidth = 1480, frameHeight = 900;

    /**
     * Custom interactive panel to display card images.
     */
    private final CardPanel cardPanel;

    /**
     * Main window container for application.
     */
    private final JFrame mainFrame;

    /**
     * Array of cards being displayed.
     */
    private Card[] cards;

    /**
     * Create application components on object creation.
     */
    private PokerFrame() {
        final int defaultPort = 22337;
        final String defaultHost = "127.0.0.1";
        final Socket socket;

        try {
            socket = new Socket(defaultHost, defaultPort);
            try {
                svrIn = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                svrOut = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Issue opening i/o stream from server: "
                        + e);
            }
        } catch (UnknownHostException e) {
            System.err.println("Host not found: " + e);
        } catch (IOException ie) {
            System.err.println("Socket cannot read from server: " + ie);
        }

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (InstantiationException | ClassNotFoundException
                | IllegalAccessException
                | UnsupportedLookAndFeelException exe) {
            System.err.println("Nimbus unavailable: " + exe);
        }

        cardPanel = new CardPanel();
        cardPanel.setOpaque(false);
        cardPanel.addMouseListener(new MouseHandler());
        cardPanel.addMouseMotionListener(new MouseHandler());

        JButton switchBtn = new JButton("Switch");
        switchBtn.addActionListener(new ButtonHandler());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(switchBtn);
        buttonPanel.setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(cardPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.setOpaque(false);
        centerPanel.setPreferredSize(cardPanel.getPreferredSize());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(centerPanel);
        mainPanel.setBackground(Color.green);

        //DEBUG Borders to help frame window
        cardPanel.setBorder(new TitledBorder("Drawn Cards"));
        buttonPanel.setBorder(new TitledBorder("Button panel"));
        centerPanel.setBorder(new TitledBorder("Center panel"));
        mainPanel.setBorder(new TitledBorder("Entire window"));

        mainFrame = new JFrame("Poker Client");
        mainFrame.add(mainPanel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.setPreferredSize(new Dimension(frameWidth, frameHeight));
        mainFrame.setLocationByPlatform(true);
        mainFrame.setResizable(false);
        mainFrame.pack();
        try {
            clientNum = Integer.parseInt(svrIn.readLine());
        } catch (IOException e) {
            System.err.println("Couldn't acquire client number: " + e);
        }
    }

    /**
     * Handle adjustments to Card array following server input.
     *
     * @param newCardList array of new cards from server
     */
    private void adjustCardArr(final ArrayList<String> newCardList) {
        int cardH, cardW, cardX, cardY;
        for (int j = 0; j < cards.length; j++) {
            if (cards[j].toSwitch()) {
                cardH = cards[j].getH();
                cardW = cards[j].getW();
                cardX = cards[j].getX();
                cardY = cards[j].getY();
                cards[j] = new Card(
                        cardX, cardY, cardW, cardH, newCardList.remove(0));
            }
        }
    }

    /**
     * Create application thread.
     *
     * @param args command-line arguments; unused here
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            PokerFrame pokerFrame = new PokerFrame();
            pokerFrame.mainFrame.setVisible(true);
        });
    }

    /**
     * Handles drawing of cards.
     */
    private final class CardPanel extends JPanel {

        /**
         * Card panel dimensions. //TODO find a way to intuitively scale this
         */
        private final int widthAdjust = 200, heightAdjust = 150,
                cardPanelWidth = frameWidth - widthAdjust,
                cardPanelHeight = frameHeight - heightAdjust;

        /**
         * Card image containers.
         */
        private BufferedImage cardFront, cardBack;

        /**
         * Create images from file, get dimensions, populate card array.
         */
        private CardPanel() {
            try {
                cardFront = ImageIO.read(new File("images\\cardFront.png"));
                cardBack = ImageIO.read(new File("images\\cardBack.png"));
            } catch (IOException ie) {
                System.err.println("Image sources couldn't be accessed: " + ie);
                System.exit(0);
            }

            final int cardHeight = cardBack.getHeight(),
                    cardWidth = cardBack.getWidth(),
                    cardSpacing = cardPanelWidth / 20 + cardWidth,
                    cardNum = 5, leftMargin = cardPanelWidth - cardNum
                    * cardWidth - cardNum * (cardSpacing - cardWidth);

            cards = new Card[cardNum];
            //TODO grab card names from server on initialization
            for (int j = 0; j < cardNum; j++) {
                if (j == 0) {
                    cards[0] = new Card(leftMargin,
                            cardPanelHeight / 2 - cardHeight / 2, cardWidth,
                            cardHeight, "TEMP");
                } else {
                    cards[j] = new Card(leftMargin + cardSpacing * j,
                            cardPanelHeight / 2 - cardHeight / 2, cardWidth,
                            cardHeight, "TEMP");
                }
            }
        }

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            doDrawing(g);
        }

        /**
         * Draw in components.
         *
         * @param g Graphics of JPanel
         */
        private void doDrawing(final Graphics g) {
            final int border = 2;
            for (Card curCard : cards) {
                g.drawImage(cardFront, curCard.getX(), curCard.getY(), null);
                if (curCard.isActive()) {
                    g.setColor(Color.black);
                    g.drawRect(curCard.getX() - border, curCard.getY() - border,
                            curCard.getW() + 2 * border,
                            curCard.getH() + 2 * border);
                }
                if (curCard.isFlipped()) {
                    g.drawImage(cardBack, curCard.getX(), curCard.getY(), null);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(frameWidth - widthAdjust,
                    frameHeight - heightAdjust);
        }
    }

    /**
     * Handles all button events for the frame.
     */
    private final class ButtonHandler implements ActionListener {

        /**
         * Possible commands.
         */
        private final String switchCmd = "Switch";

        /**
         * Number of cards switched out.
         */
        private int numCardsSwitched;

        @Override
        public void actionPerformed(final ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(switchCmd)) {
                System.out.println("Number " + clientNum + " queried "
                        + PokerServer.queryTurn(clientNum));
                ArrayList<String> newCardList = new ArrayList<>();
                numCardsSwitched = 0;
                for (Card curCard : cards) {
                    if (curCard.isFlipped()) {
                        curCard.setSwitch(true);
                        numCardsSwitched++;
                    }
                }
                svrOut.println(numCardsSwitched);
                for (Card curCard : cards) {
                    if (curCard.toSwitch()) {
                        svrOut.println(curCard.getType());
                    }
                }
                String svrInput;
                while (newCardList.size() < numCardsSwitched) {
                    try {
                        svrInput = svrIn.readLine();
                        newCardList.add(svrInput);
                    } catch (IOException ie) {
                        System.err.println("Couldn't read from server: " + ie);
                        //DEBUG
                        System.exit(0);
                    }
                }
                adjustCardArr(newCardList);
                cardPanel.repaint();
                PokerServer.nextTurn();
            }
        }
    }

    /**
     * Handles all mouse events for the frame.
     */
    private final class MouseHandler implements MouseListener,
            MouseMotionListener {

        /**
         * Mouse positions.
         */
        private int mouseX, mouseY;

        @Override
        public void mousePressed(final MouseEvent e) {
            for (Card curCard : cards) {
                if (curCard.isActive()) {
                    curCard.setFill(!curCard.isFlipped());
                }
            }
            cardPanel.repaint();
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
        }

        @Override
        public void mouseExited(final MouseEvent e) {
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            for (Card curCard : cards) {
                curCard.setActive(mouseX >= curCard.getX()
                        && mouseX <= curCard.getX() + curCard.getW()
                        && mouseY >= curCard.getY()
                        && mouseY <= curCard.getY() + curCard.getH());
            }
            cardPanel.repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
        }
    }
}
