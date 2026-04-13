package com.example.checkers.network;

import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {
    private final Sender sender;
    private final Listener listener;
    private final Piece.PieceType myColor;

    public NetworkClient(PrintWriter out, BufferedReader in,
                         GameManager localGameManager, BoardView boardView,
                         String username, String colorStr) {

        if ("WHITE".equalsIgnoreCase(colorStr)) {
            this.myColor = Piece.PieceType.WHITE;
        } else {
            this.myColor = Piece.PieceType.BLACK;
        }

        this.sender   = new Sender(out);
        this.listener = new Listener(in, localGameManager, boardView, this.sender);

        new Thread(this.listener).start();

        System.out.println("NetworkClient gotowy. Kolor: " + myColor);
    }

    public Piece.PieceType getMyColor() { return myColor; }

    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        sender.sendMove(fromRow, fromCol, toRow, toCol);
    }

    public void sendSurrender()      { sender.sendSurrender(); }
    public void sendDrawRequest()    { sender.sendDrawRequest(); }
    public void sendRematchRequest() { sender.sendRematchRequest(); }

    public void sendLeave() { sender.sendLeave(); }

    public static List<String> getArpIps() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("arp", "-a");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                            ipAddresses.add(part);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Nie można pobrać ARP: " + e.getMessage());
        }
        return ipAddresses;
    }
}