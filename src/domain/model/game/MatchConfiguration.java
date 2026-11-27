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
        Map<String, MatchValue> configuredParameters = parameters == null ? Map.of() : parameters;
        if (configuredParameters.entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null)) {
            throw new DopoSoccerException("Los parámetros de modalidad guardados son inválidos.");
        }
        parameters = new TreeMap<>(configuredParameters);
        Map<String, MatchValue> configuredState = state == null ? Map.of() : state;
        if (configuredState.entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null)) {
            throw new DopoSoccerException("El estado de modalidad guardado es inválido.");
        }
        state = new TreeMap<>(configuredState);
    }

    public MatchConfiguration(MatchType type, Map<String, MatchValue> parameters) {
        this(type, parameters, Map.of());
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
