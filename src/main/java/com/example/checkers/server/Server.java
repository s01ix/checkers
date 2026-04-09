package com.example.checkers.server;

import com.example.checkers.server.Room;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static final java.util.List<Room> activeRooms = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    private static int roomIdCounter = 1;

    public static void main(String[] args){
        System.out.println("=====Serwer warcaby startuje=====");

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
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String username = "Anonim";

            String line;
            // Pętla trzymająca gracza na serwerze
            while ((line = in.readLine()) != null) {
                System.out.println("Odebrano: " + line);

                // LOGOWANIE
                if (line.startsWith("LOGIN ")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3 && parts[1].equals(parts[2])) {
                        username = parts[1];
                        out.println("LOGIN_SUCCESS");
                    } else {
                        out.println("LOGIN_FAILED Hasło niezgodne");
                        return;
                    }
                }

                // TWORZENIE POKOJU
                else if (line.startsWith("CREATE_ROOM")) {
                    Room newRoom = new Room(username, socket, out);
                    activeRooms.add(newRoom);
                    out.println("ROOM_CREATED " + newRoom.getId());
                    System.out.println("Gracz " + username + " stworzył pokój. Czekam...");
                }
                // PRZYGOTOWANIE I WYSŁANIE DANYCH
                else if (line.equals("GET_ROOMS")) {
                    StringBuilder sb = new StringBuilder("ROOM_LIST ");
                    synchronized (activeRooms) {
                        for (Room r : activeRooms) {
                            if (!r.isFull()) {
                                sb.append(r.getId()).append(":").append(r.getName()).append(";");
                            }
                        }
                    }
                    out.println(sb.toString());
                }
                else if (line.startsWith("JOIN_ROOM ")) {
                    int id = Integer.parseInt(line.split(" ")[1]);
                    Room target = null;
                    synchronized (activeRooms) {
                        for (Room r : activeRooms) {
                            if (r.getId() == id && !r.isFull()) {
                                target = r;
                                break;
                            }
                        }
                    }

                    if (target != null) {
                        target.setPlayer2(socket);

                        // POBIERAMY STRUMIEŃ WYJŚCIOWY HOSTA
                        PrintWriter hostOut = new PrintWriter(target.getPlayer1().getOutputStream(), true);

                        // WYSYŁAMY START DO OBU GRACZY JEDNOCZEŚNIE
                        target.getHostOut().println("CONNECTED WHITE");
                        out.println("CONNECTED BLACK");

                        System.out.println("Gra startuje: " + target.getName() + " vs " + username);

                        // START SESJI
                        new GameSession(target.getPlayer1(), socket).start();
                        activeRooms.remove(target);
                        return; // Koniec obsługi lobby dla dołączającego
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Błąd klienta: " + e.getMessage());
        }
    }
}
