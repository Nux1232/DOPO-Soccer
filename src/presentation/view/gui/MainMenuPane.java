
package presentation.view.gui;

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
 * Main menu.
 * This class only manages presentation and navigation callbacks.
 */
public final class MainMenuPane extends StackPane {

    private static final String RESOURCE_ROOT = "/resources/Images/";
    private final Consumer<String> onModeSelected;
    private final Runnable onLoadMatch;

    public MainMenuPane(Consumer<String> onModeSelected, Runnable onLoadMatch) {
        this.onModeSelected = onModeSelected;
        this.onLoadMatch = onLoadMatch;
        build();
    }

    private void build() {
        loadStyles();
        ImageView background = createImage("background.png", true);
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("menu-overlay");

        Text title = new Text("DOPO-SOCCER");
        title.getStyleClass().add("menu-title");

        Label subtitle = new Label("ELIGE CÓMO QUIERES JUGAR");
        subtitle.getStyleClass().add("menu-subtitle");

        Label description = new Label(
                "Fútbol, poderes especiales y caos en la cancha"
        );
        description.getStyleClass().add("menu-description");

        VBox header = new VBox(10, title, subtitle, description);
        header.setAlignment(Pos.CENTER);

        Button playerVsPlayer = createModeButton(
                "Jugador vs Jugador",
                "DOS JUGADORES · MISMO TECLADO",
                "pvp_banner.png",
                "PLAYER_VS_PLAYER"
        );

        Button playerVsMachine = createModeButton(
                "Jugador vs Máquina",
                "4 PERFILES DE INTELIGENCIA",
                "pvai_banner.png",
                "PLAYER_VS_MACHINE"
        );

        HBox gameModes = new HBox(30, playerVsPlayer, playerVsMachine);
        gameModes.setAlignment(Pos.CENTER);

        Button loadButton = createActionButton(
                "CARGAR PARTIDA",
                "load-button"
        );

        loadButton.setOnAction(event -> onLoadMatch.run());

        Button exitButton = createActionButton(
                "SALIR",
                "exit-button"
        );

        exitButton.setOnAction(event -> System.exit(0));

        HBox actions = new HBox(15, loadButton, exitButton);
        actions.setAlignment(Pos.CENTER);

        VBox content = new VBox(25, header, gameModes, actions);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(25, 35, 25, 35));
        content.setMaxWidth(Region.USE_PREF_SIZE);
        content.getStyleClass().add("menu-content");

        getChildren().addAll(background, overlay, content);
    }

    private Button createModeButton(
            String title,
            String description,
            String imageName,
            String mode
    ) {
        ImageView artwork = createImage(imageName, false);

        artwork.setFitWidth(300);
        artwork.setFitHeight(190);
        artwork.setPreserveRatio(true);

        artwork.getStyleClass().add("mode-artwork");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("mode-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("mode-description");

        VBox cardContent = new VBox(
                12,
                artwork,
                titleLabel,
                descriptionLabel
        );

        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(20));

        Button button = new Button();
        button.setGraphic(cardContent);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        button.setMinWidth(340);
        button.setPrefWidth(340);
        button.setMinHeight(285);
        button.setPrefHeight(285);

        button.getStyleClass().add("mode-card");

        button.setOnAction(event -> onModeSelected.accept(mode));

        return button;
    }

    private Button createActionButton(String text, String styleClass) {
        Button button = new Button(text);

        button.getStyleClass().addAll(
                "menu-action",
                styleClass
        );

        button.setMinWidth(170);
        button.setPrefHeight(48);

        return button;
    }

    private ImageView createImage(String filename, boolean background) {

        String path = RESOURCE_ROOT + filename;

        InputStream stream = getClass().getResourceAsStream(path);

        ImageView imageView = new ImageView();

        if (stream == null) {
            System.err.println("No se encontró el recurso: " + path);
            return imageView;
        }

        Image image = new Image(stream);

        if (image.isError()) {
            System.err.println("Error cargando imagen: " + path);
            return imageView;
        }

        imageView.setImage(image);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(!background);

        return imageView;
    }

    private void loadStyles() {

        var css = getClass().getResource("/resources/css/styles.css");

        if (css != null) {
            getStylesheets().add(css.toExternalForm());
        }

        getStyleClass().add("main-menu");
    }
}
