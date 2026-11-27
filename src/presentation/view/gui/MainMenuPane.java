package presentation.view.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Vista adaptable del menú principal; la navegación se delega mediante callbacks.
 */
public final class MainMenuPane extends StackPane {

    private static final String RESOURCE_ROOT = "/resources/Images/";

    private final Consumer<String> onModeSelected;
    private final Runnable onLoadMatch;

    private final VBox content = new VBox(22);
    private final HBox gameModes = new HBox(22);

    private final Button pvp;
    private final Button pvm;

    private final ImageView background;

    /**
     * Construye el menú principal con las acciones para seleccionar modo o cargar partida.
     *
     * @param onModeSelected callback invocado al elegir la modalidad ({@code PLAYER_VS_PLAYER} o {@code PLAYER_VS_MACHINE})
     * @param onLoadMatch acción invocada al solicitar cargar una partida guardada
     */
    public MainMenuPane(
            Consumer<String> onModeSelected,
            Runnable onLoadMatch
    ) {
        this.onModeSelected = onModeSelected;
        this.onLoadMatch = onLoadMatch;

        loadStyles();

        // Background
        background = createImage("background.png", true);
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(3,12,27,0.18);");
        // Header
        Text title = new Text("DOPO-SOCCER");
        title.getStyleClass().add("menu-title");
        Label subtitle = new Label("ELIGE CÓMO QUIERES JUGAR");
        subtitle.getStyleClass().add("menu-subtitle");
        Label description = new Label("Fútbol, poderes especiales y caos en la cancha");
        description.getStyleClass().add("menu-description");
        VBox header = new VBox(7, title, subtitle, description);
        header.setAlignment(Pos.CENTER);
        // Game modes
        pvp = createModeButton(
                "Jugador vs Jugador",
                "DOS JUGADORES · MISMO TECLADO",
                "pvp_banner.png",
                "PLAYER_VS_PLAYER"
        );

        pvm = createModeButton(
                "Jugador vs Máquina",
                "4 PERFILES DE INTELIGENCIA",
                "pvai_banner.png",
                "PLAYER_VS_MACHINE"
        );

        gameModes.getChildren().setAll(pvp, pvm);
        gameModes.setAlignment(Pos.CENTER);
        gameModes.setFillHeight(true);

        // Actions
        Button load = createActionButton(
                "CARGAR PARTIDA",
                "load-button"
        );

        load.setOnAction(event -> onLoadMatch.run());

        Button exit = createActionButton(
                "SALIR",
                "exit-button"
        );

        exit.setOnAction(event -> System.exit(0));
        HBox actions = new HBox(14, load, exit);
        actions.setAlignment(Pos.CENTER);
        // Main content
        content.getChildren().setAll(header, gameModes, actions);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(18, 24, 18, 24));
        content.setMaxWidth(Region.USE_PREF_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);

        content.getStyleClass().add("menu-content");

        getChildren().setAll(background, overlay, content);

        // Responsive adaptation
        widthProperty().addListener((observable, oldValue, newValue) -> adapt(newValue.doubleValue(), getHeight())
        );

        heightProperty().addListener((observable, oldValue, newValue) -> adapt(getWidth(), newValue.doubleValue())
        );

        // Initial adaptation after JavaFX calculates dimensions
        Platform.runLater(() -> adapt(getWidth(), getHeight())
        );
    }

    /**
     * Adapts menu cards and spacing to the window size.
     */
    private void adapt(double width, double height) {

        if (width <= 0 || height <= 0) {
            return;
        }
        boolean stacked = width < 900;

        double availableWidth = Math.max(300, width - 80);
        double cardWidth;

        if (stacked) {
            cardWidth = Math.min(availableWidth, 520);
        } else {
            cardWidth = Math.min((availableWidth - 22) / 2, 480);
        }

        double cardHeight = Math.max(220, Math.min(320, height * 0.34));

        // Card dimensions
        pvp.setPrefWidth(cardWidth);
        pvm.setPrefWidth(cardWidth);

        pvp.setMinWidth(250);
        pvm.setMinWidth(250);

        pvp.setPrefHeight(cardHeight);
        pvm.setPrefHeight(cardHeight);

        pvp.setMaxWidth(cardWidth);
        pvm.setMaxWidth(cardWidth);

        // Spacing
        gameModes.setSpacing(
                stacked ? 15 : 22
        );

        // Artwork dimensions
        double imageWidth = cardWidth - 40;

        double imageHeight = Math.max(
                100,
                cardHeight * 0.52
        );

        for (Button button : new Button[]{pvp, pvm}) {

            if (button.getGraphic() instanceof VBox box) {

                if (!box.getChildren().isEmpty()
                        && box.getChildren().get(0)
                        instanceof ImageView artwork) {

                    artwork.setFitWidth(imageWidth);
                    artwork.setFitHeight(imageHeight);
                    artwork.setPreserveRatio(true);
                }
            }
        }

        // Main content spacing
        content.setSpacing(
                height < 650 ? 12 : 22
        );

        content.setPadding(
                new Insets(
                        height < 650 ? 10 : 18,
                        22,
                        18,
                        22
                )
        );
    }

    /**
     * Creates a game mode card.
     */
    private Button createModeButton(
            String title,
            String description,
            String imageName,
            String mode
    ) {

        ImageView artwork = createImage(imageName, false);
        artwork.setFitWidth(380);
        artwork.setFitHeight(190);
        artwork.setPreserveRatio(true);

        artwork.getStyleClass().add("mode-artwork");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("mode-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add(
                "mode-description"
        );
        VBox card = new VBox(
                9,
                artwork,
                titleLabel,
                descriptionLabel
        );
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        Button button = new Button();
        button.setGraphic(card);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setMinHeight(220);
        button.setPrefHeight(280);
        button.setMaxHeight(Region.USE_PREF_SIZE);
        button.setMinWidth(250);
        button.getStyleClass().add("mode-card"
        );
        button.setOnAction(event -> onModeSelected.accept(mode));
        return button;
    }

    /**
     * Creates an action button.
     */
    private Button createActionButton(
            String text,
            String styleClass
    ) {
        Button button = new Button(text);

        button.getStyleClass().addAll("menu-action", styleClass);
        button.setMinWidth(150);
        button.setPrefHeight(46);
        button.setMaxWidth(230);

        return button;
    }

    /**
     * Loads an image from resources.
     */
    private ImageView createImage(
            String filename,
            boolean backgroundImage
    ) {

        String path = RESOURCE_ROOT + filename;

        InputStream stream = getClass().getResourceAsStream(path);
        ImageView view = new ImageView();
        if (stream == null) {
            System.err.println("No se encontró el recurso: " + path);
            return view;
        }
        Image image = new Image(stream);
        if (image.isError()) {
            System.err.println("Error cargando imagen: " + path);
            return view;
        }
        view.setImage(image);
        view.setSmooth(true);
        view.setPreserveRatio(!backgroundImage);
        return view;
    }

    /**
     * Loads the stylesheet.
     */
    private void loadStyles() {
        var css = getClass().getResource("/resources/css/styles.css"
        );
        if (css != null) {
            getStylesheets().add(css.toExternalForm());
            System.out.println("CSS cargado correctamente");
        } else {
            System.err.println("No se encontró styles.css");
        }
        getStyleClass().add("main-menu");
    }
}