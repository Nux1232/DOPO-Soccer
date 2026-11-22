package presentation.view.gui;

import presentation.input.KeyboardInputHandler;
import presentation.model.GameSnapshot;
import presentation.model.PlayerControls;
import presentation.view.render.GameRenderer;
import presentation.view.effects.PlayerEffectsLayer;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;

/** Gameplay view. It receives immutable presentation snapshots and sends controls upward. */
public final class GamePlayPane extends BorderPane {
    private final KeyboardInputHandler keyboard = new KeyboardInputHandler();

    @FunctionalInterface
    public interface GameUpdateHandler {
        GameSnapshot update(PlayerControls first, PlayerControls second, double deltaSeconds) throws Exception;
    }

    private final GameUpdateHandler onUpdate;
    private final Runnable onBack;
    private final GameRenderer renderer = new GameRenderer();
    private final PlayerEffectsLayer effectsLayer = new PlayerEffectsLayer();
    private final Canvas canvas = new Canvas();
    private final AnimationTimer animationTimer;
    private GameSnapshot snapshot;
    private long previousFrame;

    public GamePlayPane(GameSnapshot initialSnapshot, GameUpdateHandler onUpdate, Runnable onBack) {
        this.snapshot = initialSnapshot;
        this.onUpdate = onUpdate;
        this.onBack = onBack;
        setStyle("-fx-background-color: #edf3f5;");

        StackPane gameArea = new StackPane(canvas, effectsLayer);
        gameArea.setStyle("-fx-background-color: #88c9ee;");
        canvas.widthProperty().bind(gameArea.widthProperty());
        canvas.heightProperty().bind(gameArea.heightProperty());
        setCenter(gameArea);

        javafx.scene.control.Button menuButton = new javafx.scene.control.Button("Volver al menú");
        menuButton.setFocusTraversable(false);
        menuButton.setOnAction(event -> { stop(); onBack.run(); });
        HBox footer = new HBox(
                new javafx.scene.control.Label("J1: A / D mover · W saltar · F patear · G poder"),
                new javafx.scene.control.Label("J2: ← / → mover · ↑ saltar · Enter patear · Shift poder"),
                menuButton);
        footer.setAlignment(Pos.CENTER); footer.setSpacing(22); footer.setPadding(new Insets(12));
        footer.setStyle("-fx-background-color: #edf3f5; -fx-font-size: 12px;");
        setBottom(footer);
        setFocusTraversable(true);
        addEventFilter(KeyEvent.KEY_PRESSED, event -> { keyboard.keyPressed(event); event.consume(); });
        addEventFilter(KeyEvent.KEY_RELEASED, event -> { keyboard.keyReleased(event); event.consume(); });
        gameArea.setOnMouseClicked(event -> requestFocus());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> render());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> render());

        animationTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                double delta = previousFrame == 0 ? 0 : (now - previousFrame) / 1_000_000_000.0;
                previousFrame = now;
                try {
                    snapshot = onUpdate.update(firstPlayerControls(), secondPlayerControls(), delta);
                    if (snapshot.finished()) {
                        animationTimer.stop(); keyboard.clear();
                        String result = snapshot.winner() == 0 ? "¡EMPATE!" :
                                "¡GANA " + (snapshot.winner() == 1 ? snapshot.firstPlayer().countryName() : snapshot.secondPlayer().countryName()) + "!";
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("DOPO-SOCCER"); alert.setHeaderText("¡PARTIDO TERMINADO!");
                        alert.setContentText(result + "\n\nMarcador final: " + snapshot.firstPlayer().goals() + " - " + snapshot.secondPlayer().goals());
                        alert.show(); render(); return;
                    }
                } catch (Exception exception) {
                    stop();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error del juego"); alert.setHeaderText("No se pudo actualizar el partido");
                    alert.setContentText(exception.getMessage()); alert.show(); return;
                }
                render();
            }
        };
        animationTimer.start();
    }

    public void stop() { animationTimer.stop(); keyboard.clear(); }

    private void render() {
        if (snapshot == null) return;
        double width = canvas.getWidth(), height = canvas.getHeight();
        if (width <= 0 || height <= 0) return;
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(Color.web("#88c9ee")); graphics.fillRect(0, 0, width, height);
        double scale = Math.min(width / snapshot.width(), height / snapshot.height());
        double offsetX = (width - snapshot.width() * scale) / 2;
        double offsetY = (height - snapshot.height() * scale) / 2;
        graphics.save(); graphics.setTransform(scale, 0, 0, scale, offsetX, offsetY);
        renderer.render(graphics, snapshot);
        effectsLayer.update(snapshot.firstPlayer(), snapshot.secondPlayer(), scale, offsetX, offsetY);
        graphics.restore();
    }

    private PlayerControls firstPlayerControls() {
        return new PlayerControls(keyboard.isPressed(KeyCode.A), keyboard.isPressed(KeyCode.D),
                keyboard.isPressed(KeyCode.W), keyboard.isPressed(KeyCode.F), keyboard.isPressed(KeyCode.G));
    }
    private PlayerControls secondPlayerControls() {
        return new PlayerControls(keyboard.isPressed(KeyCode.LEFT), keyboard.isPressed(KeyCode.RIGHT),
                keyboard.isPressed(KeyCode.UP), keyboard.isPressed(KeyCode.ENTER), keyboard.isPressed(KeyCode.SHIFT));
    }
}
