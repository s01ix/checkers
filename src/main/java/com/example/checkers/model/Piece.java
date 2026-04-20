package com.example.checkers.model;

public class Piece {
    public enum PieceType {
        WHITE, BLACK
    }

    private final PieceType type;
    private boolean isKing = false;

    public Piece(PieceType type) {
        this.type = type;
    }

    public PieceType getType() {
        return type;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        this.isKing = king;
    }

    public void makeKing() {
        this.isKing = true;
    }
}