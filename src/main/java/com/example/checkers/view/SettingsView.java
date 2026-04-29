package com.example.checkers.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SettingsView {
    private final Stage stage;
    private final String username;
    private final String password;
    private final PrintWriter out;
    private final BufferedReader in;


    private final List<Button> themeButtons = new ArrayList<>();

    public SettingsView(Stage stage, String username, String password, PrintWriter out, BufferedReader in) {
        this.stage = stage;
        this.username = username;
        this.password = password;
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

        VBox menuBox = new VBox(30);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(30));

        Label titleLabel = new Label("WYBIERZ MOTYW");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 10 0;");

        HBox themesBox = new HBox(30);
        themesBox.setAlignment(Pos.CENTER);


        Button theme1 = createThemePreviewButton("#f0d9b5", "#b58863"); // Klasyczny
        Button theme2 = createThemePreviewButton("#cd853f", "#5c3a21"); // Mahoniowy
        Button theme3 = createThemePreviewButton("#c4e0e5", "#4ca1af"); // Morski
        Button theme4 = createThemePreviewButton("#e0e0e0", "#424242"); // Grafitowy


        themeButtons.add(theme1);
        themeButtons.add(theme2);
        themeButtons.add(theme3);
        themeButtons.add(theme4);

        themesBox.getChildren().addAll(theme1, theme2, theme3, theme4);

        // Odświeżamy style na starcie, by obecny motyw od razu miał obwódkę
        updateButtonStyles();

        Button backBtn = new Button("POWRÓT");
        backBtn.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setPrefSize(200, 40);

        backBtn.setOnAction(e -> {
            if (out != null && in != null) {
                new MainMenuView(stage, username, password, out, in).show();
            } else {
                new MainMenuView(stage, username, password, out, in).show();
            }
        });

        menuBox.getChildren().addAll(titleLabel, themesBox, backBtn);
        root.getChildren().add(menuBox);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1000, 600));
        } else {
            stage.getScene().setRoot(root);
        }
        stage.setTitle("Warcaby - Ustawienia");
    }

    private Button createThemePreviewButton(String lightColor, String darkColor) {
        Button btn = new Button();
        GridPane miniBoard = new GridPane();

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                Region square = new Region();
                square.setPrefSize(50, 50);
                String color = ((r + c) % 2 == 0) ? lightColor : darkColor;
                square.setStyle("-fx-background-color: " + color + ";");
                miniBoard.add(square, c, r);
            }
        }

        miniBoard.setStyle("-fx-border-color: white; -fx-border-width: 3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 5);");
        btn.setGraphic(miniBoard);

        btn.getProperties().put("lightColor", lightColor);

        btn.setOnAction(e -> {
            ThemeManager.setTheme(lightColor, darkColor);
            updateButtonStyles();
        });

        btn.setOnMouseEntered(e -> {
            if (!ThemeManager.lightSquareColor.equals(lightColor)) {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10; -fx-border-color: transparent;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!ThemeManager.lightSquareColor.equals(lightColor)) {
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10; -fx-border-color: transparent;");
            }
        });

        return btn;
    }

    private void updateButtonStyles() {
        for (Button btn : themeButtons) {
            String btnLightColor = (String) btn.getProperties().get("lightColor");

            if (ThemeManager.lightSquareColor.equals(btnLightColor)) {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10; -fx-border-color: #f5f682; -fx-border-width: 3; -fx-border-radius: 10;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10; -fx-border-color: transparent;");
            }
        }
    }
}