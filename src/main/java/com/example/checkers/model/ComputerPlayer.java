package com.example.checkers.model;

import java.util.*;

public class ComputerPlayer {
    public enum Difficulty { EASY, MEDIUM, HARD }

    private final GameManager gameManager;
    private final Board board;
    private final Piece.PieceType aiColor;
    private final Difficulty difficulty;
    private final Random random = new Random();

    public ComputerPlayer(GameManager gameManager, Board board, Piece.PieceType aiColor, Difficulty difficulty) {
        this.gameManager = gameManager;
        this.board = board;
        this.aiColor = aiColor;
        this.difficulty = difficulty;
    }

    public int[] makeMoveAndGetCoords() {
        List<int[]> moves = getAllLegalMoves(board, aiColor);
        if (moves.isEmpty()) return null;

        int[] selectedMove;
        switch (difficulty) {
            case EASY:
                selectedMove = getEasyMove(moves);
                break;
            case MEDIUM:
                selectedMove = minimaxSearch(board, 4);
                break;
            case HARD:
            default:
                selectedMove = minimaxSearch(board, 6);
                break;
        }

        if (selectedMove != null) {
            gameManager.performMove(selectedMove[0], selectedMove[1], selectedMove[2], selectedMove[3]);
        }
        return selectedMove;
    }

    private int[] getEasyMove(List<int[]> moves) {
        if (random.nextDouble() < 0.3) return moves.get(random.nextInt(moves.size()));
        return minimaxSearch(board, 2);
    }

    private int[] minimaxSearch(Board currentBoard, int depth) {
        List<int[]> moves = getAllLegalMoves(currentBoard, aiColor);
        int[] bestMove = null;
        double bestVal = Double.NEGATIVE_INFINITY;

        moves.sort((a, b) -> Integer.compare(Math.abs(b[2] - b[0]), Math.abs(a[2] - a[0])));

        for (int[] move : moves) {
            Board tempBoard = currentBoard.copy();
            simulateMove(tempBoard, move);
            double val = minimax(tempBoard, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
            if (val > bestVal) {
                bestVal = val;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private double minimax(Board b, int depth, double alpha, double beta, boolean maxTurn) {
        if (depth == 0) return evaluate(b);

        Piece.PieceType color = maxTurn ? aiColor : (aiColor == Piece.PieceType.WHITE ? Piece.PieceType.BLACK : Piece.PieceType.WHITE);
        List<int[]> moves = getAllLegalMoves(b, color);

        if (moves.isEmpty()) return maxTurn ? -1000 : 1000;

        if (maxTurn) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (int[] m : moves) {
                Board next = b.copy();
                simulateMove(next, m);
                double eval = minimax(next, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (int[] m : moves) {
                Board next = b.copy();
                simulateMove(next, m);
                double eval = minimax(next, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private double evaluate(Board b) {
        double score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = b.getPiece(r, c);
                if (p == null) continue;

                double val = p.isKing() ? 10.0 : 3.0;

                if (!p.isKing()) {
                    int dist = (p.getType() == Piece.PieceType.WHITE) ? (7 - r) : r;
                    val += dist * 0.5;
                }

                if (c == 0 || c == 7) val += 1.0;
                if (r >= 3 && r <= 4 && c >= 3 && c <= 4) val += 0.5;

                score += (p.getType() == aiColor) ? val : -val;
            }
        }
        return score;
    }

    private List<int[]> getAllLegalMoves(Board b, Piece.PieceType color) {
        List<int[]> normal = new ArrayList<>();
        List<int[]> captures = new ArrayList<>();
        GameManager tempGm = new GameManager(b);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = b.getPiece(r, c);
                if (p != null && p.getType() == color) {
                    for (int tr = 0; tr < 8; tr++) {
                        for (int tc = 0; tc < 8; tc++) {
                            if (tempGm.isAvailableMove(r, c, tr, tc)) {
                                int[] move = {r, c, tr, tc};
                                if (Math.abs(tr - r) > 1) captures.add(move);
                                else normal.add(move);
                            }
                        }
                    }
                }
            }
        }
        return captures.isEmpty() ? normal : captures;
    }

    private void simulateMove(Board b, int[] m) {
        Piece p = b.getPiece(m[0], m[1]);
        b.setPiece(m[2], m[3], p);
        b.setPiece(m[0], m[1], null);
        if (Math.abs(m[2] - m[0]) > 1) {
            b.setPiece((m[0] + m[2]) / 2, (m[1] + m[3]) / 2, null);
        }
        if ((p.getType() == Piece.PieceType.WHITE && m[2] == 0) || (p.getType() == Piece.PieceType.BLACK && m[2] == 7)) {
            p.setKing(true);
        }
    }
}