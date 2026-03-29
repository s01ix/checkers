package com.example.checkers.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public void makeMove() {
        List<int[]> validMoves = getAllValidMoves();
        if (validMoves.isEmpty()) return;

        int[] selectedMove;
        if (difficulty == Difficulty.EASY) {
            selectedMove = validMoves.get(random.nextInt(validMoves.size()));
        } else {
            selectedMove = getBestMove(validMoves);
        }

        gameManager.performMove(selectedMove[0], selectedMove[1], selectedMove[2], selectedMove[3]);
    }

    private List<int[]> getAllValidMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getType() == aiColor) {
                    for (int tr = 0; tr < Board.SIZE; tr++) {
                        for (int tc = 0; tc < Board.SIZE; tc++) {
                            if (gameManager.isAvailableMove(r, c, tr, tc)) {
                                moves.add(new int[]{r, c, tr, tc});
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private int[] getBestMove(List<int[]> moves) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = moves.get(0);
        for (int[] move : moves) {
            int score = evaluateMove(move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int evaluateMove(int[] move) {
        int score = 0;
        if (Math.abs(move[0] - move[2]) > 1) score += 100; // Bicie
        if (move[2] == 0 || move[2] == 7) score += 50; // Promocja na damkę
        return score;
    }
}