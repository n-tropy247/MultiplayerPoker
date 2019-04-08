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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Multiplayer server for 5-card draw poker.
 *
 * @author NTropy
 * @version 4.7.2019
 * @since 4.7.2019
 */
public final class PokerServer extends Thread {

    /**
     * Default connection info.
     */
    private static final int DEFAULT_PORT = 22337, DEFAULT_CONNECTION_NUM = 1,
            CONNECTION_WAIT = 5000, MAX_ERROR = 3;

    /**
     * Window dimensions.
     */
    private static final int FRAME_WIDTH = 500, FRAME_HEIGHT = 420,
            DISPLAY_ROWS = 20, DISPLAY_COLS = 30, TEXT_FIELD_WIDTH = 30;

    /**
     * ActionEvent to override send button.
     */
    private static ActionEvent enterSend;

    /**
     * Reader for client input.
     */
    private static BufferedReader fromClient;

    /**
     * Client info.
     */
    private static int clientPos, connectionNum, portNum, sendCount = 0;

    /**
     * Sever command send button.
     */
    private static JButton sendBtn;

    /**
     * Primary container frame.
     */
    private static JFrame mainFrame;

    /**
     * Allows scrolling in i/o window.
     */
    private static JScrollPane scrlp;

    /**
     * Primary display window.
     */
    private static JTextArea display;

    /**
     * Primary text input field.
     */
    private static JTextField jtfInput;

    /**
     * Write output to client.
     */
    private static PrintWriter toClient;

    /**
     * Socket for connection to clients.
     */
    private static ServerSocket connectionSocket;

    /**
     * Array of Sockets on which clients connect.
     */
    private static Socket[] clients;

    /**
     * Names of clients.
     */
    private static String[] clientNames;

    /**
     * Client connection info.
     */
    private static String clientName, input, ipLocal, ipPublic;

    /**
     * Client threads.
     */
    private static Thread[] threadArr;

    /**
     * Constructor for client threads.
     *
     * @param n name of client
     * @param c position of client in array
     */
    private PokerServer(final String n, final int c) {
        clientPos = c;
        clientName = n;
    }

