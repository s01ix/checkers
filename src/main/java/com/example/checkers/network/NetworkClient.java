package com.example.checkers.network;

import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final GameManager localGameManager;
    private final BoardView boardView;
    private Piece.PieceType myColor;

    public NetworkClient(String host, int port, GameManager localGameManager, BoardView boardView) {
        this.localGameManager = localGameManager;
        this.boardView = boardView;

        try {
            System.out.println("Łączenie z serwerem...");
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Odbieramy pierwszą wiadomość - nasz kolor
            String colorMessage = in.readLine();
            if ("CONNECTED WHITE".equals(colorMessage)) {
                myColor = Piece.PieceType.WHITE;
                System.out.println("Połączono! Grasz jako BIAŁE.");
            } else if ("CONNECTED BLACK".equals(colorMessage)) {
                myColor = Piece.PieceType.BLACK;
                System.out.println("Połączono! Grasz jako CZARNE.");
            }

            // Uruchamiamy nasłuchiwanie w tle (żeby nie zamrozić okienka JavaFX)
            new Thread(this::listenToServer).start();

        } catch (IOException e) {
            System.err.println("Nie udało się połączyć z serwerem.");
            e.printStackTrace();
        }
    }

    public Piece.PieceType getMyColor() {
        return myColor;
    }

    // Tę metodę będzie wywoływać Twój kontroler Move, gdy klikniesz na planszy
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        out.println("MOVE " + fromRow + " " + fromCol + " " + toRow + " " + toCol);
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Serwer mówi: " + message);

                if (message.startsWith("UPDATE")) {
                    String[] parts = message.split(" ");
                    int r1 = Integer.parseInt(parts[1]);
                    int c1 = Integer.parseInt(parts[2]);
                    int r2 = Integer.parseInt(parts[3]);
                    int c2 = Integer.parseInt(parts[4]);

                    // WAŻNE: Wszelkie zmiany w okienku JavaFX MUSZĄ być robione w specjalnym wątku!
                    // Do tego służy Platform.runLater
                    Platform.runLater(() -> {
                        // Lokalny menedżer wykonuje legalny ruch (żeby zbić pionki i zmienić tury)
                        localGameManager.performMove(r1, c1, r2, c2);
                        // Odświeżamy obrazki
                        boardView.updateView();
                    });
                } else if (message.equals("INVALID_MOVE")) {
                    System.out.println("Twój ruch został odrzucony przez serwer!");
                }
            }
        } catch (IOException e) {
            System.out.println("Utracono połączenie z serwerem.");
        }
    }
}