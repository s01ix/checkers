package com.example.checkers;

import com.example.checkers.model.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Board board = new Board();
        GridPane gridPane = board.initBoard();

        int windowSize = Board.SIZE * 50;
        Scene scene = new Scene(gridPane, windowSize, windowSize);
        primaryStage.setTitle("Checkers");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}