package com.example.checkers.controller;

import com.example.checkers.model.GameManager;
import com.example.checkers.network.NetworkClient;
import com.example.checkers.view.BoardView;
import com.example.checkers.view.ThemeManager;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Move {
    private GameManager gameManager;
    private final BoardView boardView;
    private final NetworkClient networkClient;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public Move(GameManager gameManager, BoardView boardView, NetworkClient networkClient) {
        this.gameManager = gameManager;
        this.boardView = boardView;
        this.networkClient = networkClient;
        this.boardView.setGameManager(gameManager);
        setupClickAndDragHandlers();
    }

    private void setupClickAndDragHandlers() {
        Button[][] buttons = boardView.getButtons();
        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                final int r = row;
                final int c = col;
                Button square = buttons[row][col];

                square.setOnAction(event -> handleSquareClick(r, c));

                square.setOnDragDetected(event -> {
                    if (boardView.getButtons()[0][0].getParent().isDisabled()) { event.consume(); return; }
                    if (gameManager.isSelectable(r, c)) {
                        Dragboard db = square.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(r + "," + c);
                        db.setContent(content);
                    }
                    event.consume();
                });

                square.setOnDragOver(event -> {
                    if (event.getGestureSource() != square && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                square.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString() && !boardView.getButtons()[0][0].getParent().isDisabled()) {
                        String[] sourceCoords = db.getString().split(",");
                        int fromRow = Integer.parseInt(sourceCoords[0]);
                        int fromCol = Integer.parseInt(sourceCoords[1]);

                        if (networkClient == null) {
                            String playerName = gameManager.getCurrentPlayer().getName();
                            success = gameManager.performMove(fromRow, fromCol, r, c);
                            if (success) {
                                boardView.updateView();
                                boardView.addMoveToLog(fromRow, fromCol, r, c, playerName);
                            }
                        } else {
                            if (networkClient.getMyColor() == gameManager.getCurrentPlayer().getColor()) {
                                networkClient.sendMove(fromRow, fromCol, r, c);
                                success = true;
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });
            }
        }
    }

    private void handleSquareClick(int row, int col) {
        if (boardView.getButtons()[0][0].getParent().isDisabled()) return;

        if (selectedRow == -1) {
            if (gameManager.isSelectable(row, col)) {
                if (networkClient != null && networkClient.getMyColor() != gameManager.getCurrentPlayer().getColor()) return;
                selectedRow = row;
                selectedCol = col;
                highlightSquare(row, col);
            }
        } else {
            if (selectedRow == row && selectedCol == col) {
                clearHighlight(selectedRow, selectedCol);
                selectedRow = -1;
                selectedCol = -1;
                return;
            }

            if (gameManager.isSelectable(row, col)) {
                clearHighlight(selectedRow, selectedCol);
                selectedRow = row;
                selectedCol = col;
                highlightSquare(row, col);
                return;
            }

            if (networkClient == null) {
                String playerName = gameManager.getCurrentPlayer().getName();
                int fromRow = selectedRow;
                int fromCol = selectedCol;
                if (gameManager.performMove(selectedRow, selectedCol, row, col)) {
                    boardView.updateView();
                    boardView.addMoveToLog(fromRow, fromCol, row, col, playerName);
                }
            } else {
                if (networkClient.getMyColor() == gameManager.getCurrentPlayer().getColor()) {
                    networkClient.sendMove(selectedRow, selectedCol, row, col);
                }
            }

            clearHighlight(selectedRow, selectedCol);
            selectedRow = -1;
            selectedCol = -1;
        }
    }

    private void highlightSquare(int r, int c) {
        String colorStyle = ((r + c) % 2 == 0) ? ThemeManager.lightSquareColor : ThemeManager.darkSquareColor;
        String baseStyle = "-fx-background-color: " + colorStyle + "; " +
                "-fx-background-insets: 0; " +
                "-fx-background-radius: 0; " +
                "-fx-border-color: yellow; " +
                "-fx-border-width: 3;";
        boardView.getButtons()[r][c].setStyle(baseStyle);
    }

    private void clearHighlight(int r, int c) {
        String colorStyle = ((r + c) % 2 == 0) ? ThemeManager.lightSquareColor : ThemeManager.darkSquareColor;
        String baseStyle = "-fx-background-color: " + colorStyle + "; " +
                "-fx-background-insets: 0; " +
                "-fx-background-radius: 0; " +
                "-fx-border-color: transparent; " +
                "-fx-border-width: 3;";
        boardView.getButtons()[r][c].setStyle(baseStyle);
    }
}