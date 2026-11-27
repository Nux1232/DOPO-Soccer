package domain.model.entity;

import domain.model.exception.DopoSoccerException;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de fábricas que convierten configuración externa en poderes del dominio.
 * Cada aplicación puede crear un registro independiente y cargar su catálogo con él.
 */
public final class PowerFactoryRegistry {
    private static final PowerFactoryRegistry DEFAULT = createDefault();

    private final Map<String, PowerFactory> factories = new HashMap<>();

    /** Crea un registro aislado, útil para aplicaciones y pruebas independientes. */
    public PowerFactoryRegistry() { }

    /** @return registro compartido que usa el catálogo predeterminado */
    public static PowerFactoryRegistry defaultRegistry() {
        return DEFAULT;
    }

    /** @return registro nuevo que incluye las fábricas integradas */
    public static PowerFactoryRegistry withDefaults() {
        return createDefault();
    }

    /**
     * Registra un constructor para el tipo de poder declarado por el catálogo.
     *
     * @param type ID del tipo configurado, por ejemplo {@code impact}
     * @param factory conversor de propiedades a poder
     */
    public synchronized void register(String type, PowerFactory factory) {
        if (type == null || type.isBlank() || factory == null) {
            throw new DopoSoccerException("El tipo y la fábrica de poder son obligatorios.");
        }
        String normalized = normalize(type);
        if (factories.putIfAbsent(normalized, factory) != null) {
            throw new DopoSoccerException("Ya existe una fábrica registrada para el poder " + normalized + ".");
        }
    }

    /**
     * Construye un poder a partir de su tipo y propiedades.
     *
     * @param type tipo configurado en el país
     * @param configuration propiedades del poder
     * @return poder creado
     */
    public synchronized Power create(String type, Map<String, String> configuration) {
        PowerFactory factory = factories.get(normalize(type));
        if (factory == null) {
            throw new DopoSoccerException("No existe una fábrica registrada para el poder " + type + ".");
        }
        Power power = factory.create(Map.copyOf(configuration));
        if (power == null) {
            throw new DopoSoccerException("La fábrica del poder " + type + " devolvió null.");
        }
        return power;
    }

    private static PowerFactoryRegistry createDefault() {
        PowerFactoryRegistry registry = new PowerFactoryRegistry();
        registry.register("impact", configuration -> new ImpactPower(
                required(configuration, "id"),
                required(configuration, "asset"),
                required(configuration, "color"),
                decimal(configuration, "duration"),
                decimal(configuration, "damage"),
                decimal(configuration, "speedX"),
                decimal(configuration, "speedY"),
                PlayerStatusEffect.valueOf(required(configuration, "effect")),
                decimal(configuration, "restitution"),
                decimal(configuration, "knockbackX"),
                decimal(configuration, "knockbackY"),
                decimal(configuration, "knockbackSeconds"),
                Boolean.parseBoolean(required(configuration, "renderCopies"))));
        return registry;
    }

    private static String required(Map<String, String> configuration, String key) {
        String value = configuration.get(key);
        if (value == null || value.isBlank()) {
            throw new DopoSoccerException("Falta configuración de poder: " + key);
        }
        return value.trim();
    }

    private static double decimal(Map<String, String> configuration, String key) {
        try {
            return Double.parseDouble(required(configuration, key));
        } catch (NumberFormatException exception) {
            throw new DopoSoccerException("La propiedad " + key + " debe ser numérica.", exception);
        }
    }

    private static String normalize(String type) {
        if (type == null || type.isBlank()) {
            throw new DopoSoccerException("El tipo de poder es obligatorio.");
        }
        return type.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /** Constructor de un poder a partir de una configuración externa validada. */
    @FunctionalInterface
    public interface PowerFactory {
        /** @param configuration propiedades inmutables del poder */
        Power create(Map<String, String> configuration);
    }
}
