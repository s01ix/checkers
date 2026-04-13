package com.example.checkers.view;

import com.example.checkers.model.Board;
import com.example.checkers.model.Piece;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardView {
    private final Board boardModel;
    private final GridPane gridPane;
    private final HBox rootContainer;
    private final ListView<String> moveLog;
    private final Button[][] buttons = new Button[Board.SIZE][Board.SIZE];
    private boolean isFlipped = false;
    private com.example.checkers.model.GameManager gameManager;

    private Button surrenderBtn, drawBtn, rematchBtn, leaveBtn;

    public BoardView(Board boardModel) {
        this.boardModel = boardModel;
        this.gridPane = new GridPane();
        this.rootContainer = new HBox(20);
        this.moveLog = new ListView<>();

        this.rootContainer.setStyle("-fx-background-color: #2c1b0e;");
        this.rootContainer.setPadding(new Insets(20));
        this.rootContainer.setAlignment(Pos.CENTER);

        StackPane boardWrapper = new StackPane(gridPane);
        HBox.setHgrow(boardWrapper, Priority.ALWAYS);
        gridPane.maxWidthProperty().bind(Bindings.min(boardWrapper.widthProperty(), boardWrapper.heightProperty()));
        gridPane.maxHeightProperty().bind(Bindings.min(boardWrapper.widthProperty(), boardWrapper.heightProperty()));

        VBox sidePanel = new VBox(10);
        sidePanel.setPrefWidth(250);
        sidePanel.setAlignment(Pos.TOP_CENTER);

        Label logLabel = new Label("HISTORIA RUCHÓW");
        logLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        moveLog.setPrefHeight(400);
        moveLog.setStyle("-fx-background-color: #3e2716; -fx-control-inner-background: #3e2716; " +
                "-fx-text-fill: #f0d9b5; -fx-font-family: 'Courier New'; -fx-font-size: 14px;");

        Button saveBtn = new Button("ZAPISZ GRĘ");
        Button loadBtn = new Button("WCZYTAJ GRĘ");
        Button deleteBtn = new Button("USUŃ ZAPIS");

        this.surrenderBtn = new Button("PODDAJ SIĘ");
        this.drawBtn = new Button("REMIS");
        this.rematchBtn = new Button("REWANŻ");
        this.leaveBtn = new Button("OPUŚĆ");

        rematchBtn.setDisable(true);

        styleControlBtn(saveBtn);
        styleControlBtn(loadBtn);
        styleControlBtn(deleteBtn);
        styleControlBtn(surrenderBtn);
        styleControlBtn(drawBtn);
        styleControlBtn(rematchBtn);
        styleControlBtn(leaveBtn);

        surrenderBtn.setOnAction(e -> surrenderGame());

        saveBtn.setOnAction(e -> saveGame(saveBtn));
        loadBtn.setOnAction(e -> loadGame(loadBtn));
        deleteBtn.setOnAction(e -> deleteGame(deleteBtn));

        sidePanel.getChildren().addAll(logLabel, moveLog, saveBtn, loadBtn, deleteBtn, surrenderBtn, drawBtn, rematchBtn, leaveBtn);
        this.rootContainer.getChildren().addAll(boardWrapper, sidePanel);

        initializeBoardUI();
    }

    public void setSurrenderAction(Runnable action) {
        surrenderBtn.setOnAction(e -> action.run());
    }

    public void setDrawAction(Runnable action) {
        drawBtn.setOnAction(e -> action.run());
    }

    public void setRematchAction(Runnable action) {
        rematchBtn.setOnAction(e -> action.run());
    }

    public void setLeaveAction(Runnable action) {
        leaveBtn.setOnAction(e -> action.run());
    }

    private void surrenderGame() {
        System.out.println("[DEBUG] Kliknięto 'PODDAJ SIĘ'");
        System.out.println("[DEBUG] Gra zakończona pomyślnie - wyłączam planszę");
        disableBoard();
    }

    public void setGameManager(com.example.checkers.model.GameManager gm) {
        this.gameManager = gm;
    }

    public String getGameStateAsJson() {
        if (gameManager == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(gameManager.saveGameStateJsonCore()).append(",\n");
        sb.append("  \"log\": [\n");
        for (int i = 0; i < moveLog.getItems().size(); i++) {
            sb.append("    \"").append(moveLog.getItems().get(i)).append("\"");
            if (i < moveLog.getItems().size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    public void loadGameStateFromJson(String json) {
        if (gameManager == null) return;
        gameManager.loadGameStateJsonCore(json);

        moveLog.getItems().clear();
        Matcher mLog = Pattern.compile("\"log\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (mLog.find()) {
            String logData = mLog.group(1);
            Matcher mString = Pattern.compile("\"([^\"]+)\"").matcher(logData);
            while (mString.find()) {
                moveLog.getItems().add(mString.group(1));
            }
        }
        updateView();
    }

    private void saveGame(Button saveBtn) {
        if (gameManager == null) return;
        try (PrintWriter out = new PrintWriter("manual_save.json")) {
            out.print(getGameStateAsJson());

            String originalText = saveBtn.getText();
            saveBtn.setText("ZAPISANO!");
            saveBtn.setStyle("-fx-background-color: #f5f682; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                saveBtn.setText(originalText);
                styleControlBtn(saveBtn);
            });
            pause.play();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadGame(Button loadBtn) {
        if (gameManager == null) return;
        File file = new File("manual_save.json");
        if (file.exists()) {
            try {
                String json = new String(Files.readAllBytes(file.toPath()));
                loadGameStateFromJson(json);

                String originalText = loadBtn.getText();
                loadBtn.setText("WCZYTANO!");
                loadBtn.setStyle("-fx-background-color: #f5f682; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");

                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(e -> {
                    loadBtn.setText(originalText);
                    styleControlBtn(loadBtn);
                });
                pause.play();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteGame(Button deleteBtn) {
        File file = new File("manual_save.json");
        if (file.exists() && file.delete()) {
            String originalText = deleteBtn.getText();
            deleteBtn.setText("USUNIĘTO!");
            deleteBtn.setStyle("-fx-background-color: #f5f682; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                deleteBtn.setText(originalText);
                styleControlBtn(deleteBtn);
            });
            pause.play();
        }
    }

    private void styleControlBtn(Button btn) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    public void addMoveToLog(int fromRow, int fromCol, int toRow, int toCol, String playerName) {
        String from = (char)('A' + fromCol) + "" + (Board.SIZE - fromRow);
        String to = (char)('A' + toCol) + "" + (Board.SIZE - toRow);
        String logEntry = String.format("%-8s: %s -> %s", playerName, from, to);
        moveLog.getItems().add(0, logEntry);
    }

    private ImageView createPieceImageView(Piece piece) {
        String path = "/com/example/checkers/pieces/";
        if (piece.getType() == Piece.PieceType.WHITE) path += piece.isKing() ? "white_king.png" : "white.png";
        else path += piece.isKing() ? "black_king.png" : "black.png";
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            String fb = (piece.getType() == Piece.PieceType.WHITE) ? "white.png" : "black.png";
            stream = getClass().getResourceAsStream("/com/example/checkers/pieces/" + fb);
        }
        if (stream == null) return null;
        ImageView iv = new ImageView(new Image(stream));
        iv.setPreserveRatio(true);
        iv.fitWidthProperty().bind(gridPane.widthProperty().divide(Board.SIZE).multiply(0.75));
        iv.fitHeightProperty().bind(gridPane.heightProperty().divide(Board.SIZE).multiply(0.75));
        if (isFlipped) iv.setRotate(180);
        return iv;
    }

    public void updateView() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece pm = boardModel.getPiece(row, col);
                buttons[row][col].setGraphic(pm != null ? createPieceImageView(pm) : null);
            }
        }
    }

    private void initializeBoardUI() {
        for (int i = 0; i < Board.SIZE; i++) {
            ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(100.0 / Board.SIZE);
            gridPane.getColumnConstraints().add(cc);
            RowConstraints rc = new RowConstraints(); rc.setPercentHeight(100.0 / Board.SIZE);
            gridPane.getRowConstraints().add(rc);
        }
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = new Button();
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                String color = ((row + col) % 2 == 0) ? ThemeManager.lightSquareColor : ThemeManager.darkSquareColor;
                String base = "-fx-background-color: " + color + "; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-color: transparent; -fx-border-width: 3;";
                cell.setStyle(base);
                buttons[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        updateView();
    }

    public HBox getRootContainer() { return rootContainer; }
    public Button[][] getButtons() { return buttons; }
    public void flipBoard() {
        gridPane.setRotate(180);
        this.isFlipped = true;
        updateView();
    }
    public void clearMoveLog() {
        moveLog.getItems().clear();
    }
    public void disableBoard() {
        gridPane.setDisable(true);
        surrenderBtn.setDisable(true);
        drawBtn.setDisable(true);
        rematchBtn.setDisable(false);
    }

    public void enableBoard() {
        gridPane.setDisable(false);
        surrenderBtn.setDisable(false);
        drawBtn.setDisable(false);
        rematchBtn.setDisable(true);
    }
}
