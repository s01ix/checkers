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
    private boolean isFlipped = false;

    // Pomocnicza metoda do ładowania obrazków
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

        if(isFlipped){
            iv.setRotate(180);
        }
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
                cell.setMinSize(70, 70); // Trochę większe pola

                String colorStyle = ((row + col) % 2 == 0) ? "#f0d9b5" : "#b58863";
                String baseStyle = "-fx-background-color: " + colorStyle + "; " +
                        "-fx-background-insets: 0; " + // Usuwa białe przerwy
                        "-fx-background-radius: 0; " +
                        "-fx-focus-color: transparent; " + // Usuwa niebieską obwódkę focusa
                        "-fx-faint-focus-color: transparent;";

                cell.setStyle(baseStyle);

                //Efekty po najechaniu
                cell.setOnMousePressed(e -> cell.setStyle(baseStyle + "-fx-background-color: #f5f682;"));
                cell.setOnMouseReleased(e -> cell.setStyle(baseStyle));

                buttons[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        updateView(); // Rysujemy pionki na start
    }

    public BoardView(Board boardModel) {
        this.boardModel = boardModel;
        this.gridPane = new GridPane();
        this.gridPane.setStyle("-fx-alignment: center;");
        initializeBoardUI();
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public Button[][] getButtons() {
        return buttons;
    }

    //Obrót planszy dla czarnego pionka
    public void flipBoard(){
        gridPane.setRotate(180);
        this.isFlipped = true;
        updateView();
    }
}
