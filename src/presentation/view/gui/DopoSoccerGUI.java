package presentation.view.gui;

import domain.DopoSoccer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import javafx.stage.Stage;
import presentation.model.AIProfileOption;
import presentation.model.CountryOption;
import presentation.model.GameSnapshotMapper;
import presentation.model.MatchSetup;
import presentation.model.MatchTypeOption;
import presentation.view.effects.PlayerEffectCatalog;
import presentation.view.render.PowerShotRendererRegistry;


/** Punto de composición y coordinador de presentación. Único puente hacia el dominio. */
public class DopoSoccerGUI extends Application {
    private static final String APP_TITLE = "DOPO-SOCCER";
    private static final String APP_ICON = "/resources/Images/dopo_soccer_icon.png";
    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 650;

    private Stage primaryStage;
    private DopoSoccer dopoSoccer;
    private GamePlayPane activeGamePane;
    private MatchSetup matchConfig;

    /**
     * Inicializa la ventana principal de JavaFX y muestra el menú inicial.
     *
     * @param stage escenario principal de la aplicación
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        dopoSoccer = createDomain();
        if (dopoSoccer == null) {
            throw new IllegalStateException("La fábrica de la aplicación devolvió un dominio nulo.");
        }
        stage.setTitle(APP_TITLE);
        var iconUrl = getClass().getResource(APP_ICON);
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        showMainMenu();
    }

    /**
     * Punto de composición sustituible para registrar modalidades y repositorios propios.
     * Las aplicaciones que extiendan la interfaz pueden inyectar una fachada configurada.
     *
     * @return fachada del dominio utilizada durante esta sesión
     */
    protected DopoSoccer createDomain() {
        return new DopoSoccer();
    }

    /** Punto de extensión de presentación para registrar animaciones de estados personalizadas. */
    protected PlayerEffectCatalog createPlayerEffectCatalog() {
        return PlayerEffectCatalog.defaults();
    }

    /** Punto de extensión para personalizar el dibujo de tiros especiales por ID. */
    protected PowerShotRendererRegistry createPowerShotRendererRegistry() {
        return new PowerShotRendererRegistry();
    }

    private void showMainMenu() {
        if (dopoSoccer != null) dopoSoccer.reset();
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        navigate(new MainMenuPane(this::showCharacterSelection, this::loadSavedMatch));
    }

    private void showCharacterSelection(String mode) {
        matchConfig = new MatchSetup(mode);
        List<CountryOption> countries = dopoSoccer.availableCountries().stream()
                .map(c -> new CountryOption(c.id(), c.displayName(), c.color(), c.flagAsset()))
                .toList();
        List<AIProfileOption> aiProfiles = dopoSoccer.availableAIProfiles().stream()
                .map(p -> new AIProfileOption(p.id(), p.displayName()))
                .toList();
        List<MatchTypeOption> matchTypes = dopoSoccer.availableMatchTypes().stream()
                .map(type -> new MatchTypeOption(type.id(), type.displayName(),
                        type.parameters().stream().map(parameter -> new MatchTypeOption.Parameter(
                                parameter.id(), parameter.label(), parameter.defaultValue(),
                                parameter.minimum(), parameter.maximum(), parameter.multiplier(),
                                MatchTypeOption.Type.valueOf(parameter.type()), parameter.options()))
                                .toList()))
                .toList();
        navigate(new CharacterSelectionPane(matchConfig, countries, aiProfiles, matchTypes,
                this::startMatch, this::showMainMenu, this::showUpgrades));
    }

