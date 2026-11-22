package presentation.view.gui;
import presentation.model.MatchSetup;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ScrollPane;
import javafx.util.StringConverter;
import java.util.function.Consumer;
/**
 * Pantalla de selección de personajes.
 *
 * Presenta los dos jugadores y permite elegir
 * el país que representará cada uno.
 */
public class CharacterSelectionPane extends StackPane {

    private static final String BACKGROUND_PATH =
            "/resources/Images/background_selection.png";

    private final MatchSetup matchConfig;
    private final Consumer<MatchSetup> onStartMatch;
    private final Runnable onBack;

    public CharacterSelectionPane(MatchSetup matchConfig, Consumer<MatchSetup> onStartMatch, Runnable onBack) {
        this.matchConfig = matchConfig;
        this.onStartMatch = onStartMatch;
        this.onBack = onBack;
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
                        "-fx-padding: 8 20 8 20;"
        );
        // MODALIDAD DEL PARTIDO
        Label matchTypeLabel = new Label("MODO DE JUEGO");
        matchTypeLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: white;"
        );
        matchTypeLabel.setMinHeight(30);
        matchTypeLabel.setAlignment(Pos.CENTER);

        ComboBox<MatchSetup.Type> matchTypeSelector = new ComboBox<>();
        matchTypeSelector.getItems().setAll(MatchSetup.Type.values());
        matchTypeSelector.setValue(matchConfig.type());
        matchTypeSelector.setPrefWidth(190);
        matchTypeSelector.setPrefHeight(38);

        matchTypeSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(MatchSetup.Type type) {
                if (type == null) return "";
                return switch (type) {
                    case GOALS -> "GOLES";
                    case TIME -> "POR TIEMPO";
                };
            }

            @Override
            public MatchSetup.Type fromString(String value) {
                return null;
            }
        });


        TextField goalsSelector = new TextField(String.valueOf(matchConfig.goalLimit()));
        goalsSelector.setPrefWidth(110);
        goalsSelector.setPrefHeight(38);
        goalsSelector.setPromptText("Ej. 10");

        goalsSelector.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null
        ));

        goalsSelector.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                try {
                    matchConfig.goalLimit(Integer.parseInt(newValue));
                } catch (NumberFormatException ignored) {
                    // Se conserva el último valor válido.
                }
            }
        });

        TextField timeSelector = new TextField(
                String.valueOf(matchConfig.durationSeconds() / 60)
        );
        timeSelector.setPrefWidth(110);
        timeSelector.setPrefHeight(38);
        timeSelector.setPromptText("Ej. 4");

        timeSelector.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null
        ));

        timeSelector.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                try {
                    matchConfig.durationSeconds(Math.multiplyExact(Integer.parseInt(newValue), 60));
                } catch (NumberFormatException ignored) {
                    // El valor se valida automaticamente
                }
            }
        });


        // Etiquetas de configuración
        Label goalsLabel = new Label("Límite de goles");
        goalsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label timeLabel = new Label("Duración (minutos)");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        VBox goalsBox = new VBox(6, goalsLabel, goalsSelector);
        goalsBox.setAlignment(Pos.CENTER_LEFT);

        VBox timeBox = new VBox(6, timeLabel, timeSelector);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        // Mostrar únicamente el parámetro correspondiente
        Runnable updateMatchOptions = () -> {
            boolean goalMode = matchConfig.type() == MatchSetup.Type.GOALS;

            goalsBox.setVisible(goalMode);
            goalsBox.setManaged(goalMode);

            timeBox.setVisible(!goalMode);
            timeBox.setManaged(!goalMode);
        };

        matchTypeSelector.setOnAction(event -> {
            MatchSetup.Type selected = matchTypeSelector.getValue();

            if (selected != null) {
                matchConfig.type(selected);
                updateMatchOptions.run();
            }
        });

        updateMatchOptions.run();


