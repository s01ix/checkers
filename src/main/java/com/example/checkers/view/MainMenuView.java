package com.example.checkers.view;

import com.example.checkers.controller.Move;
import com.example.checkers.model.Board;
import com.example.checkers.model.GameManager;
import com.example.checkers.network.NetworkClient;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuView {
    private final Stage stage;
    private final String username;
    private final String password;

    public MainMenuView(Stage stage, String username, String password) {
        this.stage = stage;
        this.username = username;
        this.password = password;
    }

    public void show() {
        VBox root = new VBox(20);
        Button singlePlayerBtn = new Button("Gra z komputerem");
        Button multiPlayerBtn = new Button("Gra wieloosobowa");
        Button backBtn = new Button("Wyloguj");

        Label statusLabel = new Label("");

        singlePlayerBtn.setOnAction(e -> {
            SinglePlayerMenuView singlePlayerMenu = new SinglePlayerMenuView(stage, username, password);
            singlePlayerMenu.show();
        });

        multiPlayerBtn.setOnAction(e -> {
            statusLabel.setText("Szukanie serwera i łączenie...");
            multiPlayerBtn.setDisable(true);
            singlePlayerBtn.setDisable(true);

            new Thread(() -> {
                Board boardModel = new Board();
                GameManager gameManager = new GameManager(boardModel);
                BoardView boardView = new BoardView(boardModel);
                NetworkClient networkClient = new NetworkClient(12345, gameManager, boardView, username, password);
                Platform.runLater(() -> {
                    if (networkClient.getMyColor() != null) {
                        new Move(gameManager, boardView, networkClient);
                        Scene scene = new Scene(boardView.getGridPane());
                        stage.setScene(scene);
                        stage.setTitle("Warcaby Sieciowe - " + username + " (" + networkClient.getMyColor() + ")");
                    } else if (networkClient.getErrorMessage() != null) {
                        statusLabel.setText("Odpowedź z serwera: \n" + networkClient.getErrorMessage());
                        multiPlayerBtn.setDisable(false);
                        singlePlayerBtn.setDisable(false);
                    } else {
                        statusLabel.setText("Błąd serwera: Nie znaleziono serwera!");
                        multiPlayerBtn.setDisable(false);
                        singlePlayerBtn.setDisable(false);
                    }
                });
            }).start();
        });

        backBtn.setOnAction(e -> {
            new LoginView(stage).show();
        });

        root.getChildren().addAll(singlePlayerBtn, multiPlayerBtn, backBtn, statusLabel);

        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.setTitle("Warcaby - Menu Główne");
        stage.show();
    }
}