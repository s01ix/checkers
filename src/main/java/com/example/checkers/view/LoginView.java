package com.example.checkers.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LoginView {

    private final Stage stage;
    private PrintWriter out;
    private BufferedReader in;

    public LoginView(Stage stage, PrintWriter out, BufferedReader in) {
        this.stage = stage;
        this.out = out;
        this.in = in;
    }
    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        StackPane root = new StackPane();
        try {
            String imagePath = getClass().getResource("/com/example/checkers/pieces/background.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + imagePath + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center;");
        } catch (Exception e) {
            System.err.println("Nie udało się załadować tła: " + e.getMessage());
            root.setStyle("-fx-background-color: #4b2e1e;");
        }

        VBox menuBox = new VBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(350);
        menuBox.setPadding(new Insets(20));

        Label titleLabel = new Label("LOGOWANIE");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 10 0;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Wpisz swój nick");
        styleInput(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Wpisz hasło");
        styleInput(passwordField);

        Button loginButton = new Button("ZALOGUJ");
        styleGreenButton(loginButton);

        Button registerButton = new Button("ZAREJESTRUJ");
        styleGreenButton(registerButton);

        // NOWY PRZYCISK: Autorzy
        Button authorsButton = new Button("AUTORZY");
        styleSecondaryButton(authorsButton);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #FF3333; " + "-fx-font-weight: bold; " + "-fx-font-size: 14px; ");

        registerButton.setOnAction(e -> {
            if (this.out == null) {
                connectToServer();
            }
            if (this.out != null) {
                new RegisterView(stage, out, in).show();
            } else {
                statusLabel.setText("Błąd: Serwer jest nieosiągalny!");
            }
        });

        // LOGIKA NOWEGO PRZYCISKU
        authorsButton.setOnAction(e -> {
            new AuthorsView(stage, out, in).show();
        });

//        loginButton.setOnAction(e -> {
//            String username = usernameField.getText();
//            String password = passwordField.getText();
//            if (username.trim().isEmpty() || password.trim().isEmpty()) {
//                statusLabel.setText("Błąd: Nick i hasło muszą być wypełnione!");
//                return;
//            }
//            loginButton.setDisable(true);
//            new Thread(() -> {
//                List<String> ips = getArpIps();
//                ips.add(0, "192.168.100.19");
//                boolean authSuccess = false;
//                String serverError = "Nie znaleziono serwera.";
//                for (String ip : ips) {
//                    try{
//                        java.net.Socket socket = new java.net.Socket();
//                        socket.connect(new java.net.InetSocketAddress(ip, 12345), 2000);
//
//                        this.out = new java.io.PrintWriter(socket.getOutputStream(), true);
//                        this.in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//
//                        out.println("LOGIN " + username + " " + password);
//                        String response = in.readLine();
//                        if ("LOGIN_SUCCESS".equals(response)) {
//                            authSuccess = true;
//                            break;
//                        } else if (response != null && response.startsWith("LOGIN_FAILED ")) {
//                            serverError = response.substring("LOGIN_FAILED ".length());
//                            break;
//                        }
//                    } catch (Exception ex) { }
//                }
//
//                boolean finalSuccess = authSuccess;
//                String finalError = serverError;
//
//                javafx.application.Platform.runLater(() -> {
//                    if (finalSuccess) {
//                        MainMenuView mainMenu = new MainMenuView(stage, username, password, out, in);
//                        mainMenu.show();
//                    } else {
//                        statusLabel.setText("Błąd: " + finalError);
//                        loginButton.setDisable(false);
//                    }
//                });
//            }).start();
//        });

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.trim().isEmpty() || password.trim().isEmpty()) {
                statusLabel.setText("Błąd: Nick i hasło muszą być wypełnione!");
                return;
            }
            loginButton.setDisable(true);
            new Thread(() -> {
                // Używamy tylko Twojego IP, pętla ARP jest zbędna i spowalnia
                String ip = "172.20.10.4";
                boolean authSuccess = false;
                String serverError = "Nie znaleziono serwera.";

                try {
                    // Rezygnujemy z try-with-resources (brak nawiasów okrągłych),
                    // aby socket nie zamknął się samoczynnie.
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress(ip, 12345), 2000);

                    this.out = new java.io.PrintWriter(socket.getOutputStream(), true);
                    this.in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));

                    this.out.println("LOGIN " + username + " " + password);
                    String response = this.in.readLine();

                    if ("LOGIN_SUCCESS".equals(response)) {
                        authSuccess = true;
                    } else if (response != null && response.startsWith("LOGIN_FAILED ")) {
                        serverError = response.substring("LOGIN_FAILED ".length());
                        socket.close(); // Zamykamy tylko jeśli się NIE udało
                    } else {
                        socket.close();
                    }
                } catch (Exception ex) {
                    serverError = "Błąd połączenia: " + ex.getMessage();
                }

                boolean finalSuccess = authSuccess;
                String finalError = serverError;

                javafx.application.Platform.runLater(() -> {
                    if (finalSuccess) {
                        // Przekazujemy istniejące i OTWARTE strumienie do kolejnego widoku
                        MainMenuView mainMenu = new MainMenuView(stage, username, password, out, in);
                        mainMenu.show();
                    } else {
                        statusLabel.setText("Błąd: " + finalError);
                        loginButton.setDisable(false);
                    }
                });
            }).start();
        });
        // DODANO authorsButton DO WIDOKU
        menuBox.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, registerButton, authorsButton, statusLabel);
        root.getChildren().add(menuBox);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setTitle("Warcaby - Logowanie");
        stage.show();
    }

    private void styleInput(Control field) {
        field.setStyle("-fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 14px;");
    }

    private void styleGreenButton(Button btn) {
        btn.setMinWidth(200);
        btn.setStyle(
                "-fx-background-color: #2e7d32; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #388e3c;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #2e7d32;"));
    }

    // Nowa metoda na styl pobocznego przycisku
    private void styleSecondaryButton(Button btn) {
        btn.setMinWidth(200);
        btn.setStyle(
                "-fx-background-color: #1b5e20; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
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

    private void connectToServer() {
        try {
            //zmiana na własny adres IP
            java.net.Socket socket = new java.net.Socket("172.20.10.4", 12345);
            this.out = new java.io.PrintWriter(socket.getOutputStream(), true);
            this.in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            System.out.println("Połączono z serwerem.");
        } catch (Exception e) {
            System.err.println("Błąd połączenia: " + e.getMessage());
        }
    }
}