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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Overhauling server.
 *
 * @author Ntropy
 * @author Sam Cole
 * @version 4.13.2019
 * @since 4.7.2019
 */
public final class PokerServer extends Thread {

    /**
     * Default connection info.
     */
    private static final int DEFAULT_PORT = 22337, DEFAULT_CONNECTION_NUM = 1;

    /**
     * Input from client.
     */
    private static BufferedReader clientInpt;

    /**
     * Client info; largely unused as of now.
     */
    private static int clientPos, connectionNum, numCardsRet;

    /**
     * Card handling.
     */
    private static final ArrayList<String> CARD_STAGING = new ArrayList<>(),
            DECK = new ArrayList<>();

    /**
     * Output to client.
     */
    private static PrintWriter clientOutpt;

    /**
     * Socket for connection to clients.
     */
    private static ServerSocket mainSocket;

    /**
     * Sockets on which clients communicate.
     */
    private static Socket[] clientArr;

    /**
     * Names of clients; unused as of yet.
     */
    private static String[] clientNames;

    /**
     * Connection info for clients.
     */
    private static String clientName, clientInputStr, localIP, publicIP;

    /**
     * Client connection threads.
     */
    private static Thread[] threadArr;

    /**
     * Constructor, invoked to create client threads.
     *
     * @param n
     *          name of client
     * @param c
     *          position of client in array
     */
    private PokerServer(final String n, final int c) {
        clientPos = c;
        clientName = n;
    }

    /**
     * Main method.
     *
     * @param args
     *             command-line arguments; unused here as of yet
     */
    public static void main(final String[] args) {
        try {
            localIP = InetAddress.getLocalHost().getHostAddress().trim();
            publicIP = (new BufferedReader(new InputStreamReader((new URL(
                    "http://checkip.amazonaws.com")).openStream()))).readLine();
        } catch (IOException e) {
            System.err.println("Unable to grab local or public IP: " + e);
        }

        BufferedReader usrInpt = new BufferedReader(new InputStreamReader(
                System.in));

        initializeDeck();
        shuffleDeck();

        int port = getPort(usrInpt);
        getConnectionNum(usrInpt);

        System.out.println("Port: " + port);
        System.out.println("Local IP: " + localIP);
        System.out.println("Public IP: " + publicIP);
        System.out.println("Connection number: " + connectionNum);
        try {
            mainSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Unable to open socket: " + e);
            System.exit(0);
        }
        runThread();
    }

    /**
     * Create and shuffle deck of cards.
     */
    private static void initializeDeck() {
        final String[] cardTypes = {"Ace", "Two", "Three", "Four", "Five",
            "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King"};
        final String[] suits = {"Clubs", "Hearts", "Diamonds", "Spades"};
        for (String curSuit : suits) {
            for (String curType : cardTypes) {
                DECK.add(curSuit + curType);
            }
        }
    }

    /**
     * Shuffle deck the standard 7 times.
     * Probably unnecessary, but traditional.
     */
    private static void shuffleDeck() {
        final int standardShuffle = 7; //rule of thumb: shuffle 7 times
        for (int j = 0; j < standardShuffle; j++) {
            Collections.shuffle(DECK);
        }
    }

    /**
     * Deals a hand to be passed to client.
     *
     * @param handSize
     *          size of hand needed
     * @return hand of cards
     */
    private static ArrayList<String> dealHand(final int handSize) {
        List<String> selection = DECK.subList(DECK.size() - handSize,
                DECK.size());
        ArrayList<String> hand = new ArrayList<>(selection);
        DECK.removeAll(hand);
        selection.clear();
        return hand;
    }

    /**
     * Get valid port.
     *
     * @param br
     *           user input reader
     *
     * @return valid port number
     */
    private static int getPort(final BufferedReader br) {
        boolean valid = false;
        int port = DEFAULT_PORT;
        String usrInpt;
        try {
            while (!valid) {
                System.out.print("\nPlease enter a port number, or press "
                        + "enter to default to " + DEFAULT_PORT + ": ");
                usrInpt = br.readLine();
                if (usrInpt.matches("^[+-]?\\d+$")) {
                    port = Integer.parseInt(usrInpt);
                }
                if (!usrInpt.equals("")) {
                    System.out.print("\nInvalid port number!");
                } else {
                    valid = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to get input from user: " + e);
        }
        return port;
    }

    /**
     * Get valid connection number.
     *
     * @param br
     *           user input reader
     */
    private static void getConnectionNum(final BufferedReader br) {
        boolean valid = false;
        String usrInpt;
        connectionNum = DEFAULT_CONNECTION_NUM;
        try {
            while (!valid) {
                System.out.print("\nPlease enter the desired number of "
                        + "connections, or press enter to default to "
                        + DEFAULT_CONNECTION_NUM + ": ");
                usrInpt = br.readLine();
                if (usrInpt.matches("^[+-]?\\d+$")) {
                    connectionNum = Integer.parseInt(usrInpt);
                }
                if (!usrInpt.equals("")) {
                    System.out.print("\nInvalid connection number!");
                } else {
                    valid = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to get input from user: " + e);
        }
    }

    /**
     * Thread creation instructions.
     */
    private static void runThread() {
        clientArr = new Socket[connectionNum];
        threadArr = new Thread[connectionNum];
        clientNames = new String[connectionNum];

        for (int j = 0; j < clientArr.length; j++) {
            System.out.println("Waiting for connection...");
            try {
                clientArr[j] = mainSocket.accept();
                System.out.println("Connection established with client "
                        + (j + 1));
                threadArr[j] = new PokerServer("NAME", j);
                threadArr[j].start();
                //TODO better error messaging
            } catch (IOException e) {
                System.err.println("Unable to connect with client" + e);
            }
        }
    }

    /**
     * Thread instructions.
     */
    @Override
    public void run() {
        int curClient = clientPos;
        String inptLine;

        while (true) {
            try {
                clientInpt = new BufferedReader(
                        new InputStreamReader(
                                clientArr[curClient].getInputStream()));
                clientOutpt = new PrintWriter(clientArr[curClient].
                        getOutputStream(), true);
                numCardsRet = Integer.parseInt(clientInpt.readLine());
                while (CARD_STAGING.size() < numCardsRet) {
                    inptLine = clientInpt.readLine();
                    if (inptLine != null) {
                        CARD_STAGING.add(inptLine);
                    }
                }

                //TODO card staging that adds back cards turned in after the
                //current round

                //TODO tell each client how many cards current client took

                //TODO add deck implementation
                ArrayList<String> passBack = dealHand(numCardsRet);
                passBack.forEach((String s) -> {
                    clientOutpt.println(s);
                });
                passBack.clear();
            } catch (IOException e) {
                System.err.println("I/O error with client: " + e);
                //DEBUG
                System.exit(0);
            }
        }
    }
}