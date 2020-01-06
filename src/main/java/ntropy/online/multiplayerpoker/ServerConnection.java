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
import java.net.Socket;
import java.util.ArrayList;

/**
 * Individual connections of client to server.
 *
 * @author NTropy
 * @author Sam Cole
 * @version 10.22.2019
 * @since 4.22.2019
 */
public final class ServerConnection extends Thread {

    /**
     * Connection to server.
     */
    private Socket connection;

    /**
     * Client number.
     */
    private final int number;

    /**
     * Constructor for each connection thread.
     *
     * @param n number given to this client
     */
    public ServerConnection(final int n) {
        number = n;
    }

    /**
     * Thread instructions.
     */
    @Override
    public void run() {
        String inptLine;
        int numCardsRet;
        ArrayList<String> passBack;
        try {
            BufferedReader clientInpt = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            PrintWriter clientOutpt = new PrintWriter(
                    connection.getOutputStream(), true);
            clientOutpt.println(number);
            while (true) {
                try {
                    numCardsRet = Integer.parseInt(clientInpt.readLine());
                    ArrayList<String> cardStaging = new ArrayList<>();
                    while (cardStaging.size() < numCardsRet) {
                        inptLine = clientInpt.readLine();
                        if (inptLine != null) {
                            cardStaging.add(inptLine);
                        }
                    }
                    PokerServer.returnStage(cardStaging);
                    cardStaging.clear();
                    passBack = PokerServer.dealHand(numCardsRet);
                    passBack.forEach((String s) -> {
                        clientOutpt.println(s);
                    });
                    //TODO card staging that adds back cards turned in after
                    //the current round
                    //TODO tell each client how many cards current client
                    //took
                    //TODO improve deck implementation
                } catch (IOException e) {
                    System.err.println("I/O error with client: " + e);
                    clientOutpt.close();
                    try {
                        clientInpt.close();
                    } catch (IOException ie) {
                        System.err.println("Unable to close server input: "
                                + ie);
                    }
                    //DEBUG
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            System.err.println("I/O issue with client: " + e);
            //DEBUG
            System.exit(0);
        }
    }

    /**
     * Accepts connection to main server socket.
     *
     * @param s server socket passed
     */
    public void connect(final Socket s) {
        connection = s;
    }
}
