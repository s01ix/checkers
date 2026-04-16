package com.example.checkers.view;

import com.example.checkers.model.Board;
import com.example.checkers.model.Piece;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
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
    private boolean isSinglePlayer = false;
    private com.example.checkers.model.GameManager gameManager;

    private final Button surrenderBtn;
    private final Button drawBtn;
    private final Button rematchBtn;
    private final Button leaveBtn;

    public BoardView(Board boardModel) {
        this.boardModel = boardModel;
        this.gridPane = new GridPane();
        this.rootContainer = new HBox(20);
        this.moveLog = new ListView<>();

        this.rootContainer.setStyle("-fx-background-color: #2c1b0e;");
        this.rootContainer.setPadding(new Insets(20));
        this.rootContainer.setAlignment(Pos.CENTER);

        this.gridPane.setMinSize(0, 0);

        StackPane boardWrapper = new StackPane(gridPane);
        boardWrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(boardWrapper, Priority.ALWAYS);

        NumberBinding boardSize = Bindings.min(boardWrapper.widthProperty(), boardWrapper.heightProperty());
        gridPane.maxWidthProperty().bind(boardSize);
        gridPane.maxHeightProperty().bind(boardSize);
        gridPane.prefWidthProperty().bind(boardSize);
        gridPane.prefHeightProperty().bind(boardSize);

        VBox sidePanel = new VBox(10);
        sidePanel.setMinWidth(200);
        sidePanel.prefWidthProperty().bind(rootContainer.widthProperty().multiply(0.3));
        sidePanel.styleProperty().bind(Bindings.concat("-fx-font-size: ", rootContainer.heightProperty().divide(45), "px;"));
        sidePanel.setAlignment(Pos.TOP_CENTER);

        Label logLabel = new Label("HISTORIA RUCHÓW");
        logLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        VBox.setVgrow(moveLog, Priority.ALWAYS);
        moveLog.setStyle("-fx-background-color: #3e2716; -fx-control-inner-background: #3e2716; -fx-text-fill: #f0d9b5; -fx-font-family: 'Courier New';");

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

        saveBtn.setOnAction(e -> saveGame(saveBtn));
        loadBtn.setOnAction(e -> loadGame(loadBtn));
        deleteBtn.setOnAction(e -> deleteGame(deleteBtn));

        sidePanel.getChildren().addAll(logLabel, moveLog, saveBtn, loadBtn, deleteBtn, surrenderBtn, drawBtn, rematchBtn, leaveBtn);
        this.rootContainer.getChildren().addAll(boardWrapper, sidePanel);

        initializeBoardUI();
    }

    public void setGameManager(com.example.checkers.model.GameManager gm) {
        this.gameManager = gm;
    }

    public void setSinglePlayerMode(boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        if (isSinglePlayer) {
            drawBtn.setDisable(true);
        }
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
        } catch (IOException ex) { ex.printStackTrace(); }
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
            } catch (IOException ex) { ex.printStackTrace(); }
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

    private ImageView createPieceImageView(Piece piece) {
        String colorName = (piece.getType() == Piece.PieceType.WHITE) ? "white" : "black";
        String kingSuffix = piece.isKing() ? "_king" : "";
        String path = "/com/example/checkers/pieces/" + colorName + kingSuffix + ".png";

        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            ImageView iv = new ImageView(img);
            iv.fitWidthProperty().bind(gridPane.widthProperty().divide(Board.SIZE).multiply(0.8));
            iv.fitHeightProperty().bind(gridPane.heightProperty().divide(Board.SIZE).multiply(0.8));
            iv.setPreserveRatio(true);

            if (isFlipped) iv.setRotate(180);
            return iv;
        } catch (Exception e) {
            System.err.println("Nie znaleziono grafiki: " + path);
            return null;
        }
    }

    public void updateView() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = boardModel.getPiece(r, c);
                buttons[r][c].setGraphic(p != null ? createPieceImageView(p) : null);
            }
        }
    }

    private void initializeBoardUI() {
        gridPane.getChildren().clear();
        for (int i = 0; i < Board.SIZE; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / Board.SIZE);
            gridPane.getColumnConstraints().add(cc);

            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / Board.SIZE);
            gridPane.getRowConstraints().add(rc);
        }

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Button cell = new Button();
                cell.setMinSize(0, 0);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                String color = ((r + c) % 2 == 0) ? ThemeManager.lightSquareColor : ThemeManager.darkSquareColor;
                cell.setStyle("-fx-background-color: " + color + "; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-color: transparent; -fx-border-width: 3;");
                buttons[r][c] = cell;
                gridPane.add(cell, c, r);
            }
        }
        updateView();
    }

    public void setSurrenderAction(Runnable a) { surrenderBtn.setOnAction(e -> a.run()); }
    public void setDrawAction(Runnable a) { drawBtn.setOnAction(e -> a.run()); }
    public void setRematchAction(Runnable a) { rematchBtn.setOnAction(e -> a.run()); }
    public void setLeaveAction(Runnable a) { leaveBtn.setOnAction(e -> a.run()); }

    public void disableBoard() { gridPane.setDisable(true); surrenderBtn.setDisable(true); drawBtn.setDisable(true); rematchBtn.setDisable(false); }
    public void enableBoard() {
        gridPane.setDisable(false);
        surrenderBtn.setDisable(false);
        if (!isSinglePlayer) {
            drawBtn.setDisable(false);
        }
        rematchBtn.setDisable(true);
    }

    public void clearMoveLog() { moveLog.getItems().clear(); }
    public void flipBoard() { isFlipped = true; gridPane.setRotate(180); updateView(); }

    public HBox getRootContainer() { return rootContainer; }
    public Button[][] getButtons() { return buttons; }

    public void addMoveToLog(int fR, int fC, int tR, int tC, String pName) {
        String entry = String.format("%-8s: %c%d->%c%d", pName, (char)('A'+fC), 8-fR, (char)('A'+tC), 8-tR);
        moveLog.getItems().add(0, entry);
    }

    private void styleControlBtn(Button btn) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
    }
}