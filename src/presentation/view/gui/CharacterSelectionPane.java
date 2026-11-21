
package presentation.view.gui;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GameMatchConfig;
import domain.model.physics.PhysicsEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import presentation.view.util.ViewNavigator;

/**
 * Pantalla de selección de personajes.
 *
 * Presenta los dos jugadores y permite elegir
 * el país que representará cada uno.
 */
public class CharacterSelectionPane extends StackPane {

    private static final String BACKGROUND_PATH =
            "/resources/Images/background_selection.png";

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

        title.setStyle(
                "-fx-font-size: 30px;" + "-fx-font-weight: 900;" + "-fx-text-fill: white;"
        );

        Label subtitle = new Label("ELIGE TU SELECCIÓN Y PREPÁRATE PARA JUGAR");

        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #ffbd72;" +
                        "-fx-background-color: rgba(255,255,255,0.10);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 9 22 9 22;"
        );

        VBox firstCard = createPlayerCard("JUGADOR 1", matchConfig.getPlayer1Character(), true
        );

        VBox secondCard = createPlayerCard("JUGADOR 2", matchConfig.getPlayer2Character(), false
        );

        HBox playersContainer = new HBox(30, firstCard, secondCard
        );

        playersContainer.setAlignment(Pos.CENTER);
        Button startButton = new Button("¡COMENZAR PARTIDO!");
        startButton.setDefaultButton(true);
        startButton.setPrefWidth(260);
        startButton.setPrefHeight(52);

        startButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #54d879, #159b50);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 28;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 8, 0, 0, 4);"
        );

        startButton.setOnMouseEntered(event ->
                startButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #72ef94, #20b963);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 28;" +
                                "-fx-cursor: hand;"
                )
        );

        startButton.setOnMouseExited(event ->
                startButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #54d879, #159b50);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 28;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 8, 0, 0, 4);"
                )
        );

        startButton.setOnAction(event -> startMatch());

        Button backButton = new Button("VOLVER AL MENÚ PRINCIPAL");

        backButton.setPrefWidth(230);
        backButton.setPrefHeight(42);

        backButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 22;" +
                        "-fx-border-color: rgba(255,255,255,0.55);" +
                        "-fx-border-radius: 22;" +
                        "-fx-cursor: hand;"
        );

        backButton.setOnAction(event ->
                ViewNavigator.loadView(new MainMenuPane(matchConfig))
        );

        VBox content = new VBox(24, title, subtitle, playersContainer, startButton, backButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24, 32, 24, 32));
        content.setMaxWidth(1100);
        getChildren().addAll(backgroundView, content);
        StackPane.setAlignment(content, Pos.CENTER);
    }

    private VBox createPlayerCard(String heading, Country selectedCountry, boolean firstPlayer) {
        Label playerLabel = new Label(heading);
        playerLabel.setStyle(
                "-fx-font-size: 19px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: white;"
        );
        Label instruction = new Label(
                "ELIGE TU PAÍS"
        );
        instruction.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #c8d9eb;"
        );
        ComboBox<Country> countrySelector = new ComboBox<>();
        countrySelector.getItems().setAll(Country.available());
        countrySelector.setValue(selectedCountry);
        countrySelector.setPrefWidth(285);
        countrySelector.setPrefHeight(42);
        countrySelector.setMaxWidth(Double.MAX_VALUE);
        countrySelector.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-background-radius: 10;"
        );
        countrySelector.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        return;
                    }
                    if (firstPlayer) {
                        matchConfig.setPlayer1Character(newValue);
                    } else {
                        matchConfig.setPlayer2Character(newValue);
                    }
                }
        );

        VBox card = new VBox(16, playerLabel, instruction, countrySelector);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefWidth(330);
        card.setMinWidth(300);
        card.setMaxWidth(330);
        card.setMinHeight(175);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.09);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255,255,255,0.35);" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1.3;"
        );
        return card;
    }

    private void startMatch() {

        try {
            Game match = new Game(
                    matchConfig.getGameMode(),
                    matchConfig.getPlayer1Character(),
                    matchConfig.getPlayer2Character(),
                    new PhysicsEngine());
            ViewNavigator.loadView(new GamePlayPane(match, matchConfig));

        } catch (DopoSoccerException exception) {

            Alert alert = new Alert(
                    Alert.AlertType.ERROR
            );

            alert.setTitle("DOPO-SOCCER");
            alert.setHeaderText("No se pudo iniciar el partido");
            alert.setContentText(exception.getMessage());

            alert.showAndWait();
        }
    }
}
