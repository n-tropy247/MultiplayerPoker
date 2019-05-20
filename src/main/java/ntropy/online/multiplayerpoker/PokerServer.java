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
 * Server to handle poker deck.
 * //TODO betting system
 *
 * @author Ntropy
 * @author Sam Cole
 * @version 4.22.2019
 * @since 4.7.2019
 */
public final class PokerServer {

    /**
     * Card handling.
     */
    private static final ArrayList<String> DECK = new ArrayList<>();

    /**
     * Default connection info.
     */
    private static final int DEFAULT_PORT = 22337, DEFAULT_CONNECTION_NUM = 1;

    /**
     * Client info; largely unused as of now.
     */
    private static int connectionNum;

    /**
     * Array of clients.
     */
    private static ServerConnection[] clients;

    /**
     * Socket for connection to clients.
     */
    private static ServerSocket mainSocket;

    /**
     * Connection info for clients.
     */
    private static String localIP, publicIP;

    /**
     * Private constructor to avoid instantiation.
     */
    private PokerServer() {
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
            //DEBUG
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
     * Receive staged cards from client.
     *
     * @param arr
     *            staged cards return by client
     */
    public static void returnStage(final ArrayList<String> arr) {
        arr.forEach((String s) -> {
            DECK.add(s);
        });
    }

    /**
     * Deals a hand to be passed to client.
     *
     * @param handSize
     *                 size of hand needed
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
        try {
            br.close();
        } catch (IOException e) {
            System.err.println("Unable to close usr input reader: " + e);
        }
    }

    /**
     * Thread creation instructions.
     */
    private static void runThread() {
        clients = new ServerConnection[connectionNum];
        for (int j = 0; j < clients.length; j++) {
            System.out.println("Waiting for connection...");
            try {
                clients[j] = new ServerConnection("NAME");
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
