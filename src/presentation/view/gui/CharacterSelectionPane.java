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
import javafx.scene.control.ScrollPane;
import javafx.util.StringConverter;
import java.util.List;
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
        MatchConfigurationPane matchSettings = new MatchConfigurationPane(matchConfig, matchTypes);
        VBox firstCard = new PlayerCountryCard(
                "JUGADOR 1", matchConfig.firstCountry(), true, matchConfig, countries);

        String secondHeading = "PLAYER_VS_MACHINE".equals(matchConfig.playerMode())
                ? "MÁQUINA" : "JUGADOR 2";
        VBox secondCard = new PlayerCountryCard(
                secondHeading, matchConfig.secondCountry(), false, matchConfig, countries);

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
            try {
                matchSettings.validateAndApply();
            } catch (IllegalArgumentException exception) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Configuración inválida");
                alert.setHeaderText("Revisa los parámetros del partido");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
                return;
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

    private void startMatch() {
        onStartMatch.accept(matchConfig);
    }
}
