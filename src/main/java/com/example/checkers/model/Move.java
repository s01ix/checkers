package com.example.checkers.model;

import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Move {
    private GameManager gameManager;
    private Button selectedSquare = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public Move(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void handleMove(Button square, int row, int col) {
        square.setOnAction(event -> {
            if (selectedSquare == null) {
                Piece piece = (Piece) square.getUserData();
                if (piece != null && piece.getType() == gameManager.getCurrentPlayer().getColor()) {
                    selectedSquare = square;
                    selectedRow = row;
                    selectedCol = col;
                    square.setStyle(square.getStyle() + "-fx-border-color: yellow; -fx-border-width: 3;");
                    System.out.println("Wybrano pionek na polu: " + row + "," + col);
                } else if (piece != null) {
                    System.out.println("To nie twój pionek! Teraz gra: " + gameManager.getCurrentPlayer().getName());
                }
            } else {
                if (square != selectedSquare) {
                    gameManager.performMove(selectedRow, selectedCol, row, col);
                }
                resetSelection();
            }
        });

        square.setOnDragDetected(event -> {
            Piece piece = (Piece) square.getUserData();
            if (piece != null && piece.getType() == gameManager.getCurrentPlayer().getColor()) {
                Dragboard db = square.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(row + "," + col);
                db.setContent(content);
            } else if (piece != null) {
                System.out.println("Nie możesz podnieść tego pionka! Teraz gra: " + gameManager.getCurrentPlayer().getName());
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
            if (db.hasString()) {
                String[] sourceCoords = db.getString().split(",");
                int fromRow = Integer.parseInt(sourceCoords[0]);
                int fromCol = Integer.parseInt(sourceCoords[1]);

                // Zlecamy ruch GameManagerowi
                success = gameManager.performMove(fromRow, fromCol, row, col);
            }
            event.setDropCompleted(success);
            event.consume();
        });

        square.setOnDragDone(event -> {
            resetSelection();
            event.consume();
        });
    }

    private void resetSelection() {
        if (selectedSquare != null) {
            if ((selectedRow + selectedCol) % 2 == 0) {
                selectedSquare.setStyle("-fx-background-color: WHITE;");
            } else {
                selectedSquare.setStyle("-fx-background-color: RED;");
            }
        }
        selectedSquare = null;
        selectedRow = -1;
        selectedCol = -1;
    }
}