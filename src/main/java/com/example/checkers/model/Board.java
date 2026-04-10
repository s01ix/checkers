package com.example.checkers.model;

public class Board {
    public static final int SIZE = 8;
    private final Piece[][] squares = new Piece[SIZE][SIZE];

    public Board() {
        initStartingPositions();
    }

    private void initStartingPositions() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // W warcabach pionki stoją tylko na ciemnych polach
                if ((row + col) % 2 != 0) {
                    int piecesRows = (SIZE / 2) - 1; // 3 rzędy

                    if (row < piecesRows) {
                        squares[row][col] = new Piece(Piece.PieceType.BLACK);
                    } else if (row >= SIZE - piecesRows) {
                        squares[row][col] = new Piece(Piece.PieceType.WHITE);
                    }
                }
            }
        }
    }

    // Zwraca całą tablicę (przyda się później)
    public Piece[][] getSquares() {
        return squares;
    }
    // Pobiera pionka z konkretnego pola (bezpieczna metoda)
    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return null;
        }
        return squares[row][col];
    }
    // Ustawia pionka na konkretnym polu
    public void setPiece(int row, int col, Piece piece) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            squares[row][col] = piece;
        }
    }
    // Fizyczne przestawienie pionka w tablicy (symulacja ruchu)
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        squares[toRow][toCol] = squares[fromRow][fromCol];
        squares[fromRow][fromCol] = null;
    }
    // Usuwanie zbitego pionka
    public void removePiece(int row, int col) {
        squares[row][col] = null;
    }
}