    /**
     * Creates GUI.
     */
    private static void createGUI() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ipLocal = (localHost.getHostAddress()).trim();
        } catch (UnknownHostException hostErr) {
            System.err.println("Localhost not recognized: " + hostErr);
        }

        //change look and feel to a retro theme (I just like it better).
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException
                | InstantiationException | UnsupportedLookAndFeelException e) {
            System.err.println("Nimbus unavailable: " + e);
        }

        try {
            URL publicIP = new URL("http://checkip.amazonaws.com");

            BufferedReader ipReader = new BufferedReader(new InputStreamReader(
                    publicIP.openStream()));

            ipPublic = ipReader.readLine();
        } catch (IOException ie) {
            System.err.println("Unable to grab public IP: " + ie);
        }

        mainFrame = new JFrame("Poker Server");
        mainFrame.setLayout(new BorderLayout());

        display = new JTextArea(DISPLAY_ROWS, DISPLAY_COLS);
        display.setEditable(false);
        display.setLineWrap(true);

        scrlp = new JScrollPane(display);

        jtfInput = new JTextField(TEXT_FIELD_WIDTH);

        sendBtn = new JButton("Send");
        sendBtn.addActionListener(new SendHandler());

        KeyListener keyListen = new SendHandler();

        jtfInput.addKeyListener(keyListen);

        mainFrame.add(scrlp, BorderLayout.PAGE_START);

        enterSend = new ActionEvent(sendBtn, ActionEvent.ACTION_PERFORMED,
                "Send");

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout());
        btnPanel.add(jtfInput, BorderLayout.LINE_START);
        btnPanel.add(sendBtn, BorderLayout.LINE_END);

        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationByPlatform(true);
        mainFrame.add(btnPanel, BorderLayout.PAGE_END);
        mainFrame.setFocusable(true);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display.setText("Enter desired port (enter for default: "
                + DEFAULT_PORT + " )");
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

    /**
     * Thread code to establish i/o with clients.
     */
    @Override
    public void run() {
        int curClient = clientPos;
        String threadName = clientName;

        try {
            fromClient = new BufferedReader(
                    new InputStreamReader(clients[curClient].getInputStream()));
            String inptLine;
            while (true) {
                inptLine = fromClient.readLine();
                if (inptLine != null) {
                    display.setText(display.getText() + threadName + ": "
                            + inptLine);

                    for (int j = 0; j < clients.length; j++) {
                        if (j != curClient) {
                            if (clients[j] != null) {
                                toClient = new PrintWriter(
                                        clients[j].getOutputStream(), true);
                                toClient.println(threadName + ": " + inptLine);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client " + threadName + " was stopped: " + e);
            boolean loop = true;
            int loopCount = 0;
            while (loop && loopCount < MAX_ERROR) {
                try {
                    loop = false;
                    display.setText(display.getText()
                            + "\nWaiting for connection...");
                    try {
                        Thread.sleep(CONNECTION_WAIT);
                    } catch (InterruptedException ie) {
                        System.err.println("Thread interrupted: " + ie);
                    }
                    clients[curClient] = connectionSocket.accept();
                    display.setText(display.getText()
                            + "\nConnection restablished with " + threadName
                            + ".");
                    toClient = new PrintWriter(
                            clients[curClient].getOutputStream(), true);
                    fromClient = new BufferedReader(
                            new InputStreamReader(
                                    clients[curClient].getInputStream()));
                    clientNames[curClient] = fromClient.readLine();
                    for (int j = 0; j < clientNames.length; j++) {
                        if (curClient != j) {
                            if (clientNames[curClient].equals(clientNames[j])) {
                                clientNames[curClient] = clientNames[curClient]
                                        + (curClient + 1);
                            }
                        }
                    }
                    toClient.println(curClient + 1);
                    toClient.println("Welcome to the game!");
                    threadArr[curClient] = new PokerServer(
                            clientNames[curClient], curClient);
                    threadArr[curClient].start();
                } catch (IOException ie) {
                    loop = true;
                    loopCount++;
                    System.err.println("Client connection could not be "
                            + "restablished. Tried " + loopCount + " times: "
                            + ie);
                    if (loopCount == MAX_ERROR) {
                        System.err.println("Connection failed too many times");
                    }
                }
            }
        }
    }

    /**
     * Thread instructions.
     */
    private static void runThread() {
        clients = new Socket[connectionNum];
        threadArr = new Thread[connectionNum];
        clientNames = new String[connectionNum];

        display.setText(display.getText() + "\nLocal IP: " + ipLocal);
        display.setText(display.getText() + "\nPublic IP: " + ipPublic);

        for (int j = 0; j < clients.length; j++) {
            display.setText(display.getText() + "\nWaiting for connection...");

            try {
                clients[j] = connectionSocket.accept();
                display.setText(display.getText()
                        + "\nClient " + (j + 1) + " connection established.");
                toClient = new PrintWriter(clients[j].getOutputStream(), true);
                fromClient = new BufferedReader(
                        new InputStreamReader(clients[j].getInputStream()));
                clientNames[j] = fromClient.readLine();

                for (int i = 0; i < clientNames.length; i++) {
                    if (j != i) {
                        if (clientNames[j].equals(clientNames[i])) {
                            clientNames[j] = clientNames[j] + (j + 1);
                        }
                    }
                }

                toClient.println(j + 1);
                toClient.println("Welcome to the game!");

                threadArr[j] = new PokerServer(clientNames[j], j);
                threadArr[j].start();
            } catch (IOException ie) {
                System.err.println("I/O error with client: " + ie);
            }
        }
    }

    /**
     * Handles passing variables on button press.
     */
    private static class SendHandler implements ActionListener, KeyListener {

        /**
         * Action on button/enter key press.
         *
         * @param ae ActionEvent detected
         */
        @Override
        public void actionPerformed(final ActionEvent ae) {
            if (ae.getActionCommand().equals("Send")) {
                sendCount++;

                input = jtfInput.getText();

                switch (sendCount) {
                    case 1:
                        int portInt = 0;
                        try {
                            portInt = Integer.valueOf(input);
                        } catch (NumberFormatException nfe) {
                            System.err.println("Number invalid: " + nfe);
                        }
                        if (!input.equals("")) {
                            portNum = portInt;
                        } else {
                            portNum = DEFAULT_PORT;
                        }
                        try {
                            connectionSocket = new ServerSocket(portNum);
                        } catch (IOException ioe) {
                            System.err.println("Socket could not be created: "
                                    + ioe);
                        }
                        display.setText(display.getText() + "\nYou: " + input);
                        mainFrame.repaint();
                        display.setText(display.getText()
                                + "\nNumber of connections(enter for default: "
                                + DEFAULT_CONNECTION_NUM + " )");
                        break;
                    case 2:
                        int numInt = 0;
                        try {
                            numInt = Integer.valueOf(input);
                        } catch (NumberFormatException nfe) {
                            System.err.println("Number invalid: " + nfe);
                        }
                        if (!input.equals("")) {
                            connectionNum = numInt;
                        } else {
                            connectionNum = DEFAULT_CONNECTION_NUM;
                        }
                        display.setText(display.getText() + "\nYou: " + input);
                        jtfInput.setText("");
                        mainFrame.repaint();
                        runThread();
                        break;
                    default:
                        break;
                }
            }
            jtfInput.setText("");
            mainFrame.repaint();
        }

        /**
         * Invokes handler for server communication.
         *
         * @param e KeyEvent detected
         */
        @Override
        public void keyPressed(final KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                actionPerformed(enterSend);
            }
        }

        /**
         * Necessary override, does nothing.
         *
         * @param e KeyEvent detected
         */
        @Override
        public void keyReleased(final KeyEvent e) {
        }

        /**
         * Necessary override, does nothing.
         *
         * @param e KeyEvent detected
         */
        @Override
        public void keyTyped(final KeyEvent e) {
        }
    }
}
