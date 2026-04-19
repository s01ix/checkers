package com.example.checkers.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Board getBoard() {
        return board;
    }

    public String saveGameStateJsonCore() {
        StringBuilder sb = new StringBuilder();
        sb.append("  \"currentPlayer\": \"").append(currentPlayer.getColor()).append("\",\n");
        sb.append("  \"movesWithoutCapture\": ").append(movesWithoutCapture).append(",\n");
        sb.append("  \"board\": [\n");
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r, c);
                sb.append("    \"");
                if (p == null) {
                    sb.append("0");
                } else {
                    sb.append(p.getType() == Piece.PieceType.WHITE ? "W" : "B");
                    sb.append(p.isKing() ? "K" : "P");
                }
                sb.append("\"");
                if (r < Board.SIZE - 1 || c < Board.SIZE - 1) sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]");
        return sb.toString();
    }

    public void loadGameStateJsonCore(String json) {
        Matcher mPlayer = Pattern.compile("\"currentPlayer\"\\s*:\\s*\"(WHITE|BLACK)\"").matcher(json);
        if (mPlayer.find()) {
            currentPlayer = mPlayer.group(1).equals("WHITE") ? playerWhite : playerBlack;
        }

        Matcher mMoves = Pattern.compile("\"movesWithoutCapture\"\\s*:\\s*(\\d+)").matcher(json);
        if (mMoves.find()) {
            movesWithoutCapture = Integer.parseInt(mMoves.group(1));
        }

        Matcher mBoard = Pattern.compile("\"board\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (mBoard.find()) {
            String boardData = mBoard.group(1).replaceAll("\"", "").replaceAll("\\s+", "");
            String[] squares = boardData.split(",");
            int idx = 0;
            for (int r = 0; r < Board.SIZE; r++) {
                for (int c = 0; c < Board.SIZE; c++) {
                    if (idx < squares.length) {
                        String data = squares[idx++];
                        if (data.equals("0")) {
                            board.setPiece(r, c, null);
                        } else {
                            Piece.PieceType type = data.startsWith("W") ? Piece.PieceType.WHITE : Piece.PieceType.BLACK;
                            Piece p = new Piece(type);
                            if (data.endsWith("K")) p.makeKing();
                            board.setPiece(r, c, p);
                        }
                    }
                }
            }
        }
        positionHistory.clear();
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == playerWhite) ? playerBlack : playerWhite;
        isMultiCapturing = false;
    }

    public boolean isAvailableMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (toRow < 0 || toRow >= Board.SIZE || toCol < 0 || toCol >= Board.SIZE) return false;
        if (isMultiCapturing) {
            if (fromRow != multiCaptureRow || fromCol != multiCaptureCol) return false;
        }
        Piece piece = board.getPiece(fromRow,fromCol);
        if (piece == null) return false;
        if(board.getPiece(toRow,toCol) != null) return false;
        int dx = Math.abs(toCol - fromCol);
        int dy = Math.abs(toRow - fromRow);
        if (dx != dy) return false;
        if (piece.isKing()) return isAvailableKingMove(fromRow, fromCol, toRow, toCol, piece);
        else return isAvailableRegularMove(fromRow, fromCol, toRow, toCol, piece);
    }

    private boolean isAvailableRegularMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        int dx = Math.abs(toCol - fromCol);
        int rowDiff = toRow - fromRow;
        int direction = (piece.getType() == Piece.PieceType.WHITE) ? -1 : 1;
        if (dx == 1) {
            if (rowDiff == direction) {
                if (isMultiCapturing) return false;
                if (getPlayerMaxCaptures(currentPlayer) > 0) return false;
                return true;
            }
            return false;
        }
        if (dx == 2 && Math.abs(rowDiff) == 2) {
            int jumpedRow = (fromRow + toRow) / 2;
            int jumpedCol = (fromCol + toCol) / 2;
            Piece jumpedPiece = board.getPiece(jumpedRow,jumpedCol);
            if (jumpedPiece != null && jumpedPiece.getType() != piece.getType()) {
                int maxPossible = getPlayerMaxCaptures(currentPlayer);
                boolean[][] simulatedCaptures = new boolean[Board.SIZE][Board.SIZE];
                simulatedCaptures[jumpedRow][jumpedCol] = true;
                if (1 + getPieceMaxCaptures(toRow, toCol, piece, simulatedCaptures) < maxPossible) return false;
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
            Piece p = board.getPiece(currentRow,currentCol);
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
                if (board.getPiece(jRow,jCol) != null )break;
                jRow += rowDir;
                jCol += colDir;
            }
            int maxPossible = getPlayerMaxCaptures(currentPlayer);
            boolean[][] simulatedCaptures = new boolean[Board.SIZE][Board.SIZE];
            simulatedCaptures[jRow][jCol] = true;
            if (1 + getPieceMaxCaptures(toRow, toCol, piece, simulatedCaptures) < maxPossible) return false;
            return true;
        } else {
            if (isMultiCapturing) return false;
            if (getPlayerMaxCaptures(currentPlayer) > 0) return false;
            return true;
        }
    }

    public int getPlayerMaxCaptures(Player player) {
        int maxCapture = 0;
        for(int r = 0; r < Board.SIZE; r++){
            for(int c = 0; c < Board.SIZE; c++){
                Piece p = board.getPiece(r,c);
                if (p != null && p.getType() == player.getColor()) {
                    if (isMultiCapturing && (r != multiCaptureRow || c != multiCaptureCol)) continue;
                    int captures = getPieceMaxCaptures(r, c, p, new boolean[Board.SIZE][Board.SIZE]);
                    if (captures > maxCapture) maxCapture = captures;
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
                        Piece p = board.getPiece(currentRow,currentCol);
                        if (p != null) {
                            if (p.getType() == piece.getType() || foundEnemy || captured[currentRow][currentCol]) break;
                            foundEnemy = true;
                            enemyRow = currentRow;
                            enemyCol = currentCol;
                        } else if (foundEnemy) {
                            captured[enemyRow][enemyCol] = true;
                            int captureCount = 1 + getPieceMaxCaptures(currentRow, currentCol, piece, captured);
                            if (captureCount > max) max = captureCount;
                            captured[enemyRow][enemyCol] = false;
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
                        Piece p = board.getPiece(jumpedRow,jumpedCol);
                        if (p != null && p.getType() != piece.getType() && !captured[jumpedRow][jumpedCol]) {
                            if (board.getPiece(targetRow, targetCol) == null) {
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

    private int[] getCapturedCoords(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDir = (toRow - fromRow) > 0 ? 1 : -1;
        int colDir = (toCol - fromCol) > 0 ? 1 : -1;
        int currentRow = fromRow + rowDir;
        int currentCol = fromCol + colDir;
        while (currentRow != toRow && currentCol != toCol) {
            if (board.getPiece(currentRow, currentCol) != null) return new int[]{currentRow, currentCol};
            currentRow += rowDir;
            currentCol += colDir;
        }
        return null;
    }

    public boolean performMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isAvailableMove(fromRow, fromCol, toRow, toCol)) {
            Piece piece = board.getPiece(fromRow,fromCol);
            boolean wasCapture = false;
            int [] capturedCords = getCapturedCoords(fromRow, fromCol, toRow, toCol);
            if (capturedCords != null) {
                board.removePiece(capturedCords[0], capturedCords[1]);
                wasCapture = true;
            }
            if(wasCapture){
                movesWithoutCapture = 0;
                positionHistory.clear();
            } else movesWithoutCapture++;
            board.movePiece(fromRow, fromCol, toRow, toCol);
            if (wasCapture && getPieceMaxCaptures(toRow, toCol, piece, new boolean[Board.SIZE][Board.SIZE]) > 0) {
                isMultiCapturing = true;
                multiCaptureRow = toRow;
                multiCaptureCol = toCol;
                return true;
            }
            if(!piece.isKing()){
                if((piece.getType() == Piece.PieceType.WHITE && toRow == 0) || (piece.getType() == Piece.PieceType.BLACK && toRow == Board.SIZE-1)){
                    piece.makeKing();
                    positionHistory.clear();
                }
            }
            switchPlayer();
            String state = getBoardStateString();
            positionHistory.put(state, positionHistory.getOrDefault(state, 0) + 1);
            return true;
        }
        return false;
    }

    public String checkWin() {
        int whitePieces = 0, blackPieces = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null) {
                    if (p.getType() == Piece.PieceType.WHITE) whitePieces++;
                    else blackPieces++;
                }
            }
        }
        if (whitePieces == 0) return "BLACK";
        if (blackPieces == 0) return "WHITE";
        if (!hasAnyValidMoves(currentPlayer)) return currentPlayer.getColor() == Piece.PieceType.WHITE ? "BLACK" : "WHITE";
        if (checkDraw()) return "DRAW";
        return "NONE";
    }

    public boolean checkDraw() {
        return isThreefoldRepetition() || isMovesWithoutCaptureLimitReached() || isMaterialDraw();
    }

    private boolean isThreefoldRepetition() {
        String state = getBoardStateString();
        return positionHistory.getOrDefault(state, 0) >= 3;
    }

    private boolean isMovesWithoutCaptureLimitReached() {
        return movesWithoutCapture >= MAX_MOVES_WITHOUT_CAPTURE;
    }

    private boolean isMaterialDraw() {
        int whiteKings = 0, blackKings = 0, totalPieces = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r,c);
                if (p != null) {
                    totalPieces++;
                    if (p.isKing()) {
                        if (p.getType() == Piece.PieceType.WHITE) whiteKings++;
                        else blackKings++;
                    }
                }
            }
        }
        return totalPieces == 2 && whiteKings == 1 && blackKings == 1;
    }

    private String getBoardStateString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) sb.append("-");
                else {
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
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = board.getPiece(row,col);
                if (piece != null && piece.getType() == player.getColor()) {
                    if (piece.isKing()) {
                        int[] dRows = {-1, 1}, dCols = {-1, 1};
                        for (int dr : dRows) {
                            for (int dc : dCols) {
                                int tRow = row + dr, tCol = col + dc;
                                if (tRow >= 0 && tRow < Board.SIZE && tCol >= 0 && tCol < Board.SIZE) {
                                    if (board.getPiece(tRow, tCol) == null) return true;
                                }
                            }
                        }
                    } else {
                        int direction = (piece.getType() == Piece.PieceType.WHITE) ? -1 : 1;
                        int[] dCols = {-1, 1};
                        for (int dc : dCols) {
                            int tRow = row + direction, tCol = col + dc;
                            if (tRow >= 0 && tRow < Board.SIZE && tCol >= 0 && tCol < Board.SIZE) {
                                if (board.getPiece(tRow, tCol) == null) return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isSelectable(int row, int col) {
        Piece piece = board.getPiece(row, col);
        return piece != null && piece.getType() == currentPlayer.getColor();
    }
    public void resetGame() {
        // Tworzymy nową planszę z pionkami na pozycjach startowych
        Board newBoard = new Board();
        // Kopiujemy pola nowej planszy do obecnej
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                board.setPiece(r, c, newBoard.getPiece(r, c));
            }
        }
        this.currentPlayer      = playerWhite;
        this.isMultiCapturing   = false;
        this.multiCaptureRow    = -1;
        this.multiCaptureCol    = -1;
        this.movesWithoutCapture = 0;
        this.positionHistory.clear();
    }
}