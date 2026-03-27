package com.example.checkers.server;

import com.example.checkers.model.Board;
import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession extends Thread{
    private final Socket playerWhiteSocket;
    private final Socket playerBlackSocket;

    private final Board board;
    private final GameManager gameManager;

    private PrintWriter outWhite;
    private BufferedReader inWhite;
    private PrintWriter outBlack;
    private BufferedReader inBlack;

    public GameSession(Socket playerWhiteSocket, Socket playerBlackSocket){
        this.playerWhiteSocket = playerWhiteSocket;
        this.playerBlackSocket = playerBlackSocket;
        this.board = new Board();
        this.gameManager = new GameManager(this.board);
    }

    @Override
    public void run(){
        try {
            outWhite = new PrintWriter(playerWhiteSocket.getOutputStream(),true);
            inWhite = new BufferedReader(new InputStreamReader(playerWhiteSocket.getInputStream()));

            outBlack = new PrintWriter(playerBlackSocket.getOutputStream(),true);
            inBlack = new BufferedReader(new InputStreamReader(playerBlackSocket.getInputStream()));

            System.out.println("Sesja gry wystartowała");
            //Wysyłamy sygnał do obu graczy że gra się rozpoczeła
            outWhite.println("Start");
            outBlack.println("Start");
            //Główna pętla
            while (true){
                //sprawdzamy czyja kolej
                boolean isWhiteTurn = (gameManager.getCurrentPlayer().getColor()==Piece.PieceType.WHITE);

                String command;
                if (isWhiteTurn){
                    //Czekamy aż biały wyśle ruch
                    command = inWhite.readLine();
                }else {
                    //Czekamy aż czarny wyśle ruch
                    command = inBlack.readLine();
                }

                //Zgłoszenie rozłączenia
                if(command == null){
                    System.out.println("Jeden z graczy się rozłączył.");
                    break;
                }

                System.out.println("Otrzymano komendę: " + command);
                processCommand(command, isWhiteTurn);
            }
        }catch (IOException e) {
            System.out.println("Błąd połączenia w sesji gry: " + e.getMessage());
        }finally {
            // Sprzątamy po zakończeniu gry
            try {
                playerWhiteSocket.close();
                playerBlackSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //funkcja zmeiniająca tekst na wywołanie metody w GameMenager
    private void processCommand(String command, boolean wasWhiteTurn){
        // Spodziewamy się komendy w stylu: "MOVE 5 4 4 5"
        if (command.startsWith("MOVE")) {
            String[] parts = command.split(" ");
            if (parts.length == 5) {
                try {
                    int fromRow = Integer.parseInt(parts[1]);
                    int fromCol = Integer.parseInt(parts[2]);
                    int toRow = Integer.parseInt(parts[3]);
                    int toCol = Integer.parseInt(parts[4]);

                    // Pytamy naszego czystego GameManagera czy ten ruch jest legalny
                    boolean success = gameManager.performMove(fromRow, fromCol, toRow, toCol);

                    if (success) {
                        // Jeśli ruch się udał, rozsyłamy go do OBU graczy, żeby zaktualizowali swoje ekrany!
                        String updateMsg = "UPDATE " + fromRow + " " + fromCol + " " + toRow + " " + toCol;
                        outWhite.println(updateMsg);
                        outBlack.println(updateMsg);
                    } else {
                        // Jeśli gracz oszukuje lub się pomylił, wysyłamy błąd tylko do niego
                        if (wasWhiteTurn) {
                            outWhite.println("INVALID_MOVE");
                        } else {
                            outBlack.println("INVALID_MOVE");
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Błędny format liczb w komendzie MOVE");
                }
            }
        }
    }
}
