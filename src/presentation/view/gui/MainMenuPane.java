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

/**
 * Pantalla del menú principal, donde se elige la modalidad de juego.
 *
 * Es un StackPane: apila capas una sobre otra. La capa de abajo
 * es la imagen de fondo y la de arriba es el contenido (título, tarjetas
 * de modalidad y botones de navegación).
 *
 * Por ahora solo está habilitada la tarjeta "Jugador vs Jugador"; la de
 * jugador contra máquina aparece deshabilitada. Al elegir una modalidad se
 * guarda en la configuración del partido y se pasa a la selección de
 * personajes.
 */
public class MainMenuPane extends StackPane {

    /** Configuración del partido que se va llenando a lo largo de las pantallas. */
    private final GameMatchConfig matchConfig;

    /**
     * Crea el menú principal y arma su contenido.
     *
     * @param matchConfig configuración compartida donde se guardará la
     *        modalidad elegida
     */
    public MainMenuPane(GameMatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        setupUI();
    }

    /**
     * Construye y acomoda todos los elementos visuales del menú.
     *
     * Pasos: carga la hoja de estilos CSS, coloca la imagen de fondo
     * (ajustada al tamaño de la ventana), crea el título y el subtítulo,
     * crea las dos tarjetas de modalidad (la de máquina deshabilitada), crea
     * los botones "OPCIONES" y "SALIR" y los ordena verticalmente sobre el
     * fondo. El botón "OPCIONES" todavía no tiene acción; "SALIR" cierra
     * el programa.
     */
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
        Image pvpImage = new Image(getClass().getResourceAsStream("/resources/Images/pvp_banner.png"));
        ImageView pvpGraphic = new ImageView(pvpImage);
        pvpGraphic.setFitWidth(240);
        pvpGraphic.setFitHeight(160);
        pvpGraphic.setPreserveRatio(true);

        Button pvpButton = new Button();
        pvpButton.setGraphic(pvpGraphic); // Inserta la ilustración dentro del botón
        pvpButton.getStyleClass().add("image-card-button");
        pvpButton.setOnAction(e -> handleSelectMode(GameMode.PvP));

        Image pvAiImage = new Image(getClass().getResourceAsStream("/resources/images/pvai_banner.png"));
        ImageView pvAiGraphic = new ImageView(pvAiImage);
        pvAiGraphic.setFitWidth(240);
        pvAiGraphic.setFitHeight(160);
        pvAiGraphic.setPreserveRatio(true);

        Button pvAiButton = new Button();
        pvAiButton.setGraphic(pvAiGraphic);
        pvAiButton.getStyleClass().add("image-card-button-disabled");
        pvAiButton.setDisable(true); // Deshabilitado

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

    /**
     * Guarda la modalidad elegida en la configuración y pasa a la pantalla
     * de selección de personajes.
     *
     * @param mode modalidad que eligió el usuario
     */
    private void handleSelectMode(GameMode mode) {
        matchConfig.setGameMode(mode);
        ViewNavigator.loadView(new CharacterSelectionPane(matchConfig));
    }
}