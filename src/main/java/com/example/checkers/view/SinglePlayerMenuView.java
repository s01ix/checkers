package com.example.checkers.view;

import com.example.checkers.controller.MoveSinglePlayer;
import com.example.checkers.model.Board;
import com.example.checkers.model.ComputerPlayer;
import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SinglePlayerMenuView {

    private final Stage stage;
    private final String username;
    private final String password;

    public SinglePlayerMenuView(Stage stage, String username, String password) {
        this.stage = stage;
        this.username = username;
        this.password = password;
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
            root.setStyle("-fx-background-color: #4b2e1e;");
        }

        //Kontener na elementy
        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(500);
        menuBox.setPadding(new Insets(40));

        //Napis
        Label titleLabel = new Label("POZIOM TRUDNOŚCI");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        //Przyciski
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button easyBtn = createStyledMenuButton("ŁATWY", "#2e7d32");
        Button mediumBtn = createStyledMenuButton("ŚREDNI", "#2e7d32");
        Button hardBtn = createStyledMenuButton("TRUDNY", "#2e7d32");

        easyBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.EASY));
        mediumBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.MEDIUM));
        hardBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.HARD));

        buttonBox.getChildren().addAll(easyBtn, mediumBtn, hardBtn);

        // Przycisk powrotu
        Button backBtn = createStyledMenuButton("POWRÓT","#2e7d32");
        styleSecondaryButton(backBtn);
        backBtn.setOnAction(e -> new MainMenuView(stage, username, password).show());

        menuBox.getChildren().addAll(titleLabel, buttonBox, backBtn);
        root.getChildren().add(menuBox);

        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Warcaby - Poziom Trudności");
        stage.setScene(scene);
        stage.show();
    }

    private void startGame(ComputerPlayer.Difficulty difficulty) {
        Board board = new Board();
        GameManager gm = new GameManager(board);
        BoardView view = new BoardView(board);
        ComputerPlayer ai = new ComputerPlayer(gm, board, Piece.PieceType.BLACK, difficulty);
        new MoveSinglePlayer(gm, view, ai);

        stage.setTitle("Warcaby offline - vs Komputer [" + difficulty + "]");
        stage.setScene(new Scene(view.getGridPane()));
    }

    //Metoda stylizująca
    private Button createStyledMenuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(120, 60);
        btn.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 2);"
        );

        //Efekt po najechaniu
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-brightness: 1.2; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-brightness: 1.2; -fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));

        return btn;
    }

    private void styleSecondaryButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: #555555; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-min-width: 100px; " +
                        "-fx-cursor: hand;"
        );
    }
}