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
 * @version 4.7.2019
 * @since 4.7.2019
 */
public final class PokerFrame extends JPanel {

    /**
     * Window dimensions.
     */
    private static final int FRAME_WIDTH = 1500, FRAME_HEIGHT = 900;

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
        } catch (ClassNotFoundException | IllegalAccessException
                | InstantiationException
                | UnsupportedLookAndFeelException exe) {
            System.err.println("Nimbus unavailable: " + exe);
        }
        mainFrame = new JFrame("Poker Client");
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        mainFrame.getContentPane().add(new PokerFrame());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationByPlatform(true);
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
        g.setColor(Color.red);
        g.drawRect(100, 100, 150, 350);
    }
}
