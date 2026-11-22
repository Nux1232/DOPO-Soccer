
package presentation.view.gui;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import java.util.function.Consumer;

public class MainMenuPane extends StackPane {

    private static final String IMAGES_PATH = "/resources/Images/";

    private final Consumer<String> onModeSelected;
    private final Runnable onLoadMatch;

    public MainMenuPane(Consumer<String> onModeSelected, Runnable onLoadMatch) {
        this.onModeSelected = onModeSelected;
        this.onLoadMatch = onLoadMatch;
        setupUI();
    }

    private void setupUI() {

        String cssPath = getClass().getResource("/resources/css/styles.css").toExternalForm();

        this.getStylesheets().add(cssPath);
        this.getStyleClass().add("root-pane");

        // Fondo del menú
        Image bgImage = new Image(getClass().getResourceAsStream(IMAGES_PATH + "background.png"));

        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(this.widthProperty());
        bgView.fitHeightProperty().bind(this.heightProperty());

        // Título
        Text title = new Text("DOPO-SOCCER");
        title.getStyleClass().add("game-title");

        Text subtitle = new Text("ELIGE TU PARTIDO");
        subtitle.getStyleClass().add("subtitle");

        // Jugador vs Jugador
        Image pvpImage = new Image(getClass().getResourceAsStream(IMAGES_PATH + "pvp_banner.png"));

        ImageView pvpGraphic = new ImageView(pvpImage);
        pvpGraphic.setFitWidth(320);
        pvpGraphic.setFitHeight(190);
        pvpGraphic.setPreserveRatio(true);

        Button pvpButton = new Button();
        pvpButton.setGraphic(pvpGraphic);
        pvpButton.getStyleClass().add("mode-card");
        pvpButton.setPrefSize(340, 220);

        pvpButton.setOnAction(e ->
                handleSelectMode("PLAYER_VS_PLAYER")
        );

        // Jugador vs Máquina
        Image pvAiImage = new Image(getClass().getResourceAsStream(IMAGES_PATH + "pvai_banner.png"));

        ImageView pvAiGraphic = new ImageView(pvAiImage);
        pvAiGraphic.setFitWidth(320);
        pvAiGraphic.setFitHeight(190);
        pvAiGraphic.setPreserveRatio(true);

        Button pvAiButton = new Button();
        pvAiButton.setGraphic(pvAiGraphic);
        pvAiButton.getStyleClass().add("mode-card");
        pvAiButton.setPrefSize(340, 220);

        // El modo contra máquina ya está disponible.
        pvAiButton.setDisable(false);

        pvAiButton.setOnAction(e -> handleSelectMode("PLAYER_VS_MACHINE"));

        VBox pvpCard = new VBox(10, pvpButton, new Label("DOS JUGADORES · MISMO TECLADO"));
        pvpCard.setAlignment(Pos.CENTER);
        pvpCard.getStyleClass().add("mode-caption");
        VBox aiCard = new VBox(10, pvAiButton, new Label("4 PERFILES DE DIFICULTAD"));
        aiCard.setAlignment(Pos.CENTER);
        aiCard.getStyleClass().add("mode-caption");
        HBox modeContainer = new HBox(28, pvpCard, aiCard);
        modeContainer.setAlignment(Pos.CENTER);

        // Salir
        Button loadButton = new Button("CARGAR PARTIDA");
        loadButton.getStyleClass().add("nav-button");
        loadButton.setDisable(false);
        loadButton.setOnAction(e -> onLoadMatch.run());

        Button exitButton = new Button("SALIR");
        exitButton.getStyleClass().add("nav-button");
        exitButton.setOnAction(e -> System.exit(0));

        HBox footerContainer = new HBox(
                20,
                loadButton,
                exitButton
        );

        footerContainer.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(
                18,
                title,
                subtitle,
                modeContainer,
                footerContainer
        );

        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(Region.USE_PREF_SIZE);
        contentBox.setStyle("-fx-padding: 24;");

        this.getChildren().addAll(bgView, contentBox);
    }

    private void handleSelectMode(String mode) {

        onModeSelected.accept(mode);
    }

}
