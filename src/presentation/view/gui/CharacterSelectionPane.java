package presentation.view.gui;

import domain.model.game.GameMatchConfig;
import domain.model.entity.Country;
import domain.model.game.Game;
import domain.model.game.GameMode;
import domain.model.game.PlayerMode;
import javafx.geometry.Insets;
import presentation.view.util.ViewNavigator;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Pantalla donde cada jugador elige su personaje (país) antes del partido.
 *
 * Es un VBox: acomoda sus elementos en una columna. Muestra la
 * modalidad elegida, una lista desplegable por jugador y los botones para
 * comenzar el partido o volver al menú principal.
 *
 * Cada vez que el usuario cambia una lista desplegable, la elección se
 * guarda de inmediato en la GameMatchConfig. Al pulsar "Comenzar
 * Partido" se crea el Game con esa configuración y se muestra la
 * pantalla de juego.
 */
public class CharacterSelectionPane extends VBox {

    /** Configuración del partido que esta pantalla lee y actualiza. */
    private final GameMatchConfig matchConfig;

    /**
     * Crea la pantalla de selección y arma su contenido.
     *
     * @param matchConfig configuración compartida; debe tener ya la
     *        modalidad elegida
     */
    public CharacterSelectionPane(GameMatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        setupUI();
    }

    /**
     * Construye y acomoda los elementos visuales de la pantalla.
     *
     * Crea el título, la etiqueta de modalidad y, para cada jugador, una
     * etiqueta y una lista desplegable con todos los países Country.
     * Cada lista arranca con el personaje que ya tiene la configuración y,
     * al cambiar, lo actualiza. El botón "Volver al Menú Principal" regresa
     * al menú. El botón "¡Comenzar Partido!" (botón por defecto, se activa
     * con Enter) traduce el GameMode elegido a un PlayerMode
     * (PvP pasa a jugador contra jugador; cualquier otro valor, a jugador
     * contra máquina), crea el Game con los dos personajes y carga
     * la pantalla de juego.
     */
    private void setupUI() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(18);
        this.setPadding(new Insets(36));
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #dff3fa, #a9d8bd);");

        Label title = new Label("SELECCIÓN DE PERSONAJES");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #15364a;");
        Label currentModeLabel = new Label("Modalidad: " + modeLabel(matchConfig));
        Label firstPlayerLabel = new Label("Jugador 1");
        ComboBox<Country> firstPlayer = new ComboBox<>();
        firstPlayer.getItems().setAll(Country.values());
        firstPlayer.setValue(matchConfig.getPlayer1Character());
        firstPlayer.valueProperty().addListener((observable, oldValue, newValue) ->
                matchConfig.setPlayer1Character(newValue));
        Label secondPlayerLabel = new Label("Jugador 2");
        ComboBox<Country> secondPlayer = new ComboBox<>();
        secondPlayer.getItems().setAll(Country.values());
        secondPlayer.setValue(matchConfig.getPlayer2Character());
        secondPlayer.valueProperty().addListener((observable, oldValue, newValue) ->
                matchConfig.setPlayer2Character(newValue));

        Button backButton = new Button("Volver al Menú Principal");
        backButton.setOnAction(e -> ViewNavigator.loadView(new MainMenuPane(matchConfig)));

        Button startMatchButton = new Button("¡Comenzar Partido!");
        startMatchButton.setDefaultButton(true);
        startMatchButton.setOnAction(e -> {
            Game match = new Game(matchConfig.getGameMode() == GameMode.PvP
                    ? PlayerMode.PLAYER_VS_PLAYER : PlayerMode.PLAYER_VS_MACHINE,
                    matchConfig.getPlayer1Character(), matchConfig.getPlayer2Character());
            ViewNavigator.loadView(new GamePlayPane(match, matchConfig));
        });

        this.getChildren().addAll(title, currentModeLabel, firstPlayerLabel, firstPlayer,
                secondPlayerLabel, secondPlayer, startMatchButton, backButton);
    }

    /**
     * Devuelve el texto que describe la modalidad elegida.
     *
     * @param config configuración de la que se lee la modalidad
     * @return "Jugador vs máquina" si la modalidad es PvIA, en
     * cualquier otro caso, "Jugador vs jugador"
     */
    private String modeLabel(GameMatchConfig config) {
        return config.getGameMode() == GameMode.PvIA
                ? "Jugador vs máquina" : "Jugador vs jugador";
    }
}