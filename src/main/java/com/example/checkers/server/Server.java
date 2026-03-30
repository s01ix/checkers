package com.example.checkers.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private static final int PORT = 12345;
    private static final BlockingQueue<Socket> waitingPlayers = new LinkedBlockingQueue<>();

    public static void main(String[] args){
        System.out.println("=====Serwer warcaby startuje=====");

        new Thread(() -> {
            while (true) {
                try {
                    Socket player1 = waitingPlayers.take();
                    PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
                    out1.println("CONNECTED WHITE");

                    Socket player2 = waitingPlayers.take();
                    PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
                    out2.println("CONNECTED BLACK");

                    System.out.println("Mamy komplet graczy! Tworzę nową sesję gry...");
                    GameSession session = new GameSession(player1, player2);
                    session.start();
                } catch (Exception e) {
                    System.err.println("Błąd serwera: " + e.getMessage());
                    e.printStackTrace();}
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Serwer nasłuchuje na porcie: " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClientAuth(socket)).start();
            }

        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClientAuth(Socket socket) {
        try {
            System.out.println("Próba połączenia z IP: " + socket.getInetAddress());
            java.io.BufferedReader in = new java.io.BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String authMsg = in.readLine();
            if (authMsg != null) {
                if (authMsg.startsWith("LOGIN ")) { // Samo sprawdzenie loginu z ekranu LoginView
                    String[] parts = authMsg.split(" ");
                    if (parts.length == 3 && parts[1].equals(parts[2])) {
                        out.println("LOGIN_SUCCESS");
                    } else {
                        out.println("LOGIN_FAILED Hasło musi być takie samo jak login");
                    }
                    socket.close();
                } else if (authMsg.startsWith("JOIN ")) {
                    String[] parts = authMsg.split(" ");
                    if (parts.length == 3 && parts[1].equals(parts[2])) {
                        out.println("LOGIN_SUCCESS");
                        System.out.println("Gracz dołącza do kolejki gry: " + parts[1]);
                        waitingPlayers.put(socket);
                    } else {
                        out.println("LOGIN_FAILED Odrzucono sesje autoryzacji gry");
                        socket.close();
                    }
                } else {
                    out.println("LOGIN_FAILED Nieznana komenda");
                    socket.close();
                }
            } else {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("Błąd klienta: " + e.getMessage());
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
