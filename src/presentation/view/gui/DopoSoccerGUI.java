package presentation.view.gui;

import domain.DopoSoccer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.Objects;
import presentation.model.GameSnapshot;
import presentation.model.GameSnapshot.PlayerSnapshot;
import presentation.model.PlayerControls;
import presentation.model.MatchSetup;

/** Punto de composición y coordinador de presentación. Único puente hacia el dominio. */
public class DopoSoccerGUI extends Application {
    private static final String APP_TITLE = "DOPO-SOCCER";
    private static final String APP_ICON = "/resources/Images/dopo_soccer_icon.png";
    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 650;

    private Stage primaryStage;
    private DopoSoccer dopoSoccer;
    private MatchSetup matchConfig;
    private GamePlayPane activeGamePane;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        dopoSoccer = new DopoSoccer();
        matchConfig = null;
        stage.setTitle(APP_TITLE);
        Image icon = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(APP_ICON), "No se encontró el icono de DOPO-SOCCER"));
        stage.getIcons().add(icon);
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        showMainMenu();
    }

    private void showMainMenu() {
        if (dopoSoccer != null) dopoSoccer.reset();
        navigate(new MainMenuPane(this::showCharacterSelection));
    }

    private void showCharacterSelection(String mode) {
        matchConfig = new MatchSetup(mode);
        navigate(new CharacterSelectionPane(matchConfig, this::startMatch, this::showMainMenu));
    }

    private void startMatch(MatchSetup config) {
        try {
            dopoSoccer.startMatch(new DopoSoccer.MatchRequest(
                    DopoSoccer.PlayerModeKind.valueOf(config.playerMode()),
                    DopoSoccer.MatchKind.valueOf(config.type().name()),
                    config.firstCountry().name(),
                    config.secondCountry().name(),
                    config.goalLimit(), config.durationSeconds()));
            activeGamePane = new GamePlayPane(
                    toPresentationSnapshot(dopoSoccer.snapshot()),
                    (first, second, delta) -> {
                        dopoSoccer.updateMatch(toDomainActions(first), toDomainActions(second), delta);
                        return toPresentationSnapshot(dopoSoccer.snapshot());
                    },
                    this::showMainMenu);
            navigate(activeGamePane);
        } catch (Exception exception) {
            showError("No se pudo iniciar el partido", exception.getMessage());
        }
    }


    private DopoSoccer.PlayerActions toDomainActions(PlayerControls controls) {
        return new DopoSoccer.PlayerActions(controls.left(), controls.right(), controls.jump(), controls.kick(), controls.power());
    }

    private GameSnapshot toPresentationSnapshot(DopoSoccer.MatchSnapshot state) {
        return new GameSnapshot(state.width(), state.height(), state.groundY(), state.modeDescription(),
                state.finished(), state.winner(), toPresentationPlayer(state.firstPlayer()),
                toPresentationPlayer(state.secondPlayer()), state.ballX(), state.ballY(), state.ballRadius(),
                state.activePowerShot(), state.powerShotAsset(), state.powerShotColor(),
                state.powerShotDuration(), state.powerShotElapsed());
    }

    private PlayerSnapshot toPresentationPlayer(DopoSoccer.PlayerSnapshot player) {
        return new PlayerSnapshot(player.countryName(), player.jerseyColor(), player.x(), player.y(),
                player.goals(), player.health(), player.maxHealth(), player.powerCharge(),
                player.facingDirection(), player.kicking(), player.stunned(),
                "AURA".equals(player.power()), player.statusEffect());
    }

    private void navigate(Pane pane) {
        if (activeGamePane != null && pane != activeGamePane) {
            activeGamePane.stop();
            activeGamePane = null;
        }
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT));
        } else {
            primaryStage.getScene().setRoot(pane);
        }
        primaryStage.show();
        pane.requestFocus();
    }

    private void showError(String header, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(APP_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (activeGamePane != null) activeGamePane.stop();
    }

    public static void main(String[] args) { launch(args); }
}
