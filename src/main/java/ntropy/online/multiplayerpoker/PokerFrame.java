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
import javax.swing.border.TitledBorder;

/**
 * Multiplayer 5-card draw Poker game.
 *
 * @author NTropy
 * @author Sam Cole
 * @version 4.10.2019
 * @since 4.7.2019
 */
public final class PokerFrame extends JPanel {

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
    private static BufferedImage cardFront, cardBack;

    /**
     * Card array.
     */
    private static Card[] cards;

    /**
     * Card panel.
     */
    private static CardPanel cardPanel;

    /**
     * Main window container.
     */
    private static JFrame mainFrame;

    /**
     * JPanels, (in order), button container, center container, main container.
     */
    private static JPanel buttonPanel, centerPanel, mainPanel;

    /**
     * Constructor, private to avoid instantiation.
     */
    private PokerFrame() {
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
        cardPanel.setBorder(new TitledBorder("Drawn Cards"));
        cardPanel.setOpaque(false);
        cardPanel.addMouseListener(new MouseHandler());
        cardPanel.addMouseMotionListener(new MouseHandler());

        switchBtn = new JButton("Switch");
        switchBtn.addActionListener(new ButtonHandler());

        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(new TitledBorder("Button panel"));
        buttonPanel.add(switchBtn);
        buttonPanel.setOpaque(false);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(cardPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.setBorder(new TitledBorder("Center panel"));
        centerPanel.setOpaque(false);
        centerPanel.setPreferredSize(cardPanel.getPreferredSize());

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(centerPanel);
        mainPanel.setBorder(new TitledBorder("Entire window"));
        mainPanel.setBackground(Color.green);

        mainFrame = new JFrame("Poker Client");
        mainFrame.add(mainPanel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        mainFrame.setLocationByPlatform(true);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    /**
     * Create main frame.
     *
     * @param args command-line arguments; unused here
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            PokerFrame pokerFrame = new PokerFrame();
        });
    }

    /**
     * Handles drawing of cards.
     */
    private static final class CardPanel extends JPanel {

        /**
         * Card container dimensions.
         */
        private final int widthAdjust = 200, heightAdjust = 150,
                cardPanelWidth = FRAME_WIDTH - widthAdjust,
                cardPanelHeight = FRAME_HEIGHT - heightAdjust;

        /**
         * Default constructor.
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
            for (int j = 0; j < cardNum; j++) {
                if (j == 0) {
                    cards[0] = new Card(leftMargin,
                            cardPanelHeight / 2 - cardHeight / 2, cardWidth,
                            cardHeight);
                } else {
                    cards[j] = new Card(leftMargin + cardSpacing * j,
                            cardPanelHeight / 2 - cardHeight / 2, cardWidth,
                            cardHeight);
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
                if (curCard.isFilled()) {
                    g.drawImage(cardBack, curCard.getX(), curCard.getY(), null);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(FRAME_WIDTH - widthAdjust,
                    FRAME_HEIGHT - heightAdjust);
        }
    }

    /**
     * Handles all button events for the frame.
     */
    private static final class ButtonHandler implements ActionListener {

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
                cardPanel.repaint();
            }
        }
    }

    /**
     * Handles all mouse events for the frame.
     */
    private static final class MouseHandler implements MouseListener,
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