// Distribución de los controles
        Label modeLabel = new Label("MODALIDAD");
        modeLabel.setStyle(
                "-fx-text-fill: #c8d9eb;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        VBox modeBox = new VBox(7, modeLabel, matchTypeSelector);
        modeBox.setAlignment(Pos.CENTER);

        goalsLabel.setText("LÍMITE DE GOLES");
        timeLabel.setText("DURACIÓN (MINUTOS)");

        goalsLabel.setStyle(
                "-fx-text-fill: #c8d9eb;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        timeLabel.setStyle(
                "-fx-text-fill: #c8d9eb;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        VBox settingsOptions = new VBox();
        HBox settingsRow = new HBox(18,
                modeBox,
                goalsBox,
                timeBox
        );

        settingsRow.setAlignment(Pos.CENTER);

        settingsOptions.getChildren().add(settingsRow);
        settingsOptions.setAlignment(Pos.CENTER);

        VBox matchSettings = new VBox(
                12,
                matchTypeLabel,
                settingsOptions
        );

        matchSettings.setAlignment(Pos.CENTER);
        matchSettings.setPadding(new Insets(14, 20, 14, 20));
        matchSettings.setPrefWidth(560);
        matchSettings.setMaxWidth(560);

        matchSettings.setStyle(
                "-fx-background-color: rgba(8,25,48,0.94);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.18);" +
                        "-fx-border-radius: 18;"
        );



        VBox firstCard = createPlayerCard("JUGADOR 1", matchConfig.firstCountry(), true
        );

        VBox secondCard = createPlayerCard("JUGADOR 2", matchConfig.secondCountry(), false
        );

        HBox playersContainer = new HBox(22, firstCard, secondCard);

        Label controlsTitle = new Label("¿CÓMO JUGAR?");
        controlsTitle.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: white;"
        );

        Label player1Controls = new Label(
                "JUGADOR 1   A / D: Moverse     W: Saltar     F: Patear     G: Poder"
        );
        player1Controls.setStyle(
                "-fx-text-fill: #ffbd72;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        Label player2Controls = new Label(
                "JUGADOR 2   ← / →: Moverse     ↑: Saltar     ENTER: Patear     SHIFT: Poder"
        );
        player2Controls.setStyle(
                "-fx-text-fill: #8ecbff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        VBox controlsBox = new VBox(
                10,
                controlsTitle,
                player1Controls,
                player2Controls
        );

        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(12));
        controlsBox.setMaxWidth(760);

        controlsBox.setStyle(
                "-fx-background-color: rgba(8,25,48,0.88);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 18;"
        );


        playersContainer.setAlignment(Pos.CENTER);
        Button startButton = new Button("¡COMENZAR PARTIDO!");
        startButton.setDefaultButton(true);
        startButton.setPrefWidth(260);
        startButton.setPrefHeight(46);

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
        backButton.setPrefHeight(36);

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

        backButton.setOnAction(event -> onBack.run());

        VBox content = new VBox(
                10, title,
                subtitle, matchSettings,
                playersContainer, controlsBox,
                startButton, backButton
        );

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(12, 25, 12, 25));
        content.setMaxWidth(1000);

        StackPane centeredContent = new StackPane(content);
        centeredContent.setAlignment(Pos.CENTER);
        centeredContent.setPadding(new Insets(15));

        ScrollPane scrollPane = new ScrollPane(centeredContent);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);

        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );

        getChildren().addAll(backgroundView, scrollPane);
        StackPane.setAlignment(scrollPane, Pos.CENTER);

    }

    private VBox createPlayerCard(String heading, MatchSetup.Country selectedCountry, boolean firstPlayer) {
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
        ComboBox<MatchSetup.Country> countrySelector = new ComboBox<>();
        countrySelector.getItems().setAll(MatchSetup.Country.values());
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
                        matchConfig.firstCountry(newValue);
                    } else {
                        matchConfig.secondCountry(newValue);
                    }
                }
        );

        VBox card = new VBox(16, playerLabel, instruction, countrySelector);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(330);
        card.setMinWidth(500);
        card.setMaxWidth(300);
        card.setMinHeight(140);
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
        onStartMatch.accept(matchConfig);
    }

}