    private void showUpgrades() {
        javafx.scene.control.ChoiceDialog<String> choice = new javafx.scene.control.ChoiceDialog<>(
                "Jugador 1 - Speed", java.util.Arrays.asList(
                "Jugador 1 - Speed", "Jugador 1 - Kick", "Jugador 1 - Jump", "Jugador 1 - Survival",
                "Jugador 2 - Speed", "Jugador 2 - Kick", "Jugador 2 - Jump", "Jugador 2 - Survival"));
        choice.setTitle("Mejoras");
        choice.setHeaderText("Mejora estadísticas con tus puntos");
        DopoSoccer.PlayerProfileSummary profile1 = dopoSoccer.loadSavedProfile("player1");
        DopoSoccer.PlayerProfileSummary profile2 = dopoSoccer.loadSavedProfile("player2");
        choice.setContentText("Puntos J1: " + profile1.points() + " | Puntos J2: " + profile2.points()
                + "\nSelecciona la mejora que quieres comprar:");
        java.util.Optional<String> selected = choice.showAndWait();
        if (!selected.isPresent()) return;
        String option = selected.get();
        String slot = option.startsWith("Jugador 1") ? "player1" : "player2";
        String stat = option.substring(option.indexOf(" - ") + 3).toLowerCase();
        try {
            DopoSoccer.PlayerProfileSummary profile = dopoSoccer.improveSavedProfile(slot, stat);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Mejora comprada");
            alert.setHeaderText("¡Estadística mejorada!");
            alert.setContentText("Puntos restantes: " + profile.points()
                    + "\nCosto de la siguiente mejora: " + profile.nextUpgradeCost());
            alert.showAndWait();
        } catch (Exception exception) {
            showError("No se pudo comprar la mejora", exception.getMessage());
        }
    }

    private void startMatch(MatchSetup config) {
        try {
            primaryStage.setMinWidth(Math.min(WINDOW_WIDTH, config.resolution().width()));
            primaryStage.setMinHeight(Math.min(WINDOW_HEIGHT, config.resolution().height()));
            primaryStage.setWidth(config.resolution().width());
            primaryStage.setHeight(config.resolution().height());
            dopoSoccer.startMatch(new DopoSoccer.MatchRequest(
                    DopoSoccer.PlayerModeKind.valueOf(config.playerMode()),
                    config.matchType(),
                    config.firstCountry(),
                    config.secondCountry(),
                    config.aiProfile(), config.parameters()));
            openMatchView();
        } catch (Exception exception) {
            showError("No se pudo iniciar el partido", exception.getMessage());
        }
    }


    private void saveMatch() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar partida DOPO-Soccer");
        chooser.setInitialFileName("partida-dopo.ser");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Partida DOPO-Soccer (*.ser)", "*.ser"));
        File file = chooser.showSaveDialog(primaryStage);
        if (file == null) return;
        try {
            dopoSoccer.saveMatch(file.toPath());
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(APP_TITLE);
            alert.setHeaderText("Partida guardada");
            alert.setContentText("Archivo:\n" + file.getAbsolutePath());
            alert.show();
        } catch (Exception exception) {
            showError("No se pudo guardar la partida", exception.getMessage());
        }
    }

    private void loadSavedMatch() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir partida DOPO-Soccer");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Partida DOPO-Soccer (*.ser)", "*.ser"));
        File file = chooser.showOpenDialog(primaryStage);
        if (file == null) return;
        try {
            dopoSoccer.loadMatch(file.toPath());
            openMatchView();
        } catch (Exception exception) {
            showError("No se pudo cargar la partida", exception.getMessage());
        }
    }

    private void openMatchView() {
        activeGamePane = new GamePlayPane(
                GameSnapshotMapper.toPresentation(dopoSoccer.snapshot()),
                (first, second, delta) -> {
                    dopoSoccer.updateMatch(
                            GameSnapshotMapper.toDomain(first), GameSnapshotMapper.toDomain(second), delta);
                    return GameSnapshotMapper.toPresentation(dopoSoccer.snapshot());
                },
                this::showMainMenu,
                this::saveMatch,
                () -> {
                    dopoSoccer.endMatch();
                    return GameSnapshotMapper.toPresentation(dopoSoccer.snapshot());
                },
                createPlayerEffectCatalog(),
                createPowerShotRendererRegistry());
        navigate(activeGamePane);
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

    /** Detiene la vista activa del partido al cerrar la aplicación. */
    @Override
    public void stop() {
        if (activeGamePane != null) activeGamePane.stop();
    }

    /**
     * Punto de entrada principal de la interfaz gráfica.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) { launch(args); }
}
