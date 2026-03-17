package com.example.checkers.model;
import javafx.scene.control.Button;

import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private Board board;
    private Player playerWhite;
    private Player playerBlack;
    private Player currentPlayer;
    private boolean isMultiCapturing = false;
    private int multiCaptureRow = -1;
    private int multiCaptureCol = -1;
    private static final int MAX_MOVES_WITHOUT_CAPTURE = 30;
    private int movesWithoutCapture = 0;
    private Map<String, Integer> positionHistory = new HashMap<>();

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
                if (getPlayerMaxCaptures(currentPlayer) > 0) {
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
                int maxPossible = getPlayerMaxCaptures(currentPlayer);
                boolean[][] simulatedCaptures = new boolean[Board.SIZE][Board.SIZE];
                simulatedCaptures[jumpedRow][jumpedCol] = true;

                if (1 + getPieceMaxCaptures(toRow, toCol, piece, simulatedCaptures) < maxPossible) {
                    System.out.println("Zasada bicia większości! Musisz wybrać ścieżkę, która zbija " + maxPossible + " pionków!");
                    return false;
                }
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

        while (currentRow != toRow && currentCol != toCol) {
            Button currentSquare = board.getSquares()[currentRow][currentCol];
            Piece p = (Piece) currentSquare.getUserData();

            if (p != null) {
                if (encounteredPiece) return false;
                if (p.getType() == piece.getType()) return false;
                encounteredPiece = true;
            }

            currentRow += rowDir;
            currentCol += colDir;
        }

        if (encounteredPiece) {
            int jRow = fromRow + rowDir;
            int jCol = fromCol + colDir;
            while (jRow != toRow && jCol != toCol) {
                if (board.getSquares()[jRow][jCol].getUserData() != null) break;
                jRow += rowDir;
                jCol += colDir;
            }

            // SPRAWDZENIE ZASADY WIĘKSZOŚCI
            int maxPossible = getPlayerMaxCaptures(currentPlayer);
            boolean[][] simulatedCaptures = new boolean[Board.SIZE][Board.SIZE];
            simulatedCaptures[jRow][jCol] = true;

            if (1 + getPieceMaxCaptures(toRow, toCol, piece, simulatedCaptures) < maxPossible) {
                System.out.println("Zasada bicia większości! Musisz wybrać ścieżkę, która zbija " + maxPossible + " pionków!");
                return false;
            }
            return true;
        } else {
            if (isMultiCapturing) return false;
            // Sprawdzenie obowiązku bicia
            if (getPlayerMaxCaptures(currentPlayer) > 0) {
                System.out.println("Uwaga! Masz obowiązek bicia. Zwykły ruch jest niedozwolony.");
                return false;
            }
            return true;
        }
    }

    public int getPlayerMaxCaptures(Player player) {
        int maxCapture = 0;
        Button[][] squares = board.getSquares();
        for(int r = 0; r < Board.SIZE; r++){
            for(int c = 0; c < Board.SIZE; c++){
                Piece p = (Piece) squares[r][c].getUserData();
                if (p != null && p.getType() == player.getColor()) {
                    // Blokujemy inne pionki, jeśli jesteśmy w trakcie wielokrotnego bicia
                    if (isMultiCapturing && (r != multiCaptureRow || c != multiCaptureCol)) continue;

                    int captures = getPieceMaxCaptures(r, c, p, new boolean[Board.SIZE][Board.SIZE]);
                    if (captures > maxCapture) {
                        maxCapture = captures;
                    }
                }
            }
        }
        return maxCapture;
    }

    private int getPieceMaxCaptures(int row, int col, Piece piece, boolean[][] captured) {
        int max = 0;
        if (piece.isKing()) {
            int[] dDirs = {-1, 1};
            for (int dRows : dDirs) {
                for (int dCols : dDirs) {
                    int currentRow = row + dRows;
                    int currentCol = col + dCols;
                    boolean foundEnemy = false;
                    int enemyRow = -1;
                    int enemyCol = -1;

                    while (currentRow >= 0 && currentRow < Board.SIZE && currentCol >= 0 && currentCol < Board.SIZE) {
                        Piece p = (Piece) board.getSquares()[currentRow][currentCol].getUserData();
                        if (p != null) {
                            if (p.getType() == piece.getType() || foundEnemy || captured[currentRow][currentCol]) {
                                break;
                            }
                            foundEnemy = true;
                            enemyRow = currentRow;
                            enemyCol = currentCol;
                        } else if (foundEnemy) {
                            captured[enemyRow][enemyCol] = true; // Oznaczony pionek jako zbity
                            int captureCount = 1 + getPieceMaxCaptures(currentRow, currentCol, piece, captured); // skanujemy czy dalej możemy zbić
                            if (captureCount > max) max = captureCount;
                            captured[enemyRow][enemyCol] = false; // Cofamy symulacje
                        }
                        currentRow += dRows;
                        currentCol += dCols;
                    }
                }
            }
        } else {
            int[] dDirs = {-1, 1};
            for (int dr : dDirs) {
                for (int dc : dDirs) {
                    int jumpedRow = row + dr;
                    int jumpedCol = col + dc;
                    int targetRow = row + 2 * dr;
                    int targetCol = col + 2 * dc;
                    if (targetRow >= 0 && targetRow < Board.SIZE && targetCol >= 0 && targetCol < Board.SIZE) {
                        Piece p = (Piece) board.getSquares()[jumpedRow][jumpedCol].getUserData();
                        if (p != null && p.getType() != piece.getType() && !captured[jumpedRow][jumpedCol]) {
                            if (board.getSquares()[targetRow][targetCol].getUserData() == null) {
                                captured[jumpedRow][jumpedCol] = true;
                                int captures = 1 + getPieceMaxCaptures(targetRow, targetCol, piece, captured);
                                if (captures > max) max = captures;
                                captured[jumpedRow][jumpedCol] = false;
                            }
                        }
                    }
                }
            }
        }
        return max;
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

            if(wasCapture){
                movesWithoutCapture = 0;
                positionHistory.clear();
            }
            else{
                movesWithoutCapture++;
            }

            sourceSquare.setUserData(null);
            sourceSquare.setGraphic(null);
            targetSquare.setGraphic(piece.getImageView());
            targetSquare.setUserData(piece);

            if (wasCapture && getPieceMaxCaptures(toRow, toCol, piece, new boolean[Board.SIZE][Board.SIZE]) > 0) {                isMultiCapturing = true;
                multiCaptureRow = toRow;
                multiCaptureCol = toCol;
                System.out.println("Możliwe wielokrotne bicie! Kontynuuj ruch tym samym pionkiem.");
                return true;
            }

            if(!piece.isKing()){
                if((piece.getType() == Piece.PieceType.WHITE && toRow == 0) ||
                        (piece.getType() == Piece.PieceType.BLACK && toRow == Board.SIZE-1)){
                    piece.makeKing();
                    positionHistory.clear();
                }
            }

            switchPlayer();
            String state = getBoardStateString();
            positionHistory.put(state, positionHistory.getOrDefault(state, 0) + 1);
            checkWin();
            checkDraw();
            return true;
        } else {
            System.out.println("Ruch niedozwolony!");
            return false;
        }
    }

    //Później jak podzielimy na graczy to zmieni się na boolean
    public void checkWin(){
        int whitePeace = 0;
        int blackPieces = 0;
        for(int r = 0; r < Board.SIZE; r++){
            for(int c = 0; c < Board.SIZE; c++){
                Piece piece = (Piece) board.getSquares()[r][c].getUserData();
                if(piece != null){
                    if(piece.getType() == Piece.PieceType.WHITE){
                        whitePeace++;
                    } else {
                        blackPieces++;
                    }
                }
            }
        }
        if(whitePeace == 0){
            System.out.println("Wygrywa gracz: " + playerBlack.getName());
            return;
        } else if(blackPieces == 0){
            System.out.println("Wygrywa gracz: " + playerWhite.getName());
            return;
        }
        if (!hasAnyValidMoves(currentPlayer)) {
            Player winner = (currentPlayer == playerWhite) ? playerBlack : playerWhite;
            System.out.println("====== KONIEC GRY: ZWYCIĘŻYŁ " + winner.getName().toUpperCase() + "! (" + currentPlayer.getName() + " nie ma żadnego ruchu) ======");
        }
    }

    public boolean checkDraw() {
        if (isThreefoldRepetition() || isMovesWithoutCaptureLimitReached() || isMaterialDraw()) {
            System.out.println("====== KONIEC GRY: REMIS! ======");
            return true;
        }
        return false;
    }

    private boolean isThreefoldRepetition() {
        String state = getBoardStateString();
        if (positionHistory.getOrDefault(state, 0) >= 3) {
            System.out.println("Powód remisu: Trzykrotne powtórzenie identycznej pozycji na planszy.");
            return true;
        }
        return false;
    }

    private boolean isMovesWithoutCaptureLimitReached() {
        if (movesWithoutCapture >= MAX_MOVES_WITHOUT_CAPTURE) {
            System.out.println("Powód remisu: Wykonano " + MAX_MOVES_WITHOUT_CAPTURE + " ruchów bez żadnego bicia.");
            return true;
        }
        return false;
    }

    private boolean isMaterialDraw() {
        int whiteKings = 0;
        int blackKings = 0;
        int totalPieces = 0;
        Button[][] squares = board.getSquares();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = (Piece) squares[r][c].getUserData();
                if (p != null) {
                    totalPieces++;
                    if (p.isKing()) {
                        if (p.getType() == Piece.PieceType.WHITE) whiteKings++;
                        else blackKings++;
                    }
                }
            }
        }

        // Remis jeśli na całej planszy zostały tylko dwie figury i są to obie damki
        if (totalPieces == 2 && whiteKings == 1 && blackKings == 1) {
            System.out.println("Powód remisu: Brak możliwości wygranej (Damka vs Damka).");
            return true;
        }
        return false;
    }

    // Tworzy tekstową "mapę" całej planszy, żeby łatwo sprawdzić, czy układ się powtórzył
    private String getBoardStateString() {
        StringBuilder sb = new StringBuilder();
        Button[][] squares = board.getSquares();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = (Piece) squares[row][col].getUserData();
                if (piece == null) {
                    sb.append("-");
                } else {
                    sb.append(piece.getType() == Piece.PieceType.WHITE ? "W" : "B");
                    sb.append(piece.isKing() ? "K" : "P");
                }
            }
        }
        sb.append(currentPlayer.getColor());
        return sb.toString();
    }

    private boolean hasAnyValidMoves(Player player) {
        if (getPlayerMaxCaptures(player) > 0) return true;

        Button[][] squares = board.getSquares();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = (Piece) squares[row][col].getUserData();
                if (piece != null && piece.getType() == player.getColor()) {

                    if (piece.isKing()) {
                        int[] dRows = {-1, 1};
                        int[] dCols = {-1, 1};
                        for (int dr : dRows) {
                            for (int dc : dCols) {
                                int tRow = row + dr;
                                int tCol = col + dc;
                                if (tRow >= 0 && tRow < Board.SIZE && tCol >= 0 && tCol < Board.SIZE) {
                                    if (squares[tRow][tCol].getUserData() == null) return true;
                                }
                            }
                        }
                    } else {
                        int direction = (piece.getType() == Piece.PieceType.WHITE) ? -1 : 1;
                        int[] dCols = {-1, 1};
                        for (int dc : dCols) {
                            int tRow = row + direction;
                            int tCol = col + dc;
                            if (tRow >= 0 && tRow < Board.SIZE && tCol >= 0 && tCol < Board.SIZE) {
                                if (squares[tRow][tCol].getUserData() == null) return true;
                            }
                        }
                    }

                }
            }
        }
        return false;
    }
}