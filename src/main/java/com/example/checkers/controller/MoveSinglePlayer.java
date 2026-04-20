package com.example.checkers.controller;

import com.example.checkers.model.ComputerPlayer;
import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;
import com.example.checkers.view.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.util.Duration;
import java.io.PrintWriter;

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
        this.boardView.setGameManager(gameManager);
        setupHandlers();
    }

    private void autoSave() {
        try (PrintWriter out = new PrintWriter("autosave_single.json")) {
            out.print(boardView.getGameStateAsJson());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupHandlers() {
        Button[][] buttons = boardView.getButtons();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int row = r, col = c;
                buttons[r][c].setOnAction(e -> handlePlayerMove(row, col));
            }
        }
    }

    private void handlePlayerMove(int row, int col) {
        if (gameManager.getCurrentPlayer().getColor() != Piece.PieceType.WHITE) return;

        if (selectedRow == -1) {
            if (gameManager.isSelectable(row, col)) {
                selectedRow = row; selectedCol = col;
                highlight(row, col);
            }
        } else {
            if (selectedRow == row && selectedCol == col) {
                clearHighlight(selectedRow, selectedCol);
                selectedRow = -1; selectedCol = -1;
                return;
            }
            if (gameManager.isSelectable(row, col)) {
                clearHighlight(selectedRow, selectedCol);
                selectedRow = row; selectedCol = col;
                highlight(row, col);
                return;
            }

            int fr = selectedRow, fc = selectedCol;
            if (gameManager.performMove(selectedRow, selectedCol, row, col)) {
                boardView.updateView();
                boardView.addMoveToLog(fr, fc, row, col, "Gracz");
                autoSave();
                if (checkGameOver()) return;

                if (gameManager.getCurrentPlayer().getColor() == Piece.PieceType.BLACK) {
                    checkAiTurn();
                }
            }

            clearHighlight(selectedRow, selectedCol);
            selectedRow = -1; selectedCol = -1;
        }
    }

    private void checkAiTurn() {
        boardView.disableBoard(); // Blokujemy planszę, żeby gracz nie klikał podczas ruchu bota
        PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
        pause.setOnFinished(e -> {
            int[] move = ai.makeMoveAndGetCoords(); // Teraz bot zwraca tablicę int[] z ruchem
            if (move != null) {
                boardView.updateView();
                boardView.addMoveToLog(move[0], move[1], move[2], move[3], "Komputer");
                autoSave();

                if (checkGameOver()) return;

                // Jeśli bot ma serię bić, wywołujemy go ponownie
                if (gameManager.getCurrentPlayer().getColor() == Piece.PieceType.BLACK) {
                    checkAiTurn();
                } else {
                    boardView.enableBoard();
                }
            } else {
                boardView.enableBoard();
            }
        });
        pause.play();
    }

    private boolean checkGameOver() {
        String result = gameManager.checkWin();
        if (!result.equals("NONE")) {
            boardView.disableBoard();
            new java.io.File("autosave_single.json").delete();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Koniec Gry");
            if (result.equals("WHITE")) alert.setHeaderText("Wygrał biały!");
            else if (result.equals("BLACK")) alert.setHeaderText("Wygrał czarny!");
            else alert.setHeaderText("Remis!");
            alert.setContentText("Możesz zagrać ponownie lub opuścić grę.");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    private void highlight(int r, int c) {
        String colorStyle = ((r + c) % 2 == 0) ? ThemeManager.lightSquareColor : ThemeManager.darkSquareColor;
        String baseStyle = "-fx-background-color: " + colorStyle + "; " +
                "-fx-background-insets: 0; -fx-background-radius: 0; " +
                "-fx-border-color: yellow; -fx-border-width: 3;";
        boardView.getButtons()[r][c].setStyle(baseStyle);
    }

    private void clearHighlight(int r, int c) {
        String colorStyle = ((r + c) % 2 == 0) ? ThemeManager.lightSquareColor : ThemeManager.darkSquareColor;
        String baseStyle = "-fx-background-color: " + colorStyle + "; " +
                "-fx-background-insets: 0; -fx-background-radius: 0; " +
                "-fx-border-color: transparent; -fx-border-width: 3;";
        boardView.getButtons()[r][c].setStyle(baseStyle);
    }
}