package com.example.checkers.model;

public class Board {
    public static final int SIZE = 8;
    private final Piece[][] squares = new Piece[SIZE][SIZE];

    public Board() {
        initStartingPositions();
    }

    // Dodatkowy konstruktor dla kopii (tworzy pustą planszę)
    private Board(boolean empty) {
        // Nie robi nic, zostawia puste squares
    }

    private void initStartingPositions() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 != 0) {
                    int piecesRows = (SIZE / 2) - 1;
                    if (row < piecesRows) {
                        squares[row][col] = new Piece(Piece.PieceType.BLACK);
                    } else if (row >= SIZE - piecesRows) {
                        squares[row][col] = new Piece(Piece.PieceType.WHITE);
                    }
                }
            }
        }
    }

    public Board copy() {
        Board newBoard = new Board(true); // Tworzymy pustą instancję
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece original = this.getPiece(r, c);
                if (original != null) {
                    Piece copyPiece = new Piece(original.getType());
                    copyPiece.setKing(original.isKing());
                    newBoard.setPiece(r, c, copyPiece);
                }
            }
        }
        return newBoard;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = squares[r][c];
                if (p != null) {
                    int val = (p.getType() == Piece.PieceType.WHITE ? 1 : 5);
                    if (p.isKing()) val *= 2;
                    result = 31 * result + (val * (r * 8 + c));
                }
            }
        }
        return result;
    }

    public Piece[][] getSquares() {
        return squares;
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return null;
        return squares[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            squares[row][col] = piece;
        }
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        squares[toRow][toCol] = squares[fromRow][fromCol];
        squares[fromRow][fromCol] = null;
    }

    public void removePiece(int row, int col) {
        squares[row][col] = null;
    }
}