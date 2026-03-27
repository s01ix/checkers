package com.example.checkers.view;

import com.example.checkers.model.Board;
import com.example.checkers.model.Piece;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import java.io.InputStream;

public class BoardView {
    private final Board boardModel;
    private final GridPane gridPane;
    private final Button[][] buttons = new Button[Board.SIZE][Board.SIZE];

    // Pomocnicza metoda do ładowania obrazków (Twój stary kod z Piece)
    private ImageView createPieceImageView(Piece piece) {
        String path = "/com/example/checkers/pieces/";
        if (piece.getType() == Piece.PieceType.WHITE) {
            path += "white.png";
        } else {
            path += "black.png";
        }

        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Nie znaleziono grafiki: " + path);
            return null;
        }

        ImageView iv = new ImageView(new Image(stream));
        iv.setFitWidth(50);
        iv.setFitHeight(50);
        iv.setPreserveRatio(true);
        return iv;
    }

    // Metoda, która odświeża cały wygląd planszy na podstawie modelu
    public void updateView() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece pieceModel = boardModel.getPiece(row, col);
                if (pieceModel != null) {
                    buttons[row][col].setGraphic(createPieceImageView(pieceModel));
                } else {
                    buttons[row][col].setGraphic(null);
                }
            }
        }
    }

    private void initializeBoardUI() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = new Button();
                cell.setMinSize(60, 60); // Trochę większe pola

                if ((row + col) % 2 == 0) {
                    cell.setStyle("-fx-background-color: #f0d9b5;"); // Ładniejszy beżowy
                } else {
                    cell.setStyle("-fx-background-color: #b58863;"); // Ładniejszy brązowy
                }

                buttons[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        updateView(); // Rysujemy pionki na start
    }

    public BoardView(Board boardModel) {
        this.boardModel = boardModel;
        this.gridPane = new GridPane();
        initializeBoardUI();
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public Button[][] getButtons() {
        return buttons;
    }

}
