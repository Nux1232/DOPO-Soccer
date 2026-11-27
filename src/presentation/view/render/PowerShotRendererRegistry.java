package presentation.view.render;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Registro extensible de renderizadores de tiros especiales indexados por su ID estable. */
public final class PowerShotRendererRegistry {
    private final Map<String, PowerShotRenderer> renderers = new LinkedHashMap<>();

    /** Registra el renderizador para un ID de poder. */
    public synchronized void register(String powerId, PowerShotRenderer renderer) {
        if (powerId == null || powerId.isBlank() || renderer == null) {
            throw new IllegalArgumentException("El ID y el renderizador del tiro son obligatorios.");
        }
        String key = normalize(powerId);
        if (renderers.putIfAbsent(key, renderer) != null) {
            throw new IllegalArgumentException("Ya existe un renderizador para el poder " + key + ".");
        }
    }

    /** Busca un renderizador personalizado por ID. */
    public synchronized PowerShotRenderer find(String powerId) {
        return powerId == null ? null : renderers.get(normalize(powerId));
    }

    private static String normalize(String powerId) {
        return powerId.trim().toLowerCase(Locale.ROOT);
    }
}
