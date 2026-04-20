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

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setMaxWidth(600);

        Label titleLabel = new Label("Informatyka 3ID11B");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label projectLabel = new Label("System Rozgrywek Warcabowych Online");
        projectLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f5f682; -fx-padding: 0 0 20 0;");

        Label universityLabel = new Label("Politechnika Świętokrzyska");
        universityLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-style: italic;");

        VBox authorsBox = new VBox(5);
        authorsBox.setAlignment(Pos.CENTER);

        String authorStyle = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f0d9b5;";

        Label author1 = new Label("Kamil Korzeniowski");
        author1.setStyle(authorStyle);
        Label author2 = new Label("Jakub Żyła");
        author2.setStyle(authorStyle);
        Label author3 = new Label("Michał Lesiak");
        author3.setStyle(authorStyle);

        authorsBox.getChildren().addAll(author1, author2, author3);

        Label locationLabel = new Label("Kielce, 2026");
        locationLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-padding: 20 0 0 0;");

        Button backBtn = new Button("POWRÓT");
        backBtn.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setPrefSize(200, 40);

        backBtn.setOnAction(e -> new LoginView(stage, out, in).show());

        layout.getChildren().addAll(titleLabel, projectLabel, universityLabel, authorsBox, locationLabel, backBtn);
        root.getChildren().add(layout);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }

        stage.setTitle("Warcaby - Autorzy");
    }
}