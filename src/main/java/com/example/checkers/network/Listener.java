package com.example.checkers.network;

import com.example.checkers.model.GameManager;
import com.example.checkers.view.BoardView;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;

public class Listener implements Runnable {
    private final BufferedReader in;
    private final GameManager localGameManager;
    private final BoardView boardView;

    public Listener(BufferedReader in, GameManager localGameManager, BoardView boardView) {
        this.in = in;
        this.localGameManager = localGameManager;
        this.boardView = boardView;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("UPDATE")) {
                    String[] parts = message.split(" ");
                    int r1 = Integer.parseInt(parts[1]);
                    int c1 = Integer.parseInt(parts[2]);
                    int r2 = Integer.parseInt(parts[3]);
                    int c2 = Integer.parseInt(parts[4]);

                    Platform.runLater(() -> {
                        String pName = localGameManager.getCurrentPlayer().getName();
                        localGameManager.performMove(r1, c1, r2, c2);
                        boardView.updateView();
                        boardView.addMoveToLog(r1, c1, r2, c2, pName);
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("Utracono połączenie z serwerem.");
        }
    }
}