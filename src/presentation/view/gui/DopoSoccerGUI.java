package presentation.view.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import domain.model.game.GameMatchConfig;
import presentation.view.util.ViewNavigator;

public class DopoSoccerGUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameMatchConfig matchConfig = new GameMatchConfig();
        ViewNavigator.init(primaryStage);
        ViewNavigator.loadView(new MainMenuPane(matchConfig));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
