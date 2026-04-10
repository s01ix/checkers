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

import java.io.File;
import java.nio.file.Files;

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
        StackPane root = new StackPane();
        try {
            String imagePath = getClass().getResource("/com/example/checkers/pieces/background.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + imagePath + "'); -fx-background-size: cover;");
        } catch (Exception e) { root.setStyle("-fx-background-color: #4b2e1e;"); }

        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(40));

        Label titleLabel = new Label("GRA Z KOMPUTEREM");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button easyBtn = createStyledMenuButton("ŁATWY");
        Button mediumBtn = createStyledMenuButton("ŚREDNI");
        Button hardBtn = createStyledMenuButton("TRUDNY");

        easyBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.EASY, false));
        mediumBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.MEDIUM, false));
        hardBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.HARD, false));

        buttonBox.getChildren().addAll(easyBtn, mediumBtn, hardBtn);

        VBox mainActions = new VBox(15);
        mainActions.setAlignment(Pos.CENTER);
        mainActions.getChildren().add(buttonBox);

        File saveFile = new File("autosave_single.json");
        if (saveFile.exists()) {
            Button resumeBtn = createStyledMenuButton("WZNÓW OSTATNIĄ GRĘ");
            resumeBtn.setPrefWidth(390);
            resumeBtn.setStyle(resumeBtn.getStyle() + "-fx-background-color: #f5f682; -fx-text-fill: black;");
            resumeBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.MEDIUM, true));

            Button deleteSaveBtn = createStyledMenuButton("USUŃ ZAPISANĄ GRĘ");
            deleteSaveBtn.setPrefWidth(390);
            deleteSaveBtn.setStyle(deleteSaveBtn.getStyle() + "-fx-background-color: #d32f2f; -fx-text-fill: white;");
            deleteSaveBtn.setOnAction(e -> {
                if (saveFile.delete()) {
                    show();
                }
            });

            mainActions.getChildren().addAll(resumeBtn, deleteSaveBtn);
        }

        Button backBtn = createStyledMenuButton("POWRÓT");
        styleSecondaryButton(backBtn);
        backBtn.setOnAction(e -> new MainMenuView(stage, username, password).show());

        menuBox.getChildren().addAll(titleLabel, mainActions, backBtn);
        root.getChildren().add(menuBox);

        if (stage.getScene() == null) stage.setScene(new Scene(root, 1000, 600));
        else stage.getScene().setRoot(root);
    }

    private void startGame(ComputerPlayer.Difficulty difficulty, boolean resume) {
        Board board = new Board();
        GameManager gm = new GameManager(board);
        BoardView view = new BoardView(board);
        ComputerPlayer ai = new ComputerPlayer(gm, board, Piece.PieceType.BLACK, difficulty);
        new MoveSinglePlayer(gm, view, ai);

        if (resume) {
            try {
                String json = new String(Files.readAllBytes(new File("autosave_single.json").toPath()));
                view.loadGameStateFromJson(json);
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        stage.getScene().setRoot(view.getRootContainer());
    }

    private Button createStyledMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(120, 60);
        btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        return btn;
    }

    private void styleSecondaryButton(Button btn) {
        btn.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
    }
}