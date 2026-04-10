package com.example.checkers.view;

import com.example.checkers.model.Board;
import com.example.checkers.model.Piece;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

import java.io.InputStream;

public class BoardView {
    private final Board boardModel;
    private final GridPane gridPane;
    private final StackPane rootContainer;
    private final Button[][] buttons = new Button[Board.SIZE][Board.SIZE];
    private boolean isFlipped = false;

    public BoardView(Board boardModel) {
        this.boardModel = boardModel;
        this.gridPane = new GridPane();
        this.rootContainer = new StackPane();

        this.rootContainer.setStyle("-fx-background-color: #4b2e1e;");
        this.gridPane.setStyle("-fx-alignment: center;");

        // skalowanie
        gridPane.maxWidthProperty().bind(Bindings.min(rootContainer.widthProperty(), rootContainer.heightProperty()).subtract(40));
        gridPane.maxHeightProperty().bind(Bindings.min(rootContainer.widthProperty(), rootContainer.heightProperty()).subtract(40));

        rootContainer.getChildren().add(gridPane);

        initializeBoardUI();
    }

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
        iv.setPreserveRatio(true);


        iv.fitWidthProperty().bind(gridPane.widthProperty().divide(Board.SIZE).multiply(0.75));
        iv.fitHeightProperty().bind(gridPane.heightProperty().divide(Board.SIZE).multiply(0.75));

        if (isFlipped) {
            iv.setRotate(180);
        }
        return iv;
    }

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
        // skalowanie procentowe
        for (int i = 0; i < Board.SIZE; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / Board.SIZE);
            gridPane.getColumnConstraints().add(colConst);

            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / Board.SIZE);
            gridPane.getRowConstraints().add(rowConst);
        }

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = new Button();

                // wypelnienie 100% komorki
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                String colorStyle = ((row + col) % 2 == 0) ? "#f0d9b5" : "#b58863";
                String baseStyle = "-fx-background-color: " + colorStyle + "; " +
                        "-fx-background-insets: 0; " +
                        "-fx-background-radius: 0; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent;";

                cell.setStyle(baseStyle);

                cell.setOnMousePressed(e -> cell.setStyle(baseStyle + "-fx-background-color: #f5f682;"));
                cell.setOnMouseReleased(e -> cell.setStyle(baseStyle));

                buttons[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        updateView();
    }

    public StackPane getRootContainer() {
        return rootContainer;
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public Button[][] getButtons() {
        return buttons;
    }

    public void flipBoard() {
        gridPane.setRotate(180);
        this.isFlipped = true;
        updateView();
    }
}