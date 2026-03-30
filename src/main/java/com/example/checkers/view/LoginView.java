package com.example.checkers.view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
        VBox root = new VBox();
        Label titleLabel = new Label("Warcaby - Logowanie");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Wpisz swój nick");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Wpisz hasło");
        Button loginButton = new Button("Zaloguj");
        Button registerButton = new Button("Zarejestruj");
        Label statusLabel = new Label("");

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

                    } catch (Exception ex) {
                    }
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

        root.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, registerButton, statusLabel);

        Scene scene = new Scene(root, 300, 300);
        stage.setScene(scene);
        stage.show();
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