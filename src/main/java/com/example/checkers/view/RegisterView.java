package com.example.checkers.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class RegisterView {
    private final Stage stage;
    private final PrintWriter out;
    private final BufferedReader in;

    public RegisterView(Stage stage, PrintWriter out, BufferedReader in) {
        this.stage = stage;
        this.out = out;
        this.in = in;
    }

    public void show() {
        StackPane root = new StackPane();
        String imagePath = getClass().getResource("/com/example/checkers/pieces/background.png").toExternalForm();
        root.setStyle("-fx-background-image: url('" + imagePath + "'); -fx-background-size: cover;");

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.setMaxWidth(400);

        Label title = new Label("REJESTRACJA NOWEGO KONTA");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        TextField loginField = new TextField();
        loginField.setPromptText("Wymyśl login");
        loginField.setStyle("-fx-background-radius: 10; -fx-padding: 10;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Wymyśl hasło");
        passField.setStyle("-fx-background-radius: 10; -fx-padding: 10;");

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Powtórz hasło");
        confirmPassField.setStyle("-fx-background-radius: 10; -fx-padding: 10;");

        Button registerBtn = new Button("ZAREJESTRUJ MNIE");
        styleButton(registerBtn, "#2e7d32");

        Button backBtn = new Button("POWRÓT DO LOGOWANIA");
        styleButton(backBtn, "#d32f2f");

        registerBtn.setOnAction(e -> {
            String user = loginField.getText();
            String pass = passField.getText();
            String confirm = confirmPassField.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                showAlert("Błąd", "Wypełnij wszystkie pola!", Alert.AlertType.ERROR);
                return;
            }
            if (!pass.equals(confirm)) {
                showAlert("Błąd", "Hasła nie są identyczne!", Alert.AlertType.ERROR);
                return;
            }
            if (user.contains(" ") || user.contains(":")) {
                showAlert("Błąd", "Login nie może zawierać spacji ani dwukropka!", Alert.AlertType.ERROR);
                return;
            }

            out.println("REGISTER " + user + " " + pass);

            new Thread(() -> {
                try {
                    String response = in.readLine();
                    if ("REGISTER_SUCCESS".equals(response)) {
                        Platform.runLater(() -> {
                            showAlert("Sukces", "Konto utworzone pomyślnie! Możesz się zalogować.", Alert.AlertType.INFORMATION);
                            new LoginView(stage, out, in).show();
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Błąd", "Serwer odrzucił rejestrację: " + response, Alert.AlertType.ERROR));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        backBtn.setOnAction(e -> new LoginView(stage, out, in).show());

        layout.getChildren().addAll(title, loginField, passField, confirmPassField, registerBtn, backBtn);
        root.getChildren().add(layout);

        // NAPRAWA SKALOWANIA
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setTitle("Warcaby Online - Rejestracja");
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btn.setPrefWidth(250);
        btn.setPrefHeight(40);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}