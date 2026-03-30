package com.example.checkers.network;

import java.io.PrintWriter;

public class Sender {
    private final PrintWriter out;

    public Sender(PrintWriter out) {
        this.out = out;
    }

    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        out.println("MOVE " + fromRow + " " + fromCol + " " + toRow + " " + toCol);
    }
}

