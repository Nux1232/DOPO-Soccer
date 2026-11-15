package presentation.view.gui;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GameMatchConfig;
import domain.model.game.PlayerMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import presentation.view.util.ViewNavigator;

/** Pantalla de selección de personajes para el modo Jugador vs Jugador. */
public class CharacterSelectionPane extends StackPane {

    private static final String BACKGROUND_PATH = "/resources/Images/background_selection.png";
    private final GameMatchConfig matchConfig;

    public CharacterSelectionPane(GameMatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        setupUI();
    }

    private void setupUI() {
        Image background = new Image(getClass().getResourceAsStream(BACKGROUND_PATH));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(widthProperty());
        backgroundView.fitHeightProperty().bind(heightProperty());

        Label title = new Label("SELECCIÓN DE PERSONAJES");
        title.setStyle("-fx-font-size: 27px; -fx-font-weight: 900; -fx-text-fill: white;");

        Label mode = new Label("MODALIDAD: JUGADOR VS JUGADOR");
        mode.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ffbd72;"
                + "-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 18;"
                + "-fx-padding: 8 18 8 18;");

        VBox firstCard = createPlayerCard("JUGADOR 1", matchConfig.getPlayer1Character(), true);
        VBox secondCard = createPlayerCard("JUGADOR 2", matchConfig.getPlayer2Character(), false);
        HBox players = new HBox(28, firstCard, secondCard);
        players.setAlignment(Pos.CENTER);

        Button startButton = new Button("¡COMENZAR PARTIDO!");
        startButton.setDefaultButton(true);
        startButton.setStyle("-fx-background-color: linear-gradient(to bottom, #54d879, #159b50);"
                + "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
                + "-fx-background-radius: 24; -fx-padding: 12 34 12 34; -fx-cursor: hand;");
        startButton.setOnAction(event -> {
            Game match = null;
            try {
                match = new Game(PlayerMode.PLAYER_VS_PLAYER,
                        matchConfig.getPlayer1Character(), matchConfig.getPlayer2Character());
            } catch (DopoSoccerException e) {
                throw new RuntimeException(e);
            }
            ViewNavigator.loadView(new GamePlayPane(match, matchConfig));
        });

        Button backButton = new Button("Volver al menú principal");
        backButton.setStyle("-fx-background-color: rgba(255,255,255,0.14);"
                + "-fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 20;"
                + "-fx-border-color: rgba(255,255,255,0.55); -fx-border-radius: 20;"
                + "-fx-padding: 9 22 9 22; -fx-cursor: hand;");
        backButton.setOnAction(event -> ViewNavigator.loadView(new MainMenuPane(matchConfig)));

        VBox content = new VBox(20, title, mode, players, startButton, backButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28, 35, 28, 35));
        content.setMaxWidth(850);
        content.setStyle("-fx-background-color: rgba(5, 25, 54, 0.78);"
                + "-fx-background-radius: 26; -fx-border-color: rgba(255,255,255,0.55);"
                + "-fx-border-radius: 26; -fx-border-width: 1.5;");

        getChildren().addAll(backgroundView, content);
        StackPane.setAlignment(content, Pos.CENTER);
        StackPane.setMargin(content, new Insets(65, 20, 0, 20));
    }

    private VBox createPlayerCard(String heading, Country selectedCountry, boolean firstPlayer) {
        Label playerLabel = new Label(heading);
        playerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: white;");

        Label instruction = new Label("Elige tu país");
        instruction.setStyle("-fx-font-size: 12px; -fx-text-fill: #c8d9eb;");

        ComboBox<Country> countrySelector = new ComboBox<>();
        countrySelector.getItems().setAll(Country.values());
        countrySelector.setValue(selectedCountry);
        countrySelector.setMaxWidth(Double.MAX_VALUE);
        countrySelector.setPrefWidth(260);
        countrySelector.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        countrySelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (firstPlayer) {
                    matchConfig.setPlayer1Character(newValue);
                } else {
                    matchConfig.setPlayer2Character(newValue);
                }
            }
        });

        VBox card = new VBox(12, playerLabel, instruction, countrySelector);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.10);"
                + "-fx-background-radius: 18; -fx-border-color: rgba(255,255,255,0.30);"
                + "-fx-border-radius: 18; -fx-border-width: 1;");
        return card;
    }
}
