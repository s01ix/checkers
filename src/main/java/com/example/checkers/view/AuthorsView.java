package com.example.checkers.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class AuthorsView {
    private final Stage stage;
    private final PrintWriter out;
    private final BufferedReader in;

    public AuthorsView(Stage stage, PrintWriter out, BufferedReader in) {
        this.stage = stage;
        this.out = out;
        this.in = in;
    }

    public void show() {
        StackPane root = new StackPane();
        try {
            String imagePath = getClass().getResource("/com/example/checkers/pieces/background.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + imagePath + "'); -fx-background-size: cover; -fx-background-position: center;");
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #4b2e1e;");
        }

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setMaxWidth(500);

        Label titleLabel = new Label("AUTORZY");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 20 0;");

        Label author1 = new Label("Kamil Korzeniowski");
        Label author2 = new Label("Jakub Żyła");
        Label author3 = new Label("Michał Lesiak");

        String authorStyle = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f0d9b5;";
        author1.setStyle(authorStyle);
        author2.setStyle(authorStyle);
        author3.setStyle(authorStyle);

        Button backBtn = new Button("POWRÓT");
        backBtn.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setPrefSize(200, 40);

        backBtn.setOnAction(e -> new LoginView(stage, out, in).show());

        layout.getChildren().addAll(titleLabel, author1, author2, author3, backBtn);
        root.getChildren().add(layout);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setTitle("Warcaby - Autorzy");
    }
}