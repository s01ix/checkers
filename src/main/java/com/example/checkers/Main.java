package com.example.checkers;

import com.example.checkers.controller.Move;
import com.example.checkers.model.Board;
import com.example.checkers.model.GameManager;
import com.example.checkers.network.NetworkClient;
import com.example.checkers.view.BoardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Te trzy linie są wspólne dla obu trybów
        Board boardModel = new Board();
        GameManager gameManager = new GameManager(boardModel);
        BoardView boardView = new BoardView(boardModel);

        //Tryb sieciowy
        NetworkClient networkClient = new NetworkClient(12345, gameManager, boardView);
        new Move(gameManager, boardView, networkClient);
        primaryStage.setTitle("Warcaby Sieciowe - " + (networkClient.getMyColor() == null ? "Łączenie..." : networkClient.getMyColor()));


        //Tryb lokalny
        //new Move(gameManager, boardView, null);
        //primaryStage.setTitle("Warcaby Lokalne");


        Scene scene = new Scene(boardView.getGridPane());
        primaryStage.setScene(scene);
        // Zamknięcię wszystki wątków po zakończeniu gry online
        primaryStage.setOnCloseRequest(e -> System.exit(0));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}