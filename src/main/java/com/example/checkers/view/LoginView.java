package com.example.checkers.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoginView {

    private final Stage stage;

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        //Główny kontener z tłem
        StackPane root = new StackPane();
        try {
            String imagePath = getClass().getResource("/com/example/checkers/pieces/background.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + imagePath + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center;");
        } catch (Exception e) {
            System.err.println("Nie udało się załadować tła: " + e.getMessage());
            root.setStyle("-fx-background-color: #4b2e1e;"); // Rezerwowy kolor (brązowy)
        }

        //Kontener na elementy
        VBox menuBox = new VBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(350);
        menuBox.setPadding(new Insets(20));
        //Napis
        Label titleLabel = new Label("LOGOWANIE");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 10 0;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Wpisz swój nick");
        styleInput(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Wpisz hasło");
        styleInput(passwordField);

        //Przyciski
        Button loginButton = new Button("ZALOGUJ");
        styleGreenButton(loginButton);

        Button registerButton = new Button("ZAREJESTRUJ");
        styleGreenButton(registerButton);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-weight: bold;");

        //Logika
        registerButton.setOnAction(e -> statusLabel.setText("Funkcja testowa, nie działa narazie"));

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.trim().isEmpty() || password.trim().isEmpty()) {
                statusLabel.setText("Błąd: Nick i hasło muszą być wypełnione!");
                return;
            }
            loginButton.setDisable(true);
            new Thread(() -> {
                List<String> ips = getArpIps();
                ips.add(0, "127.0.0.1");
                boolean authSuccess = false;
                String serverError = "Nie znaleziono serwera.";
                for (String ip : ips) {
                    try (java.net.Socket socket = new java.net.Socket()) {
                        socket.connect(new java.net.InetSocketAddress(ip, 12345), 500);
                        java.io.PrintWriter out = new java.io.PrintWriter(socket.getOutputStream(), true);
                        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                        out.println("LOGIN " + username + " " + password);
                        String response = in.readLine();
                        if ("LOGIN_SUCCESS".equals(response)) {
                            authSuccess = true;
                            break;
                        } else if (response != null && response.startsWith("LOGIN_FAILED ")) {
                            serverError = response.substring("LOGIN_FAILED ".length());
                            break;
                        }
                    } catch (Exception ex) { }
                }

                boolean finalSuccess = authSuccess;
                String finalError = serverError;

                javafx.application.Platform.runLater(() -> {
                    if (finalSuccess) {
                        MainMenuView mainMenu = new MainMenuView(stage, username, password);
                        mainMenu.show();
                    } else {
                        statusLabel.setText("Błąd: " + finalError);
                        loginButton.setDisable(false);
                    }
                });
            }).start();
        });

        // Dodawanie elementów do VBox
        menuBox.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, registerButton, statusLabel);

        // Dodawanie VBox do StackPane
        root.getChildren().add(menuBox);

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Warcaby - Logowanie");
        stage.show();
    }

    //Metody stylizujące
    private void styleInput(Control field) {
        field.setStyle("-fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 14px;");
    }

    private void styleGreenButton(Button btn) {
        btn.setMinWidth(200);
        btn.setStyle(
                "-fx-background-color: #2e7d32; " + // Ciemny zielony
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand;"
        );

        //Efekt po najechaniu
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #388e3c;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #2e7d32;"));
    }

    public static List<String> getArpIps() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                for (String part : parts)
                    if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
                        ipAddresses.add(part);
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Couldnt get arp" + e);
        }
        return ipAddresses;
    }
}