package presentation.view.util;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import presentation.view.gui.GamePlayPane;
import javafx.stage.Stage;

/**
 * Cambia entre las pantallas (vistas) de la aplicación.
 *
 * Guarda la ventana principal y ofrece un único método para mostrar una
 * pantalla nueva. Todas las pantallas usan esta clase para navegar, de modo
 * que ninguna necesita conocer la ventana directamente.
 *
 * Es una clase de utilidad con estado compartido: la ventana se guarda en
 * un atributo static, por lo que hay que llamar a
 * init(Stage) antes de loadView(Pane).
 */
public class ViewNavigator {

    /** Ventana principal de la aplicación; es null hasta llamar a #init(Stage). */
    private static Stage mainStage;

    /**
     * Guarda la ventana principal para usarla después al cambiar de pantalla.
     *
     * @param stage ventana principal de JavaFX
     */
    public static void init(Stage stage) {
        mainStage = stage;
    }

    /**
     * Muestra una pantalla en la ventana principal.
     *
     * Si la ventana todavía no tiene escena, crea una de 900 por 650
     * con la pantalla como contenido. Si ya la tiene, reemplaza el contenido
     * de la escena, y si la pantalla que se está cerrando es el juego
     * GamePlayPane, antes detiene su ciclo de actualización para
     * que no siga corriendo en segundo plano. Al final muestra la ventana y
     * le da el foco a la pantalla nueva para que reciba el teclado.
     *
     * @param nextViewPane pantalla que se va a mostrar
     */
    public static void loadView(Pane nextViewPane) {
        if (mainStage.getScene() == null) {
            mainStage.setScene(new Scene(nextViewPane, 900, 650));
        } else {
            if (mainStage.getScene().getRoot() instanceof GamePlayPane gamePlayPane) {
                gamePlayPane.stop();
            }
            mainStage.getScene().setRoot(nextViewPane);
        }
        mainStage.show();
        nextViewPane.requestFocus();
    }
}