package presentation.view.gui;

import domain.model.game.Game;
import domain.model.game.GameMatchConfig;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import presentation.controller.engine.GameEngine;
import presentation.controller.input.PlayerInput;
import presentation.view.render.GameRenderer;
import presentation.view.util.ViewNavigator;

import java.util.EnumSet;
import java.util.Set;

public final class GamePlayPane extends BorderPane {
    private static final double GAME_WIDTH = Game.WIDTH;
    private static final double GAME_HEIGHT = Game.HEIGHT;

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final GameEngine engine;
    private final GameRenderer renderer = new GameRenderer();
    private final Canvas canvas = new Canvas();
    private final AnimationTimer animationTimer;
    private long previousFrame;

    public GamePlayPane(Game game, GameMatchConfig matchConfig) {
        engine = new GameEngine(game);
        setStyle("-fx-background-color: #edf3f5;");

        StackPane gameArea = new StackPane(canvas);
        gameArea.setStyle("-fx-background-color: #88c9ee;");
        canvas.widthProperty().bind(gameArea.widthProperty());
        canvas.heightProperty().bind(gameArea.heightProperty());
        setCenter(gameArea);

        javafx.scene.control.Button menuButton = new javafx.scene.control.Button("Volver al menú");
        menuButton.setOnAction(event -> {
            stop();
            ViewNavigator.loadView(new MainMenuPane(matchConfig));
        });
        HBox footer = new HBox(
                new javafx.scene.control.Label("J1: A / D mover · W saltar · F patear · G poder"),
                new javafx.scene.control.Label("J2: ← / → mover · ↑ saltar · Enter patear · Shift poder"),
                menuButton);
        footer.setAlignment(Pos.CENTER);
        footer.setSpacing(22);
        footer.setPadding(new Insets(12));
        footer.setStyle("-fx-background-color: #edf3f5; -fx-font-size: 12px;");
        setBottom(footer);

        setFocusTraversable(true);
        setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        gameArea.setOnMouseClicked(event -> requestFocus());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> render());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> render());

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double delta = previousFrame == 0 ? 0 : (now - previousFrame) / 1_000_000_000.0;
                previousFrame = now;
                engine.update(firstPlayerInput(), secondPlayerInput(), delta);
                render();
            }
        };
        animationTimer.start();
    }

    public void stop() {
        animationTimer.stop();
        pressedKeys.clear();
    }

    private void render() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(Color.web("#88c9ee"));
        graphics.fillRect(0, 0, width, height);
        double scale = Math.min(width / GAME_WIDTH, height / GAME_HEIGHT);
        double offsetX = (width - GAME_WIDTH * scale) / 2;
        double offsetY = (height - GAME_HEIGHT * scale) / 2;
        graphics.save();
        graphics.setTransform(scale, 0, 0, scale, offsetX, offsetY);
        renderer.render(graphics, engine.match());
        graphics.restore();
    }

    private PlayerInput firstPlayerInput() {
        return new PlayerInput(pressedKeys.contains(KeyCode.A), pressedKeys.contains(KeyCode.D),
                pressedKeys.contains(KeyCode.W), pressedKeys.contains(KeyCode.F),
                pressedKeys.contains(KeyCode.G));
    }

    private PlayerInput secondPlayerInput() {
        return new PlayerInput(pressedKeys.contains(KeyCode.LEFT), pressedKeys.contains(KeyCode.RIGHT),
                pressedKeys.contains(KeyCode.UP), pressedKeys.contains(KeyCode.ENTER),
                pressedKeys.contains(KeyCode.SHIFT));
    }
}
