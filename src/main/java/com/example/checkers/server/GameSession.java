package com.example.checkers.server;

import com.example.checkers.model.Board;
import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession extends Thread {
    private final Socket playerWhiteSocket;
    private final Socket playerBlackSocket;

    private Board board;
    private GameManager gameManager;

    private PrintWriter outWhite;
    private BufferedReader inWhite;
    private PrintWriter outBlack;
    private BufferedReader inBlack;

    private volatile boolean gameActive = true;
    private volatile boolean gameEnded = false;

    public GameSession(Socket playerWhiteSocket, Socket playerBlackSocket) {
        this.playerWhiteSocket = playerWhiteSocket;
        this.playerBlackSocket = playerBlackSocket;
        this.board = new Board();
        this.gameManager = new GameManager(this.board);
    }

    @Override
    public void run() {
        try {
            outWhite = new PrintWriter(playerWhiteSocket.getOutputStream(), true);
            inWhite  = new BufferedReader(new InputStreamReader(playerWhiteSocket.getInputStream()));
            outBlack = new PrintWriter(playerBlackSocket.getOutputStream(), true);
            inBlack  = new BufferedReader(new InputStreamReader(playerBlackSocket.getInputStream()));

            System.out.println("Sesja gry wystartowała");
            outWhite.println("Start");
            outBlack.println("Start");

            Thread whiteListenerThread = new Thread(() -> listenToPlayer(inWhite, true));
            Thread blackListenerThread = new Thread(() -> listenToPlayer(inBlack, false));

            whiteListenerThread.start();
            blackListenerThread.start();

            whiteListenerThread.join();
            blackListenerThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Błąd połączenia w sesji gry: " + e.getMessage());
        } finally {
            gameActive = false;
            closeQuietly(playerWhiteSocket);
            closeQuietly(playerBlackSocket);
        }
    }

    private void listenToPlayer(BufferedReader in, boolean isWhitePlayer) {
        try {
            String command;
            while (gameActive && (command = in.readLine()) != null) {
                System.out.println("Otrzymano od " + (isWhitePlayer ? "WHITE" : "BLACK") + ": " + command);
                processCommand(command, isWhitePlayer);
            }
        } catch (IOException e) {
            System.out.println("Gracz " + (isWhitePlayer ? "WHITE" : "BLACK") + " się rozłączył.");
        } finally {
            gameActive = false;
            try {
                if (isWhitePlayer) {
                    playerBlackSocket.close();
                } else {
                    playerWhiteSocket.close();
                }
            } catch (IOException ignored) {}
        }
    }

    private synchronized void processCommand(String command, boolean isWhitePlayer) {
        PrintWriter myOut  = isWhitePlayer ? outWhite : outBlack;
        PrintWriter oppOut = isWhitePlayer ? outBlack  : outWhite;

        if (command.startsWith("MOVE")) {
            if (gameEnded) {
                myOut.println("GAME_ENDED");
                return;
            }

            boolean isWhiteTurn =
                    (gameManager.getCurrentPlayer().getColor() == Piece.PieceType.WHITE);

            if (isWhitePlayer != isWhiteTurn) {
                myOut.println("NOT_YOUR_TURN");
                return;
            }

            String[] parts = command.split(" ");
            if (parts.length == 5) {
                try {
                    int fromRow = Integer.parseInt(parts[1]);
                    int fromCol = Integer.parseInt(parts[2]);
                    int toRow   = Integer.parseInt(parts[3]);
                    int toCol   = Integer.parseInt(parts[4]);

                    boolean success = gameManager.performMove(fromRow, fromCol, toRow, toCol);
                    if (success) {
                        String update = "UPDATE " + fromRow + " " + fromCol
                                + " " + toRow  + " " + toCol;
                        outWhite.println(update);
                        outBlack.println(update);
                    } else {
                        myOut.println("INVALID_MOVE");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Błędny format MOVE");
                }
            }

        } else if (command.equals("SURRENDER")) {
            oppOut.println("OPPONENT_SURRENDERED");
            gameEnded = true;

        } else if (command.equals("DRAW_REQUEST")) {
            oppOut.println("DRAW_REQUEST");

        } else if (command.equals("DRAW_ACCEPT")) {
            outWhite.println("DRAW_ACCEPTED");
            outBlack.println("DRAW_ACCEPTED");
            gameEnded = true;
        } else if (command.equals("DRAW_DECLINE")) {
            oppOut.println("DRAW_DECLINED");

        } else if (command.equals("REMATCH_REQUEST")) {
            if (gameEnded) {
                oppOut.println("REMATCH_REQUEST");
            }

        } else if (command.equals("REMATCH_ACCEPT")) {
            oppOut.println("REMATCH_ACCEPTED");
            gameManager.resetGame();
            gameEnded = false;
            System.out.println("Rewanż zaakceptowany - nowa gra!");

        } else if (command.equals("REMATCH_DECLINE")) {
            oppOut.println("REMATCH_DECLINED");

        } else if (command.equals("LEAVE")) {
            oppOut.println("OPPONENT_LEFT");
            gameActive = false;         }
    }

    private void closeQuietly(Socket s) {
        try {
            if (s != null && !s.isClosed()) s.close();
        } catch (IOException ignored) {}
    }
}