package domain.model.game;

import domain.model.exception.DopoSoccerException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Objeto de valor serializable que describe las reglas y el progreso de un partido.
 *
 * @param type identificador de la modalidad
 * @param parameters configuración serializable de esa modalidad
 * @param state estado de ejecución serializable propio de la modalidad
 */
public record MatchConfiguration(
        MatchType type,
        Map<String, MatchValue> parameters,
        Map<String, MatchValue> state) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Valida la identidad y los datos de configuración de la modalidad. */
    public MatchConfiguration {
        if (type == null) {
            throw new DopoSoccerException("La configuración del partido es inválida.");
        }
        Map<?, ?> configuredParameters = parameters == null ? Map.of() : parameters;
        validateEntries(configuredParameters, "Los parámetros de modalidad guardados son inválidos.");
        parameters = new TreeMap<>(parameters == null ? Map.of() : parameters);
        Map<?, ?> configuredState = state == null ? Map.of() : state;
        validateEntries(configuredState, "El estado de modalidad guardado es inválido.");
        state = new TreeMap<>(state == null ? Map.of() : state);
    }

    public MatchConfiguration(MatchType type, Map<String, MatchValue> parameters) {
        this(type, parameters, Map.of());
    }

    /** Convierte valores de texto de la versión 5 cuando no se dispone del esquema de modalidad. */
    public MatchConfiguration migrateLegacyValues() {
        Map<String, MatchValue> migratedParameters = new TreeMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) parameters).entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new DopoSoccerException("La configuración heredada contiene una clave inválida.");
            }
            migratedParameters.put(key, legacyValue(type, key, entry.getValue(), false));
        }
        Map<String, MatchValue> migratedState = new TreeMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) state).entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new DopoSoccerException("El estado heredado contiene una clave inválida.");
            }
            migratedState.put(key, legacyValue(type, key, entry.getValue(), true));
        }
        return new MatchConfiguration(type, migratedParameters, migratedState);
    }

    /** Construye la configuración migrada con parámetros normalizados según el esquema registrado. */
    public MatchConfiguration withMigratedParameters(Map<String, MatchValue> migratedParameters) {
        Map<String, MatchValue> migratedState = new TreeMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) state).entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new DopoSoccerException("El estado heredado contiene una clave inválida.");
            }
            migratedState.put(key, legacyValue(type, key, entry.getValue(), true));
        }
        return new MatchConfiguration(type, migratedParameters, migratedState);
    }

    private static void validateEntries(Map<?, ?> values, String message) {
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key) || key.isBlank() || entry.getValue() == null) {
                throw new DopoSoccerException(message);
            }
        }
    }

    private static MatchValue legacyValue(MatchType type, String key, Object value, boolean stateValue) {
        if (value instanceof MatchValue matchValue) return matchValue;
        if (!(value instanceof String text)) {
            throw new DopoSoccerException("La configuración heredada contiene un valor inválido.");
        }
        try {
            if (MatchType.GOALS.equals(type) && !stateValue && "goalLimit".equals(key)) {
                return new MatchValue.IntegerNumber(Integer.parseInt(text));
            }
            if (MatchType.TIME.equals(type)) {
                if (!stateValue && "durationSeconds".equals(key)) {
                    return new MatchValue.IntegerNumber(Integer.parseInt(text));
                }
                if (stateValue && "remainingSeconds".equals(key)) {
                    return new MatchValue.DecimalNumber(Double.parseDouble(text));
                }
                if (stateValue && "suddenDeath".equals(key)
                        && ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text))) {
                    return new MatchValue.Flag(Boolean.parseBoolean(text));
                }
            }
            return new MatchValue.Text(text);
        } catch (NumberFormatException exception) {
            throw new DopoSoccerException(
                    "El valor heredado " + key + " no tiene un formato válido.", exception);
        }
    }

    @Override
    public Map<String, MatchValue> parameters() {
        return Map.copyOf(parameters);
    }

    @Override
    public Map<String, MatchValue> state() {
        return Map.copyOf(state);
    }
}
