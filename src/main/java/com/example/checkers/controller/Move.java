package com.example.checkers.controller;

import com.example.checkers.model.GameManager;
import com.example.checkers.network.NetworkClient;
import com.example.checkers.view.BoardView;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Move {
    private GameManager gameManager;
    private final BoardView boardView;
    private final NetworkClient networkClient;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public Move(GameManager gameManager, BoardView boardView, NetworkClient networkClient) {
        this.gameManager = gameManager;
        this.boardView = boardView;
        this.networkClient = networkClient;
        setupClickAndDragHandlers();
    }
    private void setupClickAndDragHandlers() {
        Button[][] buttons = boardView.getButtons();
        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                final int r = row;
                final int c = col;
                Button square = buttons[row][col];

                // 1. OBSŁUGA KLIKNIĘĆ (Zwykły wybór)
                square.setOnAction(event -> handleSquareClick(r, c));

                // 2. OBSŁUGA DRAG & DROP (Z Twojej starej klasy Move!)
                square.setOnDragDetected(event -> {
                    // Sprawdzamy w czystym GameManagerze, czy gracz może ruszyć tym pionkiem
                    if (gameManager.isSelectable(r, c)) {
                        Dragboard db = square.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(r + "," + c);
                        db.setContent(content);
                    } else {
                        System.out.println("Nie możesz podnieść tego pionka! Teraz gra: " + gameManager.getCurrentPlayer().getName());
                    }
                    event.consume();
                });

                square.setOnDragOver(event -> {
                    if (event.getGestureSource() != square && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                square.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String[] sourceCoords = db.getString().split(",");
                        int fromRow = Integer.parseInt(sourceCoords[0]);
                        int fromCol = Integer.parseInt(sourceCoords[1]);

                        if (networkClient == null) {
                            // ===== TRYB LOKALNY =====
                            success = gameManager.performMove(fromRow, fromCol, r, c);
                            if (success) {
                                boardView.updateView();
                            }
                        } else {
                            // ===== TRYB SIECIOWY =====
                            if (networkClient.getMyColor() == gameManager.getCurrentPlayer().getColor()) {
                                networkClient.sendMove(fromRow, fromCol, r, c);
                                success = true; // Pozwalamy na upuszczenie, serwer to i tak zweryfikuje
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });
            }
        }
    }

    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1) {
            // --- ETAP 1: WYBÓR PIONKA ---
            if (gameManager.isSelectable(row, col)) {
                // Blokada w trybie sieciowym: nie możesz dotknąć pionka, jeśli to nie Twój kolor
                if (networkClient != null && networkClient.getMyColor() != gameManager.getCurrentPlayer().getColor()) {
                    System.out.println("To nie Twoja tura (Czekasz na: " + gameManager.getCurrentPlayer().getName() + ")");
                    return;
                }

                selectedRow = row;
                selectedCol = col;
                highlightSquare(row, col);
                System.out.println("Zaznaczono: " + row + "," + col);
            }
        } else {
            // --- ETAP 2: PRÓBA RUCHU LUB ZMIANA WYBORU ---

            // 1. Jeśli kliknąłeś ponownie w ten sam pionek -> Odznaczamy
            if (selectedRow == row && selectedCol == col) {
                clearHighlight(selectedRow, selectedCol);
                selectedRow = -1;
                selectedCol = -1;
                return;
            }

            // 2. Jeśli kliknąłeś w INNY swój pionek -> Zmieniamy zaznaczenie na nowy
            if (gameManager.isSelectable(row, col)) {
                clearHighlight(selectedRow, selectedCol);
                selectedRow = row;
                selectedCol = col;
                highlightSquare(row, col);
                return;
            }

            // 3. Jeśli kliknąłeś w puste pole (lub pole przeciwnika) -> Próbujemy wykonać ruch
            if (networkClient == null) {
                // ===== TRYB LOKALNY =====
                boolean moveMade = gameManager.performMove(selectedRow, selectedCol, row, col);
                if (moveMade) {
                    boardView.updateView();
                }
            } else {
                // ===== TRYB SIECIOWY =====
                // Sprawdzamy czy na pewno tura należy do nas
                if (networkClient.getMyColor() == gameManager.getCurrentPlayer().getColor()) {
                    System.out.println("Wysyłam ruch do serwera: " + selectedRow + "," + selectedCol + " -> " + row + "," + col);
                    networkClient.sendMove(selectedRow, selectedCol, row, col);
                    // UWAGA: Nie robimy tu boardView.updateView()!
                    // Czekamy, aż serwer odeśle komendę UPDATE do NetworkClient.
                }
            }

            // Po próbie ruchu (udanej lub nie) czyścimy zaznaczenie
            clearHighlight(selectedRow, selectedCol);
            selectedRow = -1;
            selectedCol = -1;
        }
    }

    private void highlightSquare(int r, int c) {
        boardView.getButtons()[r][c].setStyle("-fx-border-color: yellow; -fx-border-width: 3;");
    }

    private void clearHighlight(int r, int c) {
        // Przywracamy podstawowy styl na podstawie koloru pola
        if ((r + c) % 2 == 0) {
            boardView.getButtons()[r][c].setStyle("-fx-background-color: #f0d9b5; -fx-border-width: 0;");
        } else {
            boardView.getButtons()[r][c].setStyle("-fx-background-color: #b58863; -fx-border-width: 0;");
        }
    }
}