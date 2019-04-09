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
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Multiplayer 5-card draw Poker game.
 *
 * @author NTropy
 * @author Sam Cole
 * @version 4.8.2019
 * @since 4.7.2019
 */
public final class PokerFrame extends JPanel {

    /**
     * Button dimensions.
     */
    private static final int SWITCH_BTN_X = 1300, SWITCH_BTN_Y = 750,
            SWITCH_BTN_WIDTH = 120, SWITCH_BTN_HEIGHT = 40;

    /**
     * Window dimensions.
     */
    private static final int FRAME_WIDTH = 1480, FRAME_HEIGHT = 900;

    /**
     * Button to send cards to be switched out.
     */
    private static JButton switchBtn;

    /**
     * Card images.
     */
    private BufferedImage cardFront, cardBack;

    /**
     * Card dimensions.
     */
    /**
     * Card array.
     */
    private static Card[] cards;

    /**
     * Main window container.
     */
    private static JFrame mainFrame;

    /**
     * Constructor, private to avoid instantiation.
     */
    private PokerFrame() {
        try {
            cardFront = ImageIO.read(new File("cardFront.png"));
            cardBack = ImageIO.read(new File("cardBack.png"));
        } catch (IOException ie) {
            System.err.println("Image sources couldn't be accessed: " + ie);
            System.exit(0);
        }
        final int cardHeight = FRAME_HEIGHT / 3,
                cardWidth = FRAME_WIDTH / 8,
                cardSpacing = FRAME_WIDTH / 20 + cardWidth, leftMargin = 100,
                cardNum = 5;
        cards = new Card[cardNum];
        for (int j = 0; j < cardNum; j++) {
            if (j == 0) {
                cards[0] = new Card(leftMargin,
                        FRAME_HEIGHT / 2 - cardHeight / 2, cardWidth,
                        cardHeight);
            } else {
                cards[j] = new Card(leftMargin + cardSpacing * j,
                        FRAME_HEIGHT / 2 - cardHeight / 2, cardWidth,
                        cardHeight);
            }
        }
    }

    /**
     * Create actual GUI.
     */
    private static void createGUI() {
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
        mainFrame = new JFrame("Poker Client");
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        mainFrame.getContentPane().add(new PokerFrame());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationByPlatform(true);
        mainFrame.setResizable(false);
        mainFrame.setBackground(Color.lightGray);
        switchBtn = new JButton("Switch");
        switchBtn.addActionListener(new ButtonHandler());
        switchBtn.setBounds(SWITCH_BTN_X, SWITCH_BTN_Y, SWITCH_BTN_WIDTH,
                SWITCH_BTN_HEIGHT);
        mainFrame.add(switchBtn, BorderLayout.LINE_END);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.addMouseListener(new MouseHandler());
        mainFrame.addMouseMotionListener(new MouseHandler());
    }

    /**
     * Create main frame.
     *
     * @param args command-line arguments; unused here
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            createGUI();
        });
    }

    @Override
    public void paintComponent(final Graphics g) {
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
            if (curCard.isFilled()) {
                g.drawImage(cardBack, curCard.getX(), curCard.getY(), null);
            }
        }
    }

    /**
     * Handles all button events for the frame.
     */
    private static class ButtonHandler implements ActionListener {

        /**
         * Possible commands.
         */
        private final String switchCmd = "Switch";

        @Override
        public void actionPerformed(final ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(switchCmd)) {
                for (Card curCard : cards) {
                    if (curCard.isFilled()) {
                        curCard.setFill(false);
                    }
                }
                mainFrame.repaint();
            }
        }
    }

    /**
     * Handles all mouse events for the frame.
     */
    private static class MouseHandler implements MouseListener,
            MouseMotionListener {

        /**
         * Mouse positions.
         */
        private static int mouseX, mouseY;

        @Override
        public void mousePressed(final MouseEvent e) {
            for (Card curCard : cards) {
                if (curCard.isActive()) {
                    curCard.setFill(!curCard.isFilled());
                }
            }
            mainFrame.repaint();
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
            mainFrame.repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
        }
    }
}
