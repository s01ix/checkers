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

    public NetworkClient(PrintWriter out, BufferedReader in, GameManager localGameManager, BoardView boardView, String username, String colorStr) {
        this.errorMessage = null;

        // Ustalamy kolor na podstawie tego, co przysłał serwer do Lobby
        if ("WHITE".equalsIgnoreCase(colorStr)) {
            this.myColor = Piece.PieceType.WHITE;
        } else {
            this.myColor = Piece.PieceType.BLACK;
        }
        // Inicjalizujemy nadajnik i odbiornik na istniejących strumieniach
        this.sender = new Sender(out);
        this.listener = new Listener(in, localGameManager, boardView);
        // Odpalamy słuchanie ruchów przeciwnika
        new Thread(this.listener).start();

        System.out.println("NetworkClient przejął połączenie. Kolor: " + myColor);
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