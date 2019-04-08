/*
 * Copyright (C) 2019 Ryan Castelli
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
 * @version 4.8.2019
 * @since 4.7.2019
 */
public final class PokerFrame extends JPanel {

    /**
     * Window dimensions.
     */
    private static final int FRAME_WIDTH = 1500, FRAME_HEIGHT = 900;

    /**
     * Card dimensions.
     */
    private final int cardHeight = 5 * FRAME_HEIGHT / 12,
            cardWidth = FRAME_WIDTH / 10,
            cardSpacing = FRAME_WIDTH / 20 + cardWidth, leftMargin = 80,
            cardNum = 5;

    /**
     * Test rectangle dimensions.
     */
    private static final int RECT_X = 100, RECT_Y = 100, RECT_WIDTH = 150,
            RECT_HEIGHT = 350;

    /**
     * Tracks whether rectangle should be filled.
     */
    private static boolean rectActive = false, rectFill = false;

    /**
     * Main window container.
     */
    private static JFrame mainFrame;

    /**
     * Constructor, private to avoid instantiation.
     */
    private PokerFrame() {
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
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException exe) {
            System.err.println("Nimbus unavailable: " + exe);
        }
        mainFrame = new JFrame("Poker Client");
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        mainFrame.getContentPane().add(new PokerFrame());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationByPlatform(true);
        mainFrame.setResizable(false);
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
//        g.setColor(Color.black);
//        final int gridInc = 20;
//        for (int j = 0; j < FRAME_WIDTH; j += gridInc) {
//            g.drawLine(j, 0, j, FRAME_HEIGHT);
//        }
//        for (int j = 0; j < FRAME_HEIGHT; j += gridInc) {
//            g.drawLine(0, j, FRAME_WIDTH, j);
//        }
//        g.setColor(Color.magenta);
//        g.drawLine(0, FRAME_HEIGHT / 2, FRAME_WIDTH, FRAME_HEIGHT / 2);
//        g.drawLine(FRAME_WIDTH / 2, 0, FRAME_WIDTH / 2, FRAME_HEIGHT);
        g.setColor(Color.blue);
        for (int j = 0; j <= cardNum; j++) {
            if (j == 0) {
                g.drawRect(leftMargin, FRAME_HEIGHT / 2 - cardHeight / 2,
                        cardWidth, cardHeight);
            } else {
                g.drawRect(leftMargin + cardSpacing * j,
                        FRAME_HEIGHT / 2 - cardHeight / 2, cardWidth,
                        cardHeight);
            }
        }
//        if (rectActive && !rectFill) {
//            g.drawRect(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT);
//        } else if (rectFill) {
//            g.clearRect(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT);
//            g.fillRect(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT);
//        } else {
//            g.setColor(mainFrame.getBackground());
//            g.fillRect(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT);
//        }
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
            mouseX = e.getX();
            mouseY = e.getY();
            if (mouseX >= RECT_X && mouseX <= RECT_X + RECT_WIDTH
                    && mouseY >= RECT_Y && mouseY <= RECT_Y + RECT_HEIGHT) {
                rectFill = !rectFill;
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
            rectActive = mouseX >= RECT_X && mouseX <= RECT_X + RECT_WIDTH
                    && mouseY >= RECT_Y && mouseY <= RECT_Y + RECT_HEIGHT;
            mainFrame.repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
        }
    }
}
