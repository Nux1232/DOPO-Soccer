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

/**
 * Vista de juego durante un partido activo.
 * Recibe instantáneas inmutables de presentación y envía los controles del teclado hacia arriba.
 */
public final class GamePlayPane extends BorderPane {
    private final KeyboardInputHandler keyboard = new KeyboardInputHandler();

    /** Contrato funcional para avanzar la simulación física y lógica del partido. */
    @FunctionalInterface
    public interface GameUpdateHandler {
        /**
         * Actualiza el partido un paso de tiempo.
         *
         * @param first controles del jugador izquierdo
         * @param second controles del jugador derecho
         * @param deltaSeconds segundos transcurridos desde el cuadro anterior
         * @return instantánea actualizada del partido
         * @throws Exception si ocurre un error en el dominio durante la actualización
         */
        GameSnapshot update(PlayerControls first, PlayerControls second, double deltaSeconds) throws Exception;
    }

    /** Contrato funcional para finalizar anticipadamente el partido activo. */
    @FunctionalInterface
    public interface GameFinishHandler {
        /**
         * Da por terminado el partido activo y devuelve su instantánea final.
         *
         * @return instantánea del partido finalizado
         * @throws Exception si no se puede finalizar el partido
         */
        GameSnapshot finish() throws Exception;
    }

    private final GameUpdateHandler onUpdate;
    private final Runnable onBack;
    private final Runnable onSave;
    private final GameFinishHandler onFinish;
    private final GameRenderer renderer = new GameRenderer();
    private final PlayerEffectsLayer effectsLayer = new PlayerEffectsLayer();
    private final Canvas canvas = new Canvas();
    private AnimationTimer animationTimer;
    private boolean paused = false;
    private GameSnapshot snapshot;
    private long previousFrame;

    /**
     * Crea la vista de juego sin acciones de guardado ni finalización anticipada personalizadas.
     *
     * @param initialSnapshot instantánea inicial del partido
     * @param onUpdate manejador de actualización por cuadro
     * @param onBack acción para volver al menú principal
     */
    public GamePlayPane(GameSnapshot initialSnapshot, GameUpdateHandler onUpdate, Runnable onBack) {
        this(initialSnapshot, onUpdate, onBack, () -> {}, () -> initialSnapshot);
    }

    /**
     * Crea la vista de juego con soporte de guardado.
     *
     * @param initialSnapshot instantánea inicial del partido
     * @param onUpdate manejador de actualización por cuadro
     * @param onBack acción para volver al menú principal
     * @param onSave acción para guardar el partido
     */
    public GamePlayPane(GameSnapshot initialSnapshot, GameUpdateHandler onUpdate, Runnable onBack, Runnable onSave) {
        this(initialSnapshot, onUpdate, onBack, onSave, () -> initialSnapshot);
    }

    /**
     * Crea la vista de juego completa con guardado y finalización anticipada.
     *
     * @param initialSnapshot instantánea inicial del partido
     * @param onUpdate manejador de actualización por cuadro
     * @param onBack acción para volver al menú principal
     * @param onSave acción para guardar el partido
     * @param onFinish manejador para terminar el partido manualmente
     */
    public GamePlayPane(GameSnapshot initialSnapshot, GameUpdateHandler onUpdate,
            Runnable onBack, Runnable onSave, GameFinishHandler onFinish) {
        this.snapshot = initialSnapshot;
        this.onUpdate = onUpdate;
        this.onBack = onBack;
        this.onSave = onSave;
        this.onFinish = onFinish;
        setStyle("-fx-background-color: #edf3f5;");

        StackPane gameArea = new StackPane(canvas, effectsLayer);
        gameArea.setStyle("-fx-background-color: #88c9ee;");
        canvas.widthProperty().bind(gameArea.widthProperty());
        canvas.heightProperty().bind(gameArea.heightProperty());
        setCenter(gameArea);

        javafx.scene.control.Button pauseButton = new javafx.scene.control.Button("Pausar");
        pauseButton.setFocusTraversable(false);
        pauseButton.setOnAction(event -> {
            if (snapshot != null && snapshot.finished()) return;
            if (!paused) {
                animationTimer.stop();
                paused = true;
                keyboard.clear();
                pauseButton.setText("Continuar");
            } else {
                previousFrame = 0;
                keyboard.clear();
                paused = false;
                animationTimer.start();
                pauseButton.setText("Pausar");
                requestFocus();
            }
        });
        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Guardar partida");
        saveButton.setFocusTraversable(false);
        saveButton.setOnAction(event -> onSave.run());
        javafx.scene.control.Button finishButton = new javafx.scene.control.Button("Terminar partido");
        finishButton.setFocusTraversable(false);
        finishButton.setOnAction(event -> finishMatchManually());
        javafx.scene.control.Button menuButton = new javafx.scene.control.Button("Volver al menú");
        menuButton.setFocusTraversable(false);
        menuButton.setOnAction(event -> { stop(); onBack.run(); });
        HBox footer = new HBox(
                new javafx.scene.control.Label("J1: A / D mover · W saltar · F patear · G poder"),
                new javafx.scene.control.Label(snapshot.machineOpponent()
                        ? "J2: MÁQUINA" : "J2: ← / → mover · ↑ saltar · Enter patear · Shift poder"),
                pauseButton, saveButton, finishButton, menuButton);
        footer.setAlignment(Pos.CENTER); footer.setSpacing(18); footer.setPadding(new Insets(12));
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
                        stop();
                        render();
                        showFinishedAlert();
                        return;
                    }
                } catch (Exception exception) {
                    stop();
                    showErrorAlert("No se pudo actualizar el partido", exception.getMessage());
                    return;
                }
                render();
            }
        };
        animationTimer.start();
    }

    private void finishMatchManually() {
        if (snapshot != null && snapshot.finished()) return;
        try {
            stop();
            snapshot = onFinish.finish();
            render();
            showFinishedAlert();
        } catch (Exception exception) {
            showErrorAlert("No se pudo terminar el partido", exception.getMessage());
        }
    }

    private void showFinishedAlert() {
        String result = snapshot.winner() == 0 ? "¡EMPATE!" :
                "¡GANA " + (snapshot.winner() == 1 ? snapshot.firstPlayer().countryName() : snapshot.secondPlayer().countryName()) + "!";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("DOPO-SOCCER");
        alert.setHeaderText("¡PARTIDO TERMINADO!");
        String pointsMessage = snapshot.winner() == 1 ? "\nPuntos: J1 +100 · J2 +25"
                : snapshot.winner() == 2 ? "\nPuntos: J1 +25 · J2 +100" : "\nPuntos: +25 para ambos";
        alert.setContentText(result + "\n\nMarcador final: "
                + snapshot.firstPlayer().goals() + " - " + snapshot.secondPlayer().goals()
                + pointsMessage);
        alert.show();
    }

    private void showErrorAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error del juego");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    /** Detiene el ciclo de animación y limpia el estado del teclado. */
    public void stop() { animationTimer.stop(); paused = true; keyboard.clear(); }

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
