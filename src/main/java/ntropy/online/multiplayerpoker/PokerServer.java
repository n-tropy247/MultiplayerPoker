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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server to handle poker deck. //TODO betting system
 *
 * @author Ntropy
 * @author Sam Cole
 * @version 10.22.2019
 * @since 4.7.2019
 */
public final class PokerServer {

    /**
     * Full deck of cards.
     */
    private static final ArrayList<String> DECK = new ArrayList<>();

    /**
     * Tracks turn number.
     */
    private static int curTurn = 0, clientNum;

    /**
     * Private constructor to avoid instantiation.
     */
    private PokerServer() {
    }

    /**
     * Main method.
     *
     * @param args command-line arguments; unused here as of yet
     */
    public static void main(final String[] args) {
        String localIP = "", publicIP = "";
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

        final int defaultPort = 22337;
        int port = getPort(usrInpt, defaultPort);

        clientNum = getConnectionNum(usrInpt, 1);

        System.out.println("Port: " + port);
        System.out.println("Local IP: " + localIP);
        System.out.println("Public IP: " + publicIP);
        System.out.println("Connection number: " + clientNum);
        try {
            ServerSocket mainSocket = new ServerSocket(port);
            runThread(clientNum, mainSocket);
        } catch (IOException e) {
            System.err.println("Unable to open socket: " + e);
            //DEBUG
            System.exit(0);
        }
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
     * Shuffle deck the standard 7 times. Probably unnecessary, but traditional.
     */
    private static void shuffleDeck() {
        final int standardShuffle = 7; //rule of thumb: shuffle 7 times
        for (int j = 0; j < standardShuffle; j++) {
            Collections.shuffle(DECK);
        }
    }

    /**
     * Increments turn.
     */
    public static void nextTurn() {
        curTurn++;
        if (curTurn >= clientNum) {
            curTurn = 0;
        }
    }

    /**
     * Allows client to check if it's its turn.
     *
     * @param n number to check
     * @return true iff client's turn
     */
    public static boolean queryTurn(final int n) {
        return curTurn == n;
    }

    /**
     * Receive staged cards from client.
     *
     * @param arr staged cards return by client
     */
    public static void returnStage(final ArrayList<String> arr) {
        arr.forEach((String s) -> {
            DECK.add(s);
        });
    }

    /**
     * Deals a hand to be passed to client.
     *
     * @param handSize size of hand needed
     *
     * @return hand of cards
     */
    public static ArrayList<String> dealHand(final int handSize) {
        List<String> selection = DECK.subList(DECK.size() - handSize,
                DECK.size());
        ArrayList<String> hand = new ArrayList<>(selection);
        selection.clear();
        DECK.removeAll(hand);
        return hand;
    }

    /**
     * Get valid port.
     *
     * @param br user input reader
     * @param defaultPort default port number
     *
     * @return valid port number
     */
    private static int getPort(final BufferedReader br, final int defaultPort) {
        boolean valid = false;
        int port = defaultPort;
        String usrInpt;
        try {
            while (!valid) {
                System.out.print("\nPlease enter a port number, or press "
                        + "enter to default to " + defaultPort + ": ");
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
     * @param br user input reader
     * @param defaultNum default number of client connections
     *
     * @return desired number of client connections
     */
    private static int getConnectionNum(final BufferedReader br,
            final int defaultNum) {
        boolean valid = false;
        String usrInpt;
        int connectionNum = defaultNum;
        try {
            while (!valid) {
                System.out.print("\nPlease enter the desired number of "
                        + "connections, or press enter to default to "
                        + defaultNum + ": ");
                usrInpt = br.readLine();
                if (usrInpt.matches("\\d+")) {
                    connectionNum = Integer.parseInt(usrInpt);
                    valid = true;
                } else if (!usrInpt.equals("")) {
                    System.out.print("\nInvalid connection number!");
                } else {
                    valid = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to get input from user: " + e);
        }
        try {
            br.close();
        } catch (IOException e) {
            System.err.println("Unable to close usr input reader: " + e);
        }
        return connectionNum;
    }

    /**
     * Thread creation instructions.
     *
     * @param connectionNum number of client connections to create
     * @param mainSocket socket to which clients will connect.
     */
    private static void runThread(final int connectionNum,
            final ServerSocket mainSocket) {
        ServerConnection[] clients = new ServerConnection[connectionNum];
        for (int j = 0; j < clients.length; j++) {
            System.out.println("Waiting for connection...");
            try {
                clients[j] = new ServerConnection(j);
                clients[j].connect(mainSocket.accept());
                System.out.println("Connection established with client "
                        + (j + 1));
                clients[j].start();
                //TODO better error messaging
            } catch (IOException e) {
                System.err.println("Unable to connect with client" + e);
            }
        }
    }
}
