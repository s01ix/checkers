package com.example.checkers.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class MainMenuView {
    private final Stage stage;
    private final String username;
    private final String password;
    private final PrintWriter out;    // Dodane pole
    private final BufferedReader in;

    public MainMenuView(Stage stage, String username, String password,PrintWriter out, BufferedReader in) {
        this.stage    = stage;
        this.username = username;
        this.password = password;
        this.out = out;
        this.in = in;
    }

    public void show() {
        StackPane root = new StackPane();
        try {
            String imagePath = getClass()
                    .getResource("/com/example/checkers/pieces/background.png")
                    .toExternalForm();
            root.setStyle(
                    "-fx-background-image: url('" + imagePath + "'); " +
                            "-fx-background-size: cover; " +
                            "-fx-background-position: center;"
            );
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #4b2e1e;");
        }

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);
        menuBox.setPadding(new Insets(30));

        Label titleLabel = new Label("WARCABY");
        titleLabel.setStyle(
                "-fx-font-size: 48px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white; -fx-padding: 0 0 20 0;"
        );

        Button settingsBtn    = new Button("USTAWIENIA");
        Button singlePlayerBtn = new Button("GRA Z KOMPUTEREM");
        Button multiPlayerBtn  = new Button("GRA WIELOOSOBOWA");
        Button backBtn         = new Button("WYLOGUJ");

        styleGreenButton(settingsBtn);
        styleGreenButton(singlePlayerBtn);
        styleGreenButton(multiPlayerBtn);
        styleSecondaryButton(backBtn);

        Label statusLabel = new Label("");
        statusLabel.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-text-alignment: center;"
        );
        statusLabel.setWrapText(true);

        settingsBtn.setOnAction(e ->
                new SettingsView(stage, username, password, out, in).show()
        );

        singlePlayerBtn.setOnAction(e ->
                new SinglePlayerMenuView(stage, username, password, out, in).show()
        );

        multiPlayerBtn.setOnAction(e -> {
          //  statusLabel.setText("Łączenie z serwerem...");
           // multiPlayerBtn.setDisable(true);

            //new Thread(() -> {
              //  try {
//                    Socket socket = new Socket("127.0.0.1", 12345);
//                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                    BufferedReader in = new BufferedReader(
//                            new InputStreamReader(socket.getInputStream())
//                    );
                    if (this.out != null && this.in != null) {
                        new LobbyView(stage, username, password, out, in).show();
                    } else {
                        statusLabel.setText("Błąd: Utracono połączenie z serwerem!");
                    }
//
//                    out.println("LOGIN " + username + " " + password);
//                    String response = in.readLine();
//
//                    if ("LOGIN_SUCCESS".equals(response)) {
//                        Platform.runLater(() -> {
//                            multiPlayerBtn.setDisable(false);
//                            new LobbyView(stage, username, password, out, in).show();
//                        });
//                    } else {
//                        Platform.runLater(() -> {
//                            multiPlayerBtn.setDisable(false);
//                            statusLabel.setText("Błąd logowania: " + response);
//                        });
//                    }
//                } catch (IOException ex) {
//                    Platform.runLater(() -> {
//                        multiPlayerBtn.setDisable(false);
//                        statusLabel.setText("Nie znaleziono serwera!");
//                    });
//                }
//            }).start();
        });

        backBtn.setOnAction(e -> new LoginView(stage).show());

        menuBox.getChildren().addAll(
                titleLabel, settingsBtn, singlePlayerBtn,
                multiPlayerBtn, backBtn, statusLabel
        );
        root.getChildren().add(menuBox);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setTitle("Warcaby - Menu Główne");
        stage.show();
    }

    private void styleGreenButton(Button btn) {
        btn.setMinWidth(280);
        btn.setStyle(
                "-fx-background-color: #2e7d32; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 18px; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand;"
        );
    }

    private void styleSecondaryButton(Button btn) {
        btn.setMinWidth(150);
        btn.setStyle(
                "-fx-background-color: #1b5e20; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
    }
}