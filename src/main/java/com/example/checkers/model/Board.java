package com.example.checkers.model;

import javafx.scene.layout.GridPane;
import javafx.scene.control.*;

public class Board {
    private GameManager gameManager;
    private Move move;
    public static final int SIZE = 8;
    private final Button[][] squares = new Button[SIZE][SIZE];

    public Board() {
        this.gameManager = new GameManager(this);
        this.move = new Move(this.gameManager);
    }

    public Button[][] getSquares() {
        return squares;
    }

    public GridPane initBoard() {
        GridPane grid = new GridPane();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button cell = new Button();
                cell.setMinSize(50, 50);

                if ((row + col) % 2 == 0) {
                    cell.setStyle("-fx-background-color: WHITE;");
                } else {
                    cell.setStyle("-fx-background-color: RED;");

                    int piecesRows = (SIZE / 2) - 1;
                    if (row < piecesRows) {
                        Piece blackPiece = new Piece(Piece.PieceType.BLACK);
                        cell.setGraphic(blackPiece.getImageView());
                        cell.setUserData(blackPiece);
                    } else if (row >= SIZE - piecesRows) {
                        Piece whitePiece = new Piece(Piece.PieceType.WHITE);
                        cell.setGraphic(whitePiece.getImageView());
                        cell.setUserData(whitePiece);
                    }
                    move.handleMove(cell, row, col);
                }

                squares[row][col] = cell;
                grid.add(cell, col, row);
            }
        }
        return grid;
    }
}