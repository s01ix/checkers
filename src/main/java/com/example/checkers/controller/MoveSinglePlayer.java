package com.example.checkers.controller;

import com.example.checkers.model.ComputerPlayer;
import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class MoveSinglePlayer {
    private final GameManager gameManager;
    private final BoardView boardView;
    private final ComputerPlayer ai;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public MoveSinglePlayer(GameManager gameManager, BoardView boardView, ComputerPlayer ai) {
        this.gameManager = gameManager;
        this.boardView = boardView;
        this.ai = ai;
        setupHandlers();
    }

    private void setupHandlers() {
        Button[][] buttons = boardView.getButtons();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int row = r;
                final int col = c;
                buttons[r][c].setOnAction(e -> handlePlayerMove(row, col));
            }
        }
    }

    private void handlePlayerMove(int row, int col) {
        if (gameManager.getCurrentPlayer().getColor() != Piece.PieceType.WHITE) return;

        if (selectedRow == -1) {
            if (gameManager.isSelectable(row, col)) {
                selectedRow = row;
                selectedCol = col;
                highlight(row, col);
            }
        } else {
            boolean moved = gameManager.performMove(selectedRow, selectedCol, row, col);
            clearHighlight(selectedRow, selectedCol);
            selectedRow = -1;
            selectedCol = -1;

            if (moved) {
                boardView.updateView();
                checkAiTurn();
            }
        }
    }

    private void checkAiTurn() {
        if (gameManager.getCurrentPlayer().getColor() == Piece.PieceType.BLACK) {
            PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
            pause.setOnFinished(e -> {
                ai.makeMove();
                boardView.updateView();
                if (gameManager.getCurrentPlayer().getColor() == Piece.PieceType.BLACK) {
                    checkAiTurn();
                }
            });
            pause.play();
        }
    }

    private void highlight(int r, int c) {
        boardView.getButtons()[r][c].setStyle("-fx-border-color: yellow; -fx-border-width: 3;");
    }

    private void clearHighlight(int r, int c) {
        String color = (r + c) % 2 == 0 ? "#f0d9b5" : "#b58863";
        boardView.getButtons()[r][c].setStyle("-fx-background-color: " + color + ";");
    }
}