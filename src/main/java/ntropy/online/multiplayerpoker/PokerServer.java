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

/**
 * Overhauling server.
 *
 * @author Ntropy
 * @version 4.12.2019
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
    private static int clientPos, connectionNum, portNum, numCardsRet;

    /**
     * Cards received from client.
     */
    private static final ArrayList<String> CARDS_RETURNED = new ArrayList<>();

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

        int port = DEFAULT_PORT;

        String usrInptStr = "s";
        boolean validInpt = false;
        try {
            while (!validInpt) {
                System.out.print("\nPlease enter a port number, or press "
                        + "enter to default to " + DEFAULT_PORT + ": ");
                usrInptStr = usrInpt.readLine();
                if (usrInptStr.matches("^[+-]?\\d+$")) {
                    port = Integer.parseInt(usrInptStr);
                    while (!validInpt) {
                        System.out.print("\nPlease enter the desired number of "
                                + "connections, or press enter to default to "
                                + DEFAULT_CONNECTION_NUM + ": ");
                        usrInptStr = usrInpt.readLine();
                        if (usrInptStr.matches("^[+-]?\\d+$")) {
                            connectionNum = Integer.parseInt(usrInptStr);
                        }  else if (!usrInptStr.equals("")) {
                            System.out.print("\nInvalid connection number!");
                        } else {
                            validInpt = true;
                        }
                    }
                } else if (!usrInptStr.equals("")) {
                    System.out.print("\nInvalid port number!");
                }
            }
        } catch (IOException ie) {
            System.err.println("Couldn't get input from user: " + ie);
        }

        if (usrInptStr.matches("^[+-]?\\d+$")) {
            port = Integer.parseInt(usrInptStr);
        }

        System.out.println("Port: " + port);
        System.out.println("Local IP: " + localIP);
        System.out.println("Public IP: " + publicIP);
        System.out.println("Connection number: " + connectionNum);
        try {
            mainSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Unable to open socket: " + e);
        }
        runThread();
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
        String threadName = clientName;
        String inptLine;

        while (true) {
            try {
                clientInpt = new BufferedReader(
                        new InputStreamReader(
                                clientArr[curClient].getInputStream()));

                numCardsRet = Integer.parseInt(clientInpt.readLine());
                while (CARDS_RETURNED.size() < numCardsRet) {
                    inptLine = clientInpt.readLine();
                    if (inptLine != null) {
                        CARDS_RETURNED.add(inptLine);
                    }
                }
                //TODO tell each client how many cards current client took
                //TODO add deck implementation
                for (int j = 0; j < numCardsRet; j++) {
                    //TODO return each card passed to deck
                    CARDS_RETURNED.remove(0);
                    clientOutpt.println("FROM_SERVER"); //TODO grab from deck
                }
            } catch (IOException e) {
                System.err.println("I/O error with client: " + e);
            }
        }
    }
}
