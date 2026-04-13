package com.example.checkers.view;

import com.example.checkers.model.Board;
import com.example.checkers.model.GameManager;
import com.example.checkers.controller.Move;
import com.example.checkers.network.NetworkClient;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class LobbyView {
    private final Stage stage;
    private final String username;
    private final String password;
    private final PrintWriter out;
    private final BufferedReader in;

    public LobbyView(Stage stage, String username, String password,
                     PrintWriter out, BufferedReader in) {
        this.stage    = stage;
        this.username = username;
        this.password = password;
        this.out      = out;
        this.in       = in;
    }

    public void show() {
        StackPane root = new StackPane();
        String imagePath = getClass()
                .getResource("/com/example/checkers/pieces/background.png")
                .toExternalForm();
        root.setStyle(
                "-fx-background-image: url('" + imagePath + "'); " +
                        "-fx-background-size: cover;"
        );

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.setMaxWidth(600);

        Label title = new Label("LOBBY GIER");
        title.setStyle(
                "-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;"
        );

        ListView<String> roomList = new ListView<>();
        roomList.setPrefHeight(300);
        roomList.setStyle("-fx-background-radius: 10;");

        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);

        Button createBtn  = new Button("STWÓRZ POKÓJ");
        Button joinBtn    = new Button("DOŁĄCZ");
        Button refreshBtn = new Button("ODŚWIEŻ");
        Button backBtn    = new Button("POWRÓT");

        styleGreenButton(createBtn);
        styleGreenButton(joinBtn);
        styleGreenButton(refreshBtn);
        styleGreenButton(backBtn);

        controls.getChildren().addAll(createBtn, joinBtn, refreshBtn, backBtn);

        backBtn.setOnAction(e -> {
            if (out != null) {
                out.println("LEAVE_LOBBY");
            }
            new MainMenuView(stage, username, password).show();
        });

        createBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    out.println("CREATE_ROOM");

                    String roomCreatedMsg = in.readLine();
                    System.out.println("Otrzymano z serwera: " + roomCreatedMsg);

                    Platform.runLater(() -> {
                        title.setText("OCZEKIWANIE...");
                        createBtn.setDisable(true);
                    });

                    String response = in.readLine();
                    System.out.println("Otrzymano start gry: " + response);

                    if (response != null && response.equals("CONNECTED WHITE")) {
                        launchGame("WHITE");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        refreshBtn.setOnAction(e -> {
            new Thread(() -> {
                out.println("GET_ROOMS");
                try {
                    String response = in.readLine();
                    if (response != null && response.startsWith("ROOM_LIST ")) {
                        String data = response.substring(10);
                        String[] rooms = data.split(";");

                        Platform.runLater(() -> {
                            roomList.getItems().clear();
                            for (String r : rooms) {
                                if (!r.isEmpty()) roomList.getItems().add(r);
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        joinBtn.setOnAction(e -> {
            String selected = roomList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new Thread(() -> {
                    try {
                        String[] parts = selected.split(":");
                        String roomId = parts[0].trim();

                        out.println("JOIN_ROOM " + roomId);
                        System.out.println("Wysłano prośbę o dołączenie: " + roomId);

                        String response = in.readLine();
                        if (response != null && response.equals("CONNECTED BLACK")) {
                            System.out.println("Serwer zaakceptował. Start gry!");
                            launchGame("BLACK");
                        } else {
                            System.out.println("Błąd dołączania: " + response);
                        }
                    } catch (Exception ex) {
                        System.err.println("Błąd dołączania: " + ex.getMessage());
                    }
                }).start();
            }
        });

        layout.getChildren().addAll(title, roomList, controls);
        root.getChildren().add(layout);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }
    }

    private void styleGreenButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: #2e7d32; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
        );
        btn.setPrefSize(150, 40);
    }

    private void launchGame(String color) {
        Platform.runLater(() -> {
            Board boardModel     = new Board();
            GameManager gameManager = new GameManager(boardModel);
            BoardView boardView  = new BoardView(boardModel);

            if ("BLACK".equalsIgnoreCase(color)) {
                boardView.flipBoard();
            }

            NetworkClient networkClient = new NetworkClient(
                    out, in, gameManager, boardView, username, color
            );

            boardView.setSurrenderAction(() -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Poddanie");
                confirm.setHeaderText("Czy na pewno chcesz się poddać?");

                ButtonType tak = new ButtonType("Tak, poddaję się", ButtonBar.ButtonData.YES);
                ButtonType nie = new ButtonType("Nie, gram dalej",  ButtonBar.ButtonData.NO);
                confirm.getButtonTypes().setAll(tak, nie);

                confirm.showAndWait().ifPresent(response -> {
                    if (response == tak) {
                        boardView.disableBoard();
                        networkClient.sendSurrender();
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Poddano partię");
                        info.setHeaderText("Poddałeś partię");
                        info.setContentText(
                                "Możesz teraz zapisać grę, zagrać ponownie lub opuścić."
                        );
                        info.showAndWait();
                    }
                });
            });

            boardView.setDrawAction(() ->
                    networkClient.sendDrawRequest()
            );

            boardView.setRematchAction(() ->
                    networkClient.sendRematchRequest()
            );

            boardView.setLeaveAction(() -> {
                networkClient.sendLeave();
                // Nowe połączenie nawiąże MainMenuView przy "GRA WIELOOSOBOWA"
                new MainMenuView(stage, username, password).show();
            });

            new Move(gameManager, boardView, networkClient);

            if (stage.getScene() == null) {
                stage.setScene(new Scene(boardView.getRootContainer(), 800, 800));
            } else {
                stage.getScene().setRoot(boardView.getRootContainer());
            }

            stage.setTitle("Warcaby - Grasz jako " + color);
        });
    }
}