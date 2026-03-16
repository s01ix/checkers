package com.example.checkers.model;

public class Player {
    private String name;
    private Piece.PieceType color;


    public Player(String name, Piece.PieceType color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Piece.PieceType getColor() {
        return color;
    }

    public void setColor(Piece.PieceType color) {
        this.color = color;
    }
}
