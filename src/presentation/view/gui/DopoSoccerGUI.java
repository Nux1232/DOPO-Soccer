
package presentation.view.gui;

import domain.model.game.GameMatchConfig;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import presentation.view.util.ViewNavigator;

import java.util.Objects;

/**
 * Punto de entrada de DOPO-SOCCER.
 */
public class DopoSoccerGUI extends Application {

    private static final String APP_TITLE = "DOPO-SOCCER";

    private static final String APP_ICON = "/resources/Images/dopo_soccer_icon.png";

    @Override
    public void start(Stage primaryStage) {

        // Nombre de la aplicación en la barra de Windows.
        primaryStage.setTitle(APP_TITLE);

        // Icono de la ventana y de la aplicación.
        Image applicationIcon = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(APP_ICON),
                        "No se encontró el icono de DOPO-SOCCER"));
        primaryStage.getIcons().add(applicationIcon);
        // Tamaño mínimo para evitar que los componentes se compriman.
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        GameMatchConfig matchConfig = new GameMatchConfig();
        ViewNavigator.init(primaryStage);
        ViewNavigator.loadView(new MainMenuPane(matchConfig)
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
