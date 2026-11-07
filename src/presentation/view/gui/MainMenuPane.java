package presentation.view.gui;

import domain.model.game.GameMatchConfig;
import domain.model.game.GameMode;
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
    private final GameMatchConfig matchConfig;

    public MainMenuPane(GameMatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        setupUI();
    }

    private void setupUI() {
        // 1. Cargar hoja de estilos CSS
        String cssPath = getClass().getResource("/resources/css/styles.css").toExternalForm();
        this.getStylesheets().add(cssPath);
        this.getStyleClass().add("root-pane");

        // 2. Fondo de pantalla (ImageView escalable)
        Image bgImage = new Image(getClass().getResourceAsStream("/resources/Images/background.png"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(this.widthProperty());
        bgView.fitHeightProperty().bind(this.heightProperty());

        // 3. Títulos principales
        Text title = new Text("DOPO SOCCER");
        title.getStyleClass().add("game-title");

        Text subtitle = new Text("SELECCIONA MODO DE JUEGO");
        subtitle.getStyleClass().add("subtitle");

        // 4. Tarjetas de Modo de Juego
        Button pvpButton = new Button("JUGADOR VS JUGADOR");
        pvpButton.getStyleClass().add("card-button-pvp");
        pvpButton.setPrefSize(280, 180);
        pvpButton.setOnAction(e -> handleSelectMode(GameMode.PvP));

        Button pvAiButton = new Button("JUGADOR VS MÁQUINA\n\n🔒 (EN CONSTRUCCIÓN)");
        pvAiButton.getStyleClass().add("card-button-disabled");
        pvAiButton.setPrefSize(280, 180);
        pvAiButton.setDisable(true); // Deshabilitado temporalmente

        HBox modeContainer = new HBox(30, pvpButton, pvAiButton);
        modeContainer.setAlignment(Pos.CENTER);

        // 5. Botones inferiores de navegación
        Button optionsButton = new Button("⚙ OPCIONES");
        optionsButton.getStyleClass().add("nav-button");

        Button exitButton = new Button("SALIR");
        exitButton.getStyleClass().add("nav-button");
        exitButton.setOnAction(e -> System.exit(0));

        HBox footerContainer = new HBox(20, optionsButton, exitButton);
        footerContainer.setAlignment(Pos.CENTER);

        // 6. Envoltorio principal para ordenar elementos verticalmente
        VBox contentBox = new VBox(25, title, subtitle, modeContainer, footerContainer);
        contentBox.setAlignment(Pos.CENTER);

        // Capa 1: Fondo | Capa 2: Contenido UI
        this.getChildren().addAll(bgView, contentBox);
    }

    private void handleSelectMode(GameMode mode) {
        matchConfig.setGameMode(mode);
        ViewNavigator.loadView(new CharacterSelectionPane(matchConfig));
    }
}