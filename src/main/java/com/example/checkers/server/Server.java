package com.example.checkers.server;

import com.example.checkers.server.Room;

import java.io.*;
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

                // REJESTRACJA
                if (line.startsWith("REGISTER ")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String response = registerUser(parts[1], parts[2]);
                        out.println(response);
                    }
                }
                // LOGOWANIE
                else if (line.startsWith("LOGIN ")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String user = parts[1];
                        String pass = parts[2];

                        if (validateLogin(user, pass)) {
                            username = user;
                            out.println("LOGIN_SUCCESS");
                            System.out.println("Zalogowano: " + username);
                        } else {
                            out.println("LOGIN_FAILED Bledny login lub haslo");
                        }
                    }
                }
                // TWORZENIE POKOJU
                else if (line.startsWith("CREATE_ROOM")) {
                    Room newRoom = new Room(username, socket, out);
                    activeRooms.add(newRoom);
                    out.println("ROOM_CREATED " + newRoom.getId());
                    System.out.println("Gracz " + username + " stworzył pokój. Czekam...");
                    // zakomentowane żeby sprawdzić czy działa połączenie laptpa z innym laptopem
//                    while (!newRoom.isFull()) {
//                        Thread.sleep(100);
//                    }
//                    return;
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
    //Metoda do rejestracji
    private static synchronized String registerUser(String username, String password) {
        try {
            File file = new File("users.txt");
            if (!file.exists()) file.createNewFile();

            // Sprawdzamy czy login już istnieje
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(":");
                    if (parts[0].equalsIgnoreCase(username)) {
                        return "REGISTER_FAILED Login zajety";
                    }
                }
            }

            // Jeśli nie istnieje, dopisujemy na końcu pliku
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                pw.println(username + ":" + password);
            }
            return "REGISTER_SUCCESS";
        } catch (IOException e) {
            return "REGISTER_FAILED Blad serwera";
        }
    }
    //Metoda do logowania
    private static synchronized boolean validateLogin(String username, String password) {
        File file = new File("users.txt");
        if (!file.exists()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
