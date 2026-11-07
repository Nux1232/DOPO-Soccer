package presentation.view.util;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import presentation.view.gui.GamePlayPane;
import javafx.stage.Stage;

public class ViewNavigator {
    private static Stage mainStage;

    public static void init(Stage stage) {
        mainStage = stage;
    }

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
