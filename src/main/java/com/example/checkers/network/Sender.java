package com.example.checkers.network;

import java.io.PrintWriter;

public class Sender {
    private final PrintWriter out;

    public Sender(PrintWriter out) {
        this.out = out;
    }

    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        out.println("MOVE " + fromRow + " " + fromCol + " " + toRow + " " + toCol);
        out.flush();
    }

    public void sendSurrender() {
        out.println("SURRENDER");
        out.flush();
    }

    public void sendDrawRequest() {
        out.println("DRAW_REQUEST");
        out.flush();
    }

    public void sendDrawAccept() {
        out.println("DRAW_ACCEPT");
        out.flush();
    }

    public void sendDrawDecline() {
        out.println("DRAW_DECLINE");
        out.flush();
    }

    public void sendRematchRequest() {
        out.println("REMATCH_REQUEST");
        out.flush();
    }

    public void sendRematchAccept() {
        out.println("REMATCH_ACCEPT");
        out.flush();
    }

    public void sendRematchDecline() {
        out.println("REMATCH_DECLINE");
        out.flush();
    }

    public void sendLeave() {
        out.println("LEAVE");
        out.flush();
    }
}