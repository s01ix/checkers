package com.example.checkers.model;
import javafx.scene.control.Button;

public class GameManager {
    private Board board;
    private Player playerWhite;
    private Player playerBlack;
    private Player currentPlayer;

    private boolean isMultiCapturing = false;
    private int multiCaptureRow = -1;
    private int multiCaptureCol = -1;

    public GameManager(Board board) {
        this.board = board;
        this.playerWhite = new Player("Biały", Piece.PieceType.WHITE);
        this.playerBlack = new Player("Czarny", Piece.PieceType.BLACK);
        this.currentPlayer = playerWhite;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == playerWhite) ? playerBlack : playerWhite;
        isMultiCapturing = false;
    }

    public boolean isAvailableMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (toRow < 0 || toRow >= Board.SIZE || toCol < 0 || toCol >= Board.SIZE) return false;

        if (isMultiCapturing) {
            if (fromRow != multiCaptureRow || fromCol != multiCaptureCol) {
                System.out.println("Musisz dokończyć wielokrotne bicie tym samym pionkiem!");
                return false;
            }
        }

        Button sourceSquare = board.getSquares()[fromRow][fromCol];
        Piece piece = (Piece) sourceSquare.getUserData();
        if (piece == null) return false;

        Button targetSquare = board.getSquares()[toRow][toCol];
        if (targetSquare.getUserData() != null) return false;

        int dx = Math.abs(toCol - fromCol);
        int dy = Math.abs(toRow - fromRow);

        if (dx != dy) return false;

        if (piece.isKing()) {
            return isAvailableKingMove(fromRow, fromCol, toRow, toCol, piece);
        } else {
            return isAvailableRegularMove(fromRow, fromCol, toRow, toCol, piece);
        }
    }

    private boolean isAvailableRegularMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        int dx = Math.abs(toCol - fromCol);
        int rowDiff = toRow - fromRow;
        int direction = (piece.getType() == Piece.PieceType.WHITE) ? -1 : 1;

        if (dx == 1) {
            if (rowDiff == direction) {
                if (isMultiCapturing) return false;
                if (hasMandatoryCapture(currentPlayer)) {
                    System.out.println("Uwaga! Masz obowiązek bicia. Zwykły ruch jest niedozwolony.");
                    return false;
                }
                return true;
            }
            return false;
        }

        if (dx == 2 && Math.abs(rowDiff) == 2) {
            int jumpedRow = (fromRow + toRow) / 2;
            int jumpedCol = (fromCol + toCol) / 2;
            Button jumpedSquare = board.getSquares()[jumpedRow][jumpedCol];
            Piece jumpedPiece = (Piece) jumpedSquare.getUserData();
            if (jumpedPiece != null && jumpedPiece.getType() != piece.getType()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAvailableKingMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        int rowDir = (toRow - fromRow) > 0 ? 1 : -1;
        int colDir = (toCol - fromCol) > 0 ? 1 : -1;

        int currentRow = fromRow + rowDir;
        int currentCol = fromCol + colDir;

        boolean encounteredPiece = false;
        Piece capturedPiece = null;

        while (currentRow != toRow && currentCol != toCol) {
            Button currentSquare = board.getSquares()[currentRow][currentCol];
            Piece p = (Piece) currentSquare.getUserData();

            if (p != null) {
                if (encounteredPiece) return false;
                if (p.getType() == piece.getType()) return false; // Nie można przeskoczyć swojego
                encounteredPiece = true;
                capturedPiece = p;
            }

            currentRow += rowDir;
            currentCol += colDir;
        }

        if (encounteredPiece) {
            return true;
        } else {
            if (isMultiCapturing) return false;
            if (hasMandatoryCapture(currentPlayer)) {
                System.out.println("Uwaga! Masz obowiązek bicia. Zwykły ruch jest niedozwolony.");
                return false;
            }
            return true;
        }
    }

    private boolean canPieceCapture(int row, int col, Piece piece) {
        if (piece.isKing()) {
            int[] dRows = {-1, 1};
            int[] dCols = {-1, 1};

            for (int dRow : dRows) {
                for (int dCol : dCols) {
                    int currentRow = row + dRow;
                    int currentCol = col + dCol;
                    boolean foundEnemy = false;

                    while (currentRow >= 0 && currentRow < Board.SIZE && currentCol >= 0 && currentCol < Board.SIZE) {
                        Button currentSquare = board.getSquares()[currentRow][currentCol];
                        Piece p = (Piece) currentSquare.getUserData();

                        if (p == null) {
                            if (foundEnemy) {
                                return true;
                            }
                        } else {
                            if (foundEnemy) break;
                            if (p.getType() == piece.getType()) break;
                            foundEnemy = true;
                        }

                        currentRow += dRow;
                        currentCol += dCol;
                    }
                }
            }
            return false;
        } else {
            int[] dRows = {-2, 2};
            int[] dCols = {-2, 2};

            for (int dRow : dRows) {
                for (int dCol : dCols) {
                    int targetRow = row + dRow;
                    int targetCol = col + dCol;

                    if (targetRow >= 0 && targetRow < Board.SIZE && targetCol >= 0 && targetCol < Board.SIZE) {
                        Button targetSquare = board.getSquares()[targetRow][targetCol];
                        if (targetSquare.getUserData() == null) {

                            int jumpedRow = row + (dRow / 2);
                            int jumpedCol = col + (dCol / 2);
                            Button jumpedSquare = board.getSquares()[jumpedRow][jumpedCol];
                            Piece jumpedPiece = (Piece) jumpedSquare.getUserData();

                            if (jumpedPiece != null && jumpedPiece.getType() != piece.getType()) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    private boolean hasMandatoryCapture(Player player) {
        Button[][] squares = board.getSquares();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = (Piece) squares[row][col].getUserData();
                if (piece != null && piece.getType() == player.getColor()) {
                    if (canPieceCapture(row, col, piece)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Button getCapturedSquare(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDir = (toRow - fromRow) > 0 ? 1 : -1;
        int colDir = (toCol - fromCol) > 0 ? 1 : -1;

        int currentRow = fromRow + rowDir;
        int currentCol = fromCol + colDir;

        while (currentRow != toRow && currentCol != toCol) {
            Button currentSquare = board.getSquares()[currentRow][currentCol];
            if (currentSquare.getUserData() != null) {
                return currentSquare;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }
        return null;
    }

    public boolean performMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isAvailableMove(fromRow, fromCol, toRow, toCol)) {
            Button[][] squares = board.getSquares();
            Button sourceSquare = squares[fromRow][fromCol];
            Button targetSquare = squares[toRow][toCol];

            Piece piece = (Piece) sourceSquare.getUserData();

            boolean wasCapture = false;

            Button capturedSquare = getCapturedSquare(fromRow, fromCol, toRow, toCol);

            if (capturedSquare != null) {
                capturedSquare.setGraphic(null);
                capturedSquare.setUserData(null);
                wasCapture = true;
                System.out.println("Zbito pionek przeciwnika!");
            }

            sourceSquare.setUserData(null);
            sourceSquare.setGraphic(null);
            targetSquare.setGraphic(piece.getImageView());
            targetSquare.setUserData(piece);

            boolean promotedKing = false;
            if(!piece.isKing()){
                if((piece.getType() == Piece.PieceType.WHITE && toRow == 0) ||
                        (piece.getType() == Piece.PieceType.BLACK && toRow == Board.SIZE-1)){
                    piece.makeKing();
                    promotedKing = true;
                }
            }
            if (wasCapture && !promotedKing && canPieceCapture(toRow, toCol, piece)) {
                isMultiCapturing = true;
                multiCaptureRow = toRow;
                multiCaptureCol = toCol;
                System.out.println("Możliwe wielokrotne bicie! Kontynuuj ruch tym samym pionkiem.");
                return true;
            }

            switchPlayer();
            return true;
        } else {
            System.out.println("Ruch niedozwolony!");
            return false;
        }
    }
}