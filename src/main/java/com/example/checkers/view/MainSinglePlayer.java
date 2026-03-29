package com.example.checkers;

import com.example.checkers.controller.MoveSinglePlayer;
import com.example.checkers.model.Board;
import com.example.checkers.model.ComputerPlayer;
import com.example.checkers.model.GameManager;
import com.example.checkers.model.Piece;
import com.example.checkers.view.BoardView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainSinglePlayer extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showDifficultyMenu();
    }

    private void showDifficultyMenu() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50; -fx-padding: 30;");

        Label titleLabel = new Label("Wybierz poziom trudności");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button easyBtn = createStyledButton("Łatwy", "#27ae60");
        Button mediumBtn = createStyledButton("Średni", "#f39c12");
        Button hardBtn = createStyledButton("Trudny", "#c0392b");

        easyBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.EASY));
        mediumBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.MEDIUM));
        hardBtn.setOnAction(e -> startGame(ComputerPlayer.Difficulty.HARD));

        buttonBox.getChildren().addAll(easyBtn, mediumBtn, hardBtn);
        root.getChildren().addAll(titleLabel, buttonBox);

        Scene menuScene = new Scene(root, 450, 200);
        primaryStage.setTitle("Warcaby - Menu");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void startGame(ComputerPlayer.Difficulty difficulty) {
        Board board = new Board();
        GameManager gm = new GameManager(board);
        BoardView view = new BoardView(board);
        ComputerPlayer ai = new ComputerPlayer(gm, board, Piece.PieceType.BLACK, difficulty);
        new MoveSinglePlayer(gm, view, ai);

        primaryStage.setTitle("Warcaby offline - vs Komputer [" + difficulty + "]");
        primaryStage.setScene(new Scene(view.getGridPane()));
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(100, 40);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}