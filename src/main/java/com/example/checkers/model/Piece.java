package com.example.checkers.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class Piece {
    public enum PieceType {
        WHITE, BLACK
    }

    private final PieceType type;
    private ImageView imageView;
    private boolean isKing = false;

    public Piece(PieceType type) {
        this.type = type;
        loadImage();
    }

    private void loadImage() {
        String imageFileName = (type == PieceType.WHITE) ? "/com/example/checkers/pieces/white.png" : "/com/example/checkers/pieces/black.png";

        InputStream imageStream = getClass().getResourceAsStream(imageFileName);

        if (imageStream == null) {
            System.err.println("Nie znaleziono pliku: " + imageFileName + " w folderze resources!");
            return;
        }

        Image image = new Image(imageStream);
        this.imageView = new ImageView(image);

        this.imageView.setFitWidth(40);
        this.imageView.setFitHeight(40);
        this.imageView.setPreserveRatio(true);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public PieceType getType() {
        return type;
    }

    public boolean isKing(){ return isKing; }

    public void makeKing(){
        this.isKing = true;
        String kingImageFile = (type == PieceType.WHITE) ? "/com/example/checkers/pieces/white_king.png" : "/com/example/checkers/pieces/black_king.png";
        InputStream imageStream = getClass().getResourceAsStream(kingImageFile);
        if (imageStream != null) {
            this.imageView.setImage(new Image(imageStream));
        } else {
            System.out.println("Brak obrazka damki! Używam powiększenia dla odróżnienia.");
            this.imageView.setFitWidth(48);
            this.imageView.setFitHeight(48);
        }
    }
}