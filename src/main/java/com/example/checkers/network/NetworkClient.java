package com.example.checkers.network;

import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {
    private Socket socket;
    private Sender sender;
    private Listener listener;

    private Piece.PieceType myColor;
    private String errorMessage;

    public NetworkClient(int port, GameManager localGameManager, BoardView boardView, String username, String password) {
        System.out.println("Szukanie serwera...");
        List<String> ipsToTry = getArpIps();
        ipsToTry.add(0, "127.0.0.1");

        boolean connected = false;

        for (String ip : ipsToTry) {
            try {
                System.out.println("Próba połączenia z " + ip + "...");
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Wysyłamy prośbę o dołączenie do gry (właściwe rozegranie meczu)
                out.println("JOIN " + username + " " + password);
                String loginResponse = in.readLine();

                if (loginResponse != null && loginResponse.startsWith("LOGIN_FAILED ")) {
                    errorMessage = loginResponse.substring("LOGIN_FAILED ".length());
                    socket.close();
                    break;
                } else if ("LOGIN_SUCCESS".equals(loginResponse)) {
                    String colorMessage = in.readLine();
                    if ("CONNECTED WHITE".equals(colorMessage)) {
                        myColor = Piece.PieceType.WHITE;
                    } else if ("CONNECTED BLACK".equals(colorMessage)) {
                        myColor = Piece.PieceType.BLACK;
                    }

                    sender = new Sender(out);
                    listener = new Listener(in, localGameManager, boardView);
                    new Thread(listener).start();

                    connected = true;
                    break;
                } else {
                    socket.close();
                }

            } catch (IOException e) {
            }
        }

        if (!connected && errorMessage == null) {
            System.err.println("Nie udało się znaleźć serwera.");
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static List<String> getArpIps() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                for (String part : parts)
                    if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
                        ipAddresses.add(part);
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Couldnt get arp" + e);
        }
        return ipAddresses;
    }

    public Piece.PieceType getMyColor() {
        return myColor;
    }

    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (sender != null) {
            sender.sendMove(fromRow, fromCol, toRow, toCol);
        }
    }
}