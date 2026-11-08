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

/**
 * Pantalla donde se juega el partido.
 *
 * Es un BorderPane: en el centro va el lienzo donde se dibuja la
 * cancha y abajo una barra con los controles de cada jugador y el botón
 * "Volver al menú".
 *
 * Conecta tres piezas: lee el teclado, se lo entrega al
 * GameEngine para que avance el partido y le pide al
 * GameRenderer que lo dibuje. Esto se repite en cada cuadro gracias
 * a un AnimationTimer, que JavaFX llama unas 60 veces por
 * segundo.
 *
 * Controles: jugador 1 con A, D, W, F y G (mover, saltar, patear,
 * poder); jugador 2 con las flechas, Enter y Shift.
 */
public final class GamePlayPane extends BorderPane {

    /** Ancho de la cancha lógica, tomado de {@link Game#WIDTH}. */
    private static final double GAME_WIDTH = Game.WIDTH;

    /** Alto de la cancha lógica, tomado de {@link Game#HEIGHT}. */
    private static final double GAME_HEIGHT = Game.HEIGHT;

    /** Teclas que están presionadas en este momento. */
    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

    /** Motor que hace avanzar el partido. */
    private final GameEngine engine;

    /** Objeto que dibuja el partido. */
    private final GameRenderer renderer = new GameRenderer();

    /** Lienzo donde se dibuja; su tamaño sigue al del área de juego. */
    private final Canvas canvas = new Canvas();

    /** Temporizador que ejecuta un paso del juego en cada cuadro. */
    private final AnimationTimer animationTimer;

    /** Marca de tiempo (en nanosegundos) del cuadro anterior; 0 si aún no hubo. */
    private long previousFrame;

    /**
     * Crea la pantalla de juego y arranca el ciclo de actualización.
     *
     * Arma el lienzo y la barra inferior, registra los eventos de teclado
     * (guardar la tecla al presionarla y quitarla al soltarla) y crea el
     * AnimationTimer. En cada cuadro este calcula cuántos segundos
     * pasaron desde el anterior (0 en el primer cuadro), actualiza el
     * partido y lo dibuja.
     *
     * @param game partido que se va a jugar
     * @param matchConfig configuración elegida; se conserva para poder
     *        volver al menú principal con las mismas opciones
     */
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

    /**
     * Detiene el ciclo del juego y olvida las teclas presionadas. Hay que
     * llamarlo al salir de esta pantalla; si no, el temporizador seguiría
     * ejecutándose en segundo plano.
     */
    public void stop() {
        animationTimer.stop();
        pressedKeys.clear();
    }

    /**
     * Dibuja el partido en el lienzo.
     *
     * Primero pinta el fondo de todo el lienzo. Luego calcula una escala
     * (la mayor que cabe sin deformar la cancha de 960 por 540) y un
     * desplazamiento para centrarla, aplica esa transformación y le pide al
     * GameRenderer que dibuje. Así el juego se ve completo aunque
     * cambie el tamaño de la ventana. No hace nada si el lienzo todavía no
     * tiene tamaño.
     */
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

    /**
     * Arma los controles del primer jugador según las teclas presionadas:
     * A (izquierda), D (derecha), W (saltar), F (patear) y G (poder).
     *
     * @return los controles actuales del jugador 1
     */
    private PlayerInput firstPlayerInput() {
        return new PlayerInput(pressedKeys.contains(KeyCode.A), pressedKeys.contains(KeyCode.D),
                pressedKeys.contains(KeyCode.W), pressedKeys.contains(KeyCode.F),
                pressedKeys.contains(KeyCode.G));
    }

    /**
     * Arma los controles del segundo jugador según las teclas presionadas:
     * flecha izquierda, flecha derecha, flecha arriba (saltar), Enter
     * (patear) y Shift (poder).
     *
     * @return los controles actuales del jugador 2
     */
    private PlayerInput secondPlayerInput() {
        return new PlayerInput(pressedKeys.contains(KeyCode.LEFT), pressedKeys.contains(KeyCode.RIGHT),
                pressedKeys.contains(KeyCode.UP), pressedKeys.contains(KeyCode.ENTER),
                pressedKeys.contains(KeyCode.SHIFT));
    }
}