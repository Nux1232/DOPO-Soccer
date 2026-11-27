package presentation.view.effects;

import presentation.model.GameSnapshot.PlayerSnapshot;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import presentation.view.render.PlayerVisualMetrics;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Capa superpuesta de JavaFX encargada de posicionar y mostrar animaciones GIF
 * de efectos visuales (aura, aturdimiento y estados alterados) sobre ambos jugadores.
 */
public final class PlayerEffectsLayer extends Pane {
    private final PlayerEffects firstEffects;
    private final PlayerEffects secondEffects;
    private final Set<String> missingOptionalResources = new HashSet<>();

    /**
     * Crea la capa de efectos cargando los recursos animados configurados para ambos jugadores.
     */
    public PlayerEffectsLayer() {
        this(PlayerEffectCatalog.defaults());
    }

    /**
     * Crea la capa usando un catálogo de efectos configurable por la aplicación.
     *
     * @param catalog estilos de efectos temporales disponibles
     */
    public PlayerEffectsLayer(PlayerEffectCatalog catalog) {
        if (catalog == null) throw new IllegalArgumentException("El catálogo visual es obligatorio.");
        firstEffects = createPlayerEffects(catalog.styles());
        secondEffects = createPlayerEffects(catalog.styles());
        getChildren().addAll(firstEffects.views());
        getChildren().addAll(secondEffects.views());
        setMouseTransparent(true);
        setPickOnBounds(false);
    }

    private PlayerEffects createPlayerEffects(Map<String, PlayerEffectCatalog.EffectStyle> statusStyles) {
        List<EffectStyle> styles = new ArrayList<>(List.of(
                new EffectStyle("/resources/effects/chakra-energy-charge.gif", 2.4, 1.9,
                        PlayerVisualMetrics.CENTER_OFFSET_Y, PlayerSnapshot::auraActive, true),
                new EffectStyle("/resources/effects/stun-stars.gif", 1.7, 0.9,
                        -PlayerVisualMetrics.TOP_OFFSET - PlayerVisualMetrics.HEIGHT * 0.15,
                        PlayerSnapshot::stunned, true)));
        statusStyles.values().forEach(style -> styles.add(new EffectStyle(
                style.resourcePath(), style.widthMultiplier(), style.heightMultiplier(),
                style.centerOffsetY(),
                player -> style.effectId().equalsIgnoreCase(player.statusEffect()), false)));
        List<EffectView> effects = styles.stream()
                .map(style -> new EffectView(style, createEffect(style)))
                .filter(effect -> effect.view() != null).toList();
        return new PlayerEffects(effects);
    }

    private ImageView createEffect(EffectStyle style) {
        var resource = getClass().getResource(style.resourcePath());
        if (resource == null) {
            if (!style.required() && missingOptionalResources.add(style.resourcePath()))
                System.err.println("Recurso visual opcional no encontrado: " + style.resourcePath());
            if (style.required()) throw new IllegalStateException("No se encontró el GIF: " + style.resourcePath());
            return null;
        }
        Image image = new Image(resource.toExternalForm());
        if (image.isError()) throw new IllegalStateException("No se pudo cargar el GIF: " + style.resourcePath(), image.getException());
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true); view.setSmooth(true); view.setVisible(false); view.setMouseTransparent(true);
        return view;
    }

    /**
     * Actualiza la posición, escala y visibilidad de los efectos de ambos jugadores.
     *
     * @param first instantánea del primer jugador
     * @param second instantánea del segundo jugador
     * @param scale factor de escala de la cancha en pantalla
     * @param offsetX desplazamiento horizontal de centrado
     * @param offsetY desplazamiento vertical de centrado
     */
    public void update(PlayerSnapshot first, PlayerSnapshot second, double scale, double offsetX, double offsetY) {
        updatePlayerEffects(first, firstEffects, scale, offsetX, offsetY);
        updatePlayerEffects(second, secondEffects, scale, offsetX, offsetY);
    }

    private void updatePlayerEffects(PlayerSnapshot player, PlayerEffects effects, double scale, double offsetX, double offsetY) {
        double screenX = offsetX + player.x() * scale;
        double screenY = offsetY + player.y() * scale;
        for (EffectView effect : effects.effects())
            updateEffect(effect.view(), effect.style(), effect.style().activeWhen().test(player), screenX, screenY, scale);
    }

    private void updateEffect(ImageView view, EffectStyle style, boolean visible, double screenX, double screenY, double scale) {
        view.setVisible(visible);
        if (!visible) return;
        double targetWidth = PlayerVisualMetrics.WIDTH * style.widthMultiplier() * scale;
        double targetHeight = PlayerVisualMetrics.HEIGHT * style.heightMultiplier() * scale;
        double imageRatio = view.getImage().getWidth() / view.getImage().getHeight();
        double targetRatio = targetWidth / targetHeight;
        double imageWidth = targetWidth, imageHeight = targetHeight;
        if (imageRatio > targetRatio) imageHeight = targetWidth / imageRatio;
        else imageWidth = targetHeight * imageRatio;
        view.setFitWidth(imageWidth); view.setFitHeight(imageHeight);
        double centerX = screenX, centerY = screenY + style.centerOffsetY() * scale;
        view.relocate(centerX - imageWidth / 2, centerY - imageHeight / 2);
    }

    private record EffectStyle(String resourcePath, double widthMultiplier, double heightMultiplier,
            double centerOffsetY, Predicate<PlayerSnapshot> activeWhen, boolean required) { }
    private record EffectView(EffectStyle style, ImageView view) { }
    private record PlayerEffects(List<EffectView> effects) {
        private List<ImageView> views() { return effects.stream().map(EffectView::view).toList(); }
    }
}
