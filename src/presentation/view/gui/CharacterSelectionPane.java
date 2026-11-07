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

public class CharacterSelectionPane extends VBox {
    private final GameMatchConfig matchConfig;

    public CharacterSelectionPane(GameMatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        setupUI();
    }

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

    private String modeLabel(GameMatchConfig config) {
        return config.getGameMode() == GameMode.PvIA
                ? "Jugador vs máquina" : "Jugador vs jugador";
    }
}
