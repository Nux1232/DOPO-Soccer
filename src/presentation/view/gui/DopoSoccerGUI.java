package presentation.view.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import domain.model.game.GameMatchConfig;
import presentation.view.util.ViewNavigator;

/**
 * Punto de entrada de la aplicación gráfica de DOPOSoccer.
 *
 * Extiende Application, la clase base de JavaFX para programas con
 * ventana. JavaFX crea la ventana principal Stage y llama a
 * start(Stage), ahí se prepara la configuración del partido y se
 * muestra el menú principal.
 */
public class DopoSoccerGUI extends Application {

    /**
     * Arranca la interfaz: crea una configuración de partido vacía (con los
     * personajes por defecto), le entrega la ventana principal al
     * ViewNavigator y muestra el menú principal.
     *
     * JavaFX llama a este método automáticamente después de
     * launch, no se invoca a mano.
     *
     * @param primaryStage ventana principal que crea JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        GameMatchConfig matchConfig = new GameMatchConfig();
        ViewNavigator.init(primaryStage);
        ViewNavigator.loadView(new MainMenuPane(matchConfig));
    }

    /**
     * Método principal de Java: inicia JavaFX, que a su vez llama a
     * start(Stage).
     */
    public static void main(String[] args) {
        launch(args);
    }
}