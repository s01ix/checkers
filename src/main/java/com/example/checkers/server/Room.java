package com.example.checkers.server;

import java.net.Socket;
import java.io.PrintWriter;

public class Room {
    private static int idCounter = 1;
    private int id;
    private String name;
    private Socket player1;
    private volatile Socket player2;
    private PrintWriter hostOut;

    public Room(String name, Socket host, PrintWriter hostOut) {
        this.id = idCounter++;
        this.name = name;
        this.player1 = host;
        this.hostOut = hostOut;
    }

    // Gettery i Settery
    public PrintWriter getHostOut() {return hostOut;}
    public Socket getPlayer1() {return player1;}
    public boolean isFull() { return player2 != null; }
    public void setPlayer2(Socket p2) { this.player2 = p2; }
    public int getId() { return id; }
    public String getName() { return name; }
}