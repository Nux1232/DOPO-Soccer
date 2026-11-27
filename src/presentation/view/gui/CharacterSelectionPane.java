package presentation.view.gui;
import presentation.model.AIProfileOption;
import presentation.model.CountryOption;
import presentation.model.MatchSetup;
import presentation.model.MatchTypeOption;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ScrollPane;
import javafx.util.StringConverter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
    private final List<CountryOption> countries;
    private final List<AIProfileOption> aiProfiles;
    private final List<MatchTypeOption> matchTypes;
    private final Consumer<MatchSetup> onStartMatch;
    private final Runnable onBack;
    private final Runnable onImprove;

    /**
     * Crea la pantalla de selección de países, modalidad, resolución y mejoras previas.
     *
     * @param matchConfig configuración mutable del partido que se está preparando
     * @param countries países disponibles para seleccionar
     * @param aiProfiles perfiles de inteligencia artificial disponibles
     * @param onStartMatch acción invocada al iniciar el partido
     * @param onBack acción invocada para regresar al menú principal
     * @param onImprove acción invocada para abrir el diálogo de mejoras de estadísticas
     */
    public CharacterSelectionPane(MatchSetup matchConfig,
                                  List<CountryOption> countries,
                                  List<AIProfileOption> aiProfiles,
                                  List<MatchTypeOption> matchTypes,
                                  Consumer<MatchSetup> onStartMatch,
                                  Runnable onBack,
                                  Runnable onImprove) {
        this.matchConfig = matchConfig;
        this.countries = List.copyOf(countries);
        this.aiProfiles = List.copyOf(aiProfiles);
        this.matchTypes = List.copyOf(matchTypes);
        this.onStartMatch = onStartMatch;
        this.onBack = onBack;
        this.onImprove = onImprove;
        setupUI();
    }

    private void setupUI() {
        // Fondo ilustrado + capa oscura para conservar la legibilidad.
        ImageView background = new ImageView();
        var backgroundResource = getClass().getResource(BACKGROUND_PATH);
        if (backgroundResource != null) {
            background.setImage(new Image(backgroundResource.toExternalForm(), true));
        } else {
            System.err.println("No se encontró background_selection.png: " + BACKGROUND_PATH);
        }
        background.setPreserveRatio(false);
        background.setSmooth(true);
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());

        StackPane backgroundTint = new StackPane();
        backgroundTint.setStyle("-fx-background-color: rgba(3, 12, 30, 0.58);");

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

        ComboBox<MatchTypeOption> matchTypeSelector = new ComboBox<>();
        matchTypeSelector.getItems().setAll(matchTypes);
        MatchTypeOption selectedType = matchTypes.stream()
                .filter(type -> type.id().equals(matchConfig.matchType()))
                .findFirst()
                .orElse(matchTypes.isEmpty() ? null : matchTypes.get(0));
        if (selectedType != null) {
            matchConfig.matchType(selectedType.id());
            matchTypeSelector.setValue(selectedType);
        }
        matchTypeSelector.setPrefWidth(190);
        matchTypeSelector.setPrefHeight(38);

        matchTypeSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(MatchTypeOption type) {
                return type == null ? "" : type.displayName();
            }

            @Override
            public MatchTypeOption fromString(String value) {
                return matchTypes.stream()
                        .filter(type -> type.displayName().equals(value))
                        .findFirst()
                        .orElse(null);
            }
        });


        VBox parameterBox = new VBox(8);
        Map<String, TextField> parameterFields = new HashMap<>();
        parameterBox.setAlignment(Pos.CENTER_LEFT);
        Runnable updateParameters = () -> {
            parameterBox.getChildren().clear();
            parameterFields.clear();
            MatchTypeOption selected = matchTypeSelector.getValue();
            if (selected == null) return;
            for (MatchTypeOption.Parameter parameter : selected.parameters()) {
                Label label = new Label(parameter.label());
                label.setStyle("-fx-text-fill: #c8d9eb; -fx-font-size: 12px; -fx-font-weight: bold;");
                TextField field = new TextField(
                        matchConfig.parameter(parameter.id(), parameter.defaultValue()));
                field.setPrefWidth(140);
                field.setPrefHeight(38);
                field.setTextFormatter(new TextFormatter<String>(change ->
                        change.getControlNewText().matches("\\d*") ? change : null));
                matchConfig.setParameter(parameter.id(), field.getText());
                applyMatchParameter(parameter, field.getText());
                field.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue.isBlank()) return;
                    try {
                        int value = Integer.parseInt(newValue);
                        if (value < parameter.minimum() || value > parameter.maximum()) return;
                        matchConfig.setParameter(parameter.id(), newValue);
                        applyMatchParameter(parameter, newValue);
                    } catch (ArithmeticException | NumberFormatException ignored) {
                        // Se conserva el último valor válido hasta que el campo sea corregido.
                    }
                });
                parameterFields.put(parameter.id(), field);
                parameterBox.getChildren().add(new VBox(5, label, field));
            }
        };
        updateParameters.run();

        Runnable updateMatchOptions = () -> {
            updateParameters.run();
        };

        matchTypeSelector.setOnAction(event -> {
            MatchTypeOption selected = matchTypeSelector.getValue();

            if (selected != null) {
                matchConfig.matchType(selected.id());
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

        VBox settingsOptions = new VBox();
        HBox settingsRow = new HBox(18,
                modeBox,
                parameterBox
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

        String secondHeading = "PLAYER_VS_MACHINE".equals(matchConfig.playerMode())
                ? "MÁQUINA" : "JUGADOR 2";
        VBox secondCard = createPlayerCard(secondHeading, matchConfig.secondCountry(), false);

        HBox playerRow = new HBox(18);
        playerRow.setAlignment(Pos.CENTER);
        VBox playerColumn = new VBox(16);
        playerColumn.setAlignment(Pos.CENTER);
        StackPane playersContainer = new StackPane(playerRow, playerColumn);
        playerColumn.setVisible(false);
        playerColumn.setManaged(false);
        Runnable layoutPlayers = () -> {
            boolean narrow = playersContainer.getWidth() < 760;
            playerRow.getChildren().clear();
            playerColumn.getChildren().clear();
            if (narrow) {
                firstCard.setPrefWidth(Math.max(260, Math.min(520, playersContainer.getWidth() - 35)));
                secondCard.setPrefWidth(Math.max(260, Math.min(520, playersContainer.getWidth() - 35)));
                playerColumn.getChildren().setAll(firstCard, secondCard);
            } else {
                double cardWidth = Math.min(500, (playersContainer.getWidth() - 40) / 2);
                firstCard.setPrefWidth(cardWidth);
                secondCard.setPrefWidth(cardWidth);
                playerRow.getChildren().setAll(firstCard, secondCard);
            }
            playerRow.setVisible(!narrow);
            playerRow.setManaged(!narrow);
            playerColumn.setVisible(narrow);
            playerColumn.setManaged(narrow);
        };
        playersContainer.widthProperty().addListener((obs, oldWidth, newWidth) -> layoutPlayers.run());
        playersContainer.setMaxWidth(Double.MAX_VALUE);

        VBox aiProfileBox = new VBox(6);
        Label aiProfileLabel = new Label("PERFIL DE LA MÁQUINA");
        aiProfileLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        ComboBox<AIProfileOption> aiProfileSelector = new ComboBox<>();
        aiProfileSelector.getItems().setAll(aiProfiles);
        AIProfileOption selectedAiProfile = aiProfiles.stream()
                .filter(profile -> profile.id().equalsIgnoreCase(matchConfig.aiProfile()))
                .findFirst()
                .orElse(aiProfiles.isEmpty() ? null : aiProfiles.get(aiProfiles.size() - 1));
        aiProfileSelector.setValue(selectedAiProfile);
        if (selectedAiProfile != null) {
            matchConfig.aiProfile(selectedAiProfile.id());
        }
        aiProfileSelector.setPrefWidth(220);
        aiProfileSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                matchConfig.aiProfile(newValue.id());
            }
        });
        aiProfileBox.getChildren().addAll(aiProfileLabel, aiProfileSelector);
        aiProfileBox.setAlignment(Pos.CENTER);
        aiProfileBox.setVisible("PLAYER_VS_MACHINE".equals(matchConfig.playerMode()));
        aiProfileBox.setManaged(aiProfileBox.isVisible());

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
                "JUGADOR 2 / MÁQUINA   ← / →: Moverse     ↑: Saltar     ENTER: Patear     SHIFT: Poder"
        );
        player2Controls.setStyle(
                "-fx-text-fill: #8ecbff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        Label resolutionLabel = new Label("RESOLUCIÓN DE CANCHA");
        resolutionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        ComboBox<MatchSetup.Resolution> resolutionSelector = new ComboBox<>();
        resolutionSelector.getItems().setAll(MatchSetup.Resolution.values());
        resolutionSelector.setValue(matchConfig.resolution());
        resolutionSelector.setConverter(new StringConverter<>() {
            @Override public String toString(MatchSetup.Resolution value) {
                if (value == null) return "";
                return value.width() + " × " + value.height();
            }
            @Override public MatchSetup.Resolution fromString(String text) { return null; }
        });
        resolutionSelector.valueProperty().addListener((obs, oldValue, value) -> matchConfig.resolution(value));
        VBox resolutionBox = new VBox(5, resolutionLabel, resolutionSelector);
        resolutionBox.setAlignment(Pos.CENTER);

        VBox controlsBox = new VBox(
                10,
                controlsTitle,
                player1Controls,
                player2Controls,
                resolutionBox
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

        startButton.setOnAction(event -> {
            MatchTypeOption selected = matchTypeSelector.getValue();
            if (selected != null) {
                for (MatchTypeOption.Parameter parameter : selected.parameters()) {
                    TextField field = parameterFields.get(parameter.id());
                    try {
                        int value = Integer.parseInt(field.getText());
                        if (value < parameter.minimum() || value > parameter.maximum()) {
                            throw new NumberFormatException();
                        }
                        Math.multiplyExact(value, parameter.multiplier());
                    } catch (ArithmeticException | NumberFormatException exception) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.WARNING);
                        alert.setTitle("Configuración inválida");
                        alert.setHeaderText("Revisa los parámetros del partido");
                        alert.setContentText(parameter.label() + " debe estar entre "
                                + parameter.minimum() + " y " + parameter.maximum() + ".");
                        alert.showAndWait();
                        return;
                    }
                }
            }
            startMatch();
        });

        Button improveButton = new Button("MEJORAR ESTADÍSTICAS");
        improveButton.setPrefWidth(260);
        improveButton.setOnAction(event -> onImprove.run());

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
                aiProfileBox, playersContainer, controlsBox,
                improveButton, startButton, backButton
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

        getChildren().setAll(background, backgroundTint, scrollPane);
        StackPane.setAlignment(scrollPane, Pos.CENTER);
    }

    private void applyMatchParameter(MatchTypeOption.Parameter parameter, String text) {
        try {
            int configuredValue = Math.multiplyExact(
                    Integer.parseInt(text), parameter.multiplier());
            if ("goalLimit".equals(parameter.id())) matchConfig.goalLimit(configuredValue);
            if ("durationSeconds".equals(parameter.id())) matchConfig.durationSeconds(configuredValue);
        } catch (ArithmeticException | NumberFormatException ignored) {
            // Se mantiene el último valor válido mientras el campo se edita.
        }
    }

    private VBox createPlayerCard(String heading, String selectedCountryId, boolean firstPlayer) {
        Label playerLabel = new Label(heading);
        playerLabel.setStyle(
                "-fx-font-size: 19px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: white;"
        );
        Label instruction = new Label("ELIGE TU PAÍS");
        instruction.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #c8d9eb;"
        );
        ComboBox<CountryOption> countrySelector = new ComboBox<>();
        countrySelector.getItems().setAll(countries);
        CountryOption selectedCountry = countries.stream()
                .filter(country -> country.id().equalsIgnoreCase(selectedCountryId))
                .findFirst()
                .orElse(countries.isEmpty() ? null : countries.get(0));
        countrySelector.setValue(selectedCountry);
        if (selectedCountry != null) {
            if (firstPlayer) {
                matchConfig.firstCountry(selectedCountry.id());
            } else {
                matchConfig.secondCountry(selectedCountry.id());
            }
        }
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
                        matchConfig.firstCountry(newValue.id());
                    } else {
                        matchConfig.secondCountry(newValue.id());
                    }
                }
        );
        ImageView flagView = new ImageView();
        flagView.setFitWidth(58);
        flagView.setFitHeight(38);
        flagView.setPreserveRatio(true);
        flagView.setSmooth(true);
        updateFlag(flagView, selectedCountry);
        countrySelector.valueProperty().addListener((observable, oldValue, newValue) -> updateFlag(flagView, newValue));
        HBox countryChoice = new HBox(12, flagView, countrySelector);
        countryChoice.setAlignment(Pos.CENTER);
        countryChoice.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(countryChoice, javafx.scene.layout.Priority.ALWAYS);
        VBox card = new VBox(16, playerLabel, instruction, countryChoice);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(420);
        card.setMinWidth(260);
        card.setMaxWidth(520);
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


    private void updateFlag(ImageView flagView, CountryOption country) {
        if (country == null) {
            flagView.setImage(null);
            return;
        }
        var resource = getClass().getResource("/resources/Images/flags/" + country.flagAsset());
        flagView.setImage(resource == null ? null : new Image(resource.toExternalForm()));
    }
    private void startMatch() {
        onStartMatch.accept(matchConfig);
    }
}
