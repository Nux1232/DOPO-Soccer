package presentation.view.gui;

import domain.model.game.GameMatchConfig;
import domain.model.game.PlayerMode;
import presentation.view.util.ViewNavigator;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MainMenuPane extends StackPane {

    private static final String IMAGES_PATH = "/resources/Images/";
    private final GameMatchConfig matchConfig;

    public MainMenuPane(GameMatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        setupUI();
    }

    private void setupUI() {
        String cssPath = getClass().getResource("/resources/css/styles.css").toExternalForm();
        this.getStylesheets().add(cssPath);
        this.getStyleClass().add("root-pane");

        Image bgImage = new Image(getClass().getResourceAsStream(IMAGES_PATH + "background.png"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(this.widthProperty());
        bgView.fitHeightProperty().bind(this.heightProperty());

        Text title = new Text("DOPO SOCCER");
        title.getStyleClass().add("game-title");

        Text subtitle = new Text("SELECCIONA MODO DE JUEGO");
        subtitle.getStyleClass().add("subtitle");

        Image pvpImage = new Image(getClass().getResourceAsStream(IMAGES_PATH + "pvp_banner.png"));
        ImageView pvpGraphic = new ImageView(pvpImage);
        pvpGraphic.setFitWidth(240);
        pvpGraphic.setFitHeight(160);
        pvpGraphic.setPreserveRatio(true);

        Button pvpButton = new Button();
        pvpButton.setGraphic(pvpGraphic);
        pvpButton.getStyleClass().add("card-button-pvp");
        pvpButton.setOnAction(e -> handleSelectMode(PlayerMode.PLAYER_VS_PLAYER));

        Image pvAiImage = new Image(getClass().getResourceAsStream(IMAGES_PATH + "pvai_banner.png"));
        ImageView pvAiGraphic = new ImageView(pvAiImage);
        pvAiGraphic.setFitWidth(240);
        pvAiGraphic.setFitHeight(160);
        pvAiGraphic.setPreserveRatio(true);

        Button pvAiButton = new Button();
        pvAiButton.setGraphic(pvAiGraphic);
        pvAiButton.getStyleClass().add("card-button-disabled");
        pvAiButton.setDisable(true);

        HBox modeContainer = new HBox(30, pvpButton, pvAiButton);
        modeContainer.setAlignment(Pos.CENTER);

        Button optionsButton = new Button("OPCIONES");
        optionsButton.getStyleClass().add("nav-button");

        Button exitButton = new Button("SALIR");
        exitButton.getStyleClass().add("nav-button");
        exitButton.setOnAction(e -> System.exit(0));

        HBox footerContainer = new HBox(20, optionsButton, exitButton);
        footerContainer.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(25, title, subtitle, modeContainer, footerContainer);
        contentBox.setAlignment(Pos.CENTER);

        this.getChildren().addAll(bgView, contentBox);
    }

    private void handleSelectMode(PlayerMode mode) {
        matchConfig.setGameMode(mode);
        ViewNavigator.loadView(new CharacterSelectionPane(matchConfig));
    }
}