package com.example.checkers.network;

import com.example.checkers.model.GameManager;
import com.example.checkers.view.BoardView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.BufferedReader;
import java.io.IOException;

public class Listener implements Runnable {
    private final BufferedReader in;
    private final GameManager localGameManager;
    private final BoardView boardView;
    private final Sender sender;

    public Listener(BufferedReader in, GameManager localGameManager,
                    BoardView boardView, Sender sender) {
        this.in = in;
        this.localGameManager = localGameManager;
        this.boardView = boardView;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;

                if (msg.startsWith("UPDATE")) {
                    String[] parts = msg.split(" ");
                    int r1 = Integer.parseInt(parts[1]);
                    int c1 = Integer.parseInt(parts[2]);
                    int r2 = Integer.parseInt(parts[3]);
                    int c2 = Integer.parseInt(parts[4]);

                    Platform.runLater(() -> {
                        String pName = localGameManager.getCurrentPlayer().getName();
                        localGameManager.performMove(r1, c1, r2, c2);
                        boardView.updateView();
                        boardView.addMoveToLog(r1, c1, r2, c2, pName);
                    });

                } else if (msg.equals("OPPONENT_SURRENDERED")) {
                    Platform.runLater(() -> {
                        boardView.disableBoard();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Koniec Gry");
                        alert.setHeaderText("Przeciwnik się poddał!");
                        alert.setContentText("Wygrałeś! Możesz zagrać ponownie lub opuścić grę.");
                        alert.showAndWait();
                    });

                } else if (msg.equals("DRAW_REQUEST")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Prośba o remis");
                        alert.setHeaderText("Przeciwnik proponuje remis");
                        alert.setContentText("Czy akceptujesz remis?");

                        ButtonType akceptuj = new ButtonType("Akceptuj", ButtonBar.ButtonData.YES);
                        ButtonType odrzuc   = new ButtonType("Odrzuć",   ButtonBar.ButtonData.NO);
                        alert.getButtonTypes().setAll(akceptuj, odrzuc);

                        alert.showAndWait().ifPresent(response -> {
                            if (response == akceptuj) {
                                sender.sendDrawAccept();
                            } else {
                                sender.sendDrawDecline();
                            }
                        });
                    });

                } else if (msg.equals("DRAW_ACCEPTED")) {
                    Platform.runLater(() -> {
                        boardView.disableBoard();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Remis");
                        alert.setHeaderText("Partia zakończona remisem!");
                        alert.setContentText("Możesz zagrać ponownie lub opuścić grę.");
                        alert.showAndWait();
                    });

                } else if (msg.equals("DRAW_DECLINED")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Remis odrzucony");
                        alert.setHeaderText("Przeciwnik odrzucił propozycję remisu");
                        alert.showAndWait();
                    });

                } else if (msg.equals("REMATCH_REQUEST")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Rewanż");
                        alert.setHeaderText("Przeciwnik chce rewanżu");
                        alert.setContentText("Czy chcesz zagrać ponownie?");

                        ButtonType tak = new ButtonType("Tak", ButtonBar.ButtonData.YES);
                        ButtonType nie = new ButtonType("Nie", ButtonBar.ButtonData.NO);
                        alert.getButtonTypes().setAll(tak, nie);

                        alert.showAndWait().ifPresent(response -> {
                            if (response == tak) {
                                sender.sendRematchAccept();
                                localGameManager.resetGame();
                                boardView.clearMoveLog();
                                boardView.enableBoard();
                                boardView.updateView();
                            } else {
                                sender.sendRematchDecline();
                            }
                        });
                    });

                } else if (msg.equals("REMATCH_ACCEPTED")) {
                    Platform.runLater(() -> {
                        localGameManager.resetGame();
                        boardView.clearMoveLog();
                        boardView.enableBoard();
                        boardView.updateView();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Rewanż");
                        alert.setHeaderText("Przeciwnik zaakceptował rewanż!");
                        alert.setContentText("Nowa partia się rozpoczyna. Powodzenia!");
                        alert.showAndWait();
                    });

                } else if (msg.equals("REMATCH_DECLINED")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Rewanż odrzucony");
                        alert.setHeaderText("Przeciwnik nie chce grać ponownie");
                        alert.showAndWait();
                    });

                } else if (msg.equals("OPPONENT_LEFT")) {
                    Platform.runLater(() -> {
                        boardView.disableBoard();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Koniec Gry");
                        alert.setHeaderText("Przeciwnik opuścił grę");
                        alert.setContentText("Możesz zapisać grę lub opuścić.");
                        alert.showAndWait();
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("Utracono połączenie z serwerem.");
        }
    }
}