package com.example.checkers.network;

import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;
import javafx.application.Platform;

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

    public NetworkClient(int port, GameManager localGameManager, BoardView boardView) {
        System.out.println("Szukanie serwera...");
        List<String> ipsToTry = getArpIps();
        ipsToTry.add(0, "127.0.0.1"); // dodajemy localhost najpierw

        boolean connected = false;

        for (String ip : ipsToTry) {
            try {
                System.out.println("Próba połączenia z " + ip + "...");
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 500); // 500ms timeout

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Odbieramy pierwszą wiadomość - nasz kolor
                String colorMessage = in.readLine();
                if ("CONNECTED WHITE".equals(colorMessage)) {
                    myColor = Piece.PieceType.WHITE;
                    System.out.println("Połączono z " + ip + "! Grasz jako BIAŁE.");
                } else if ("CONNECTED BLACK".equals(colorMessage)) {
                    myColor = Piece.PieceType.BLACK;
                    System.out.println("Połączono z " + ip + "! Grasz jako CZARNE.");
                }

                sender = new Sender(out);
                listener = new Listener(in, localGameManager, boardView);
                new Thread(listener).start();

                connected = true;
                break;
            } catch (IOException e) {
                // ignorujemy, spróbujemy następne IP
            }
        }

        if (!connected) {
            System.err.println("Nie udało się znaleźć serwera ani połączyć lokalnie.");
        }
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

    // Tę metodę będzie wywoływać Twój kontroler Move, gdy klikniesz na planszy
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (sender != null) {
            sender.sendMove(fromRow, fromCol, toRow, toCol);
        }
    }
}