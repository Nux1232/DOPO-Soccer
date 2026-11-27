package presentation.view.effects;

import presentation.view.render.PlayerVisualMetrics;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Registro de recursos y geometría de efectos visuales por identificador de estado. */
public final class PlayerEffectCatalog {
    private final Map<String, EffectStyle> styles = new LinkedHashMap<>();

    /** Crea un catálogo sin efectos de estado registrados. */
    public PlayerEffectCatalog() { }

    /** @return catálogo con los efectos visuales integrados */
    public static PlayerEffectCatalog defaults() {
        PlayerEffectCatalog catalog = new PlayerEffectCatalog();
        catalog.register("PUSHED", "/resources/effects/powers/pushed.gif", 1.9, 1.8, 0);
        catalog.register("ELECTRIFIED", "/resources/effects/powers/electrified.gif", 1.7, 1.7, 0);
        catalog.register("UNCONSCIOUS", "/resources/effects/powers/unconscious.gif",
                1.7, 0.9, -PlayerVisualMetrics.TOP_OFFSET - PlayerVisualMetrics.HEIGHT * 0.15);
        catalog.register("CONFUSED", "/resources/effects/powers/confused.gif",
                1.7, 0.9, -PlayerVisualMetrics.TOP_OFFSET - PlayerVisualMetrics.HEIGHT * 0.15);
        return catalog;
    }

    /**
     * Registra la animación que representa un efecto de estado durante el partido.
     *
     * @param effectId identificador textual expuesto por el snapshot del jugador
     * @param resourcePath ruta absoluta del GIF o recurso JavaFX
     * @param widthMultiplier ancho relativo al jugador
     * @param heightMultiplier alto relativo al jugador
     * @param centerOffsetY desplazamiento vertical respecto al centro del jugador
     */
    public synchronized void register(
            String effectId, String resourcePath, double widthMultiplier,
            double heightMultiplier, double centerOffsetY) {
        if (effectId == null || effectId.isBlank() || resourcePath == null || resourcePath.isBlank()
                || !Double.isFinite(widthMultiplier) || widthMultiplier <= 0
                || !Double.isFinite(heightMultiplier) || heightMultiplier <= 0
                || !Double.isFinite(centerOffsetY)) {
            throw new IllegalArgumentException("La definición del efecto visual es inválida.");
        }
        String key = normalize(effectId);
        if (styles.putIfAbsent(key,
                new EffectStyle(key, resourcePath, widthMultiplier, heightMultiplier, centerOffsetY)) != null) {
            throw new IllegalArgumentException("Ya existe un efecto visual registrado para " + key + ".");
        }
    }

    /** @return estilos registrados en una copia inmutable */
    public synchronized Map<String, EffectStyle> styles() {
        return Map.copyOf(styles);
    }

    private static String normalize(String effectId) {
        return effectId.trim().toUpperCase(Locale.ROOT);
    }

    /** Metadatos de presentación de una animación de estado. */
    public record EffectStyle(
            String effectId,
            String resourcePath,
            double widthMultiplier,
            double heightMultiplier,
            double centerOffsetY) { }
}
