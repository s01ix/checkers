package com.example.checkers.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args){
        System.out.println("=====Serwer warcaby startuje=====");

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Serwer nasłuchuje na porcie: " + PORT);
            while (true) {
                System.out.println("\nCzekam na Gracza 1 (Białe)...");
                Socket player1Socket = serverSocket.accept();
                System.out.println("Gracz 1 połączony! (IP: " + player1Socket.getInetAddress() + ")");

                PrintWriter out1 = new PrintWriter(player1Socket.getOutputStream(), true);
                out1.println("CONNECTED WHITE");

                System.out.println("Czekam na Gracza 2 (Czarne)...");
                Socket player2Socket = serverSocket.accept();
                System.out.println("Gracz 2 połączony! (IP: " + player2Socket.getInetAddress() + ")");

                PrintWriter out2 = new PrintWriter(player2Socket.getOutputStream(), true);
                out2.println("CONNECTED BLACK");

                System.out.println("Mamy komplet graczy! Tworzę nową sesję gry...");

                GameSession session = new GameSession(player1Socket, player2Socket);
                session.start();
            }

        } catch (IOException e) {
        System.err.println("Błąd serwera: " + e.getMessage());
        e.printStackTrace();
        }

    }
}
