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
    private String password;
    private PrintWriter out;
    private BufferedReader in;

    public MainMenuView(Stage stage, String username, String password) {
        this.stage = stage;
        this.username = username;
        this.password = password;
    }
    public MainMenuView(Stage stage, String username, PrintWriter out, BufferedReader in){
        this.stage = stage;
        this.username = username;
        this.out = out;
        this.in = in;
    }

    public void show() {
        StackPane root = new StackPane();
        try {
            String imagePath = getClass().getResource("/com/example/checkers/pieces/background.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + imagePath + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center;");
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #4b2e1e;");
        }

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);
        menuBox.setPadding(new Insets(30));

        Label titleLabel = new Label("WARCABY");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 20 0;");

        // NOWY PRZYCISK: Ustawienia
        Button settingsBtn = new Button("USTAWIENIA");
        styleGreenButton(settingsBtn);

        Button singlePlayerBtn = new Button("GRA Z KOMPUTEREM");
        styleGreenButton(singlePlayerBtn);

        Button multiPlayerBtn = new Button("GRA WIELOOSOBOWA");
        styleGreenButton(multiPlayerBtn);

        Button backBtn = new Button("WYLOGUJ");
        styleSecondaryButton(backBtn);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-text-alignment: center;");
        statusLabel.setWrapText(true);

        // LOGIKA NOWEGO PRZYCISKU
        settingsBtn.setOnAction(e -> {
            new SettingsView(stage, username, password, out, in).show();
        });

        singlePlayerBtn.setOnAction(e -> {
            SinglePlayerMenuView singlePlayerMenu = new SinglePlayerMenuView(stage, username, password);
            singlePlayerMenu.show();
        });

        multiPlayerBtn.setOnAction(e -> {
            statusLabel.setText("Łączenie z serwerem...");

            new Thread(() -> {
                try {
                    Socket socket = new Socket("127.0.0.1", 12345);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("LOGIN " + username + " " + password);
                    String response = in.readLine();

                    if ("LOGIN_SUCCESS".equals(response)) {
                        Platform.runLater(() -> {
                            LobbyView lobby = new LobbyView(stage, username, out, in);
                            lobby.show();
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("Błąd logowania!"));
                    }
                } catch (IOException ex) {
                    Platform.runLater(() -> statusLabel.setText("Nie znaleziono serwera!"));
                }
            }).start();
        });

        backBtn.setOnAction(e -> {
            new LoginView(stage).show();
        });

        // DODANO settingsBtn DO WIDOKU
        menuBox.getChildren().addAll(titleLabel, settingsBtn, singlePlayerBtn, multiPlayerBtn, backBtn, statusLabel);
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
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #388e3c;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: #388e3c;", "")));
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