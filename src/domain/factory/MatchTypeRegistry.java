package domain.factory;

import domain.model.exception.DopoSoccerException;
import domain.model.game.MatchConfiguration;
import domain.model.game.MatchValue;
import domain.model.game.MatchType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mantiene las modalidades registradas y normaliza sus parámetros de configuración. */
public final class MatchTypeRegistry {
    private final Map<MatchType, Registration> registrations = new HashMap<>();

    /** Registra una modalidad con sus metadatos de interfaz y constructor. */
    public void register(
            MatchType type,
            String displayName,
            List<MatchFactory.MatchParameter> parameters,
            MatchFactory.MatchCreator creator) {
        if (type == null || creator == null) {
            throw new DopoSoccerException("La modalidad y su constructor son obligatorios.");
        }
        if (displayName == null || displayName.isBlank() || parameters == null) {
            throw new DopoSoccerException("El nombre y la configuración de modalidad son obligatorios.");
        }
        if (parameters.stream().anyMatch(parameter -> parameter == null)
                || parameters.stream().map(MatchFactory.MatchParameter::id).distinct().count()
                != parameters.size()) {
            throw new DopoSoccerException("Los parámetros deben ser válidos y tener identificadores únicos.");
        }
        registrations.put(type, new Registration(
                type, displayName.trim(), List.copyOf(parameters), creator));
    }

    /** @return registro encontrado o {@code null} si la modalidad no está definida */
    public Registration find(MatchType type) {
        return registrations.get(type);
    }

    /** @return descriptores de las modalidades ordenados por ID */
    public List<MatchFactory.MatchTypeDescriptor> descriptors() {
        return registrations.values().stream()
                .map(registration -> new MatchFactory.MatchTypeDescriptor(
                        registration.type(), registration.displayName(), registration.parameters()))
                .sorted((first, second) -> first.type().id().compareTo(second.type().id()))
                .toList();
    }

    /** Valida los valores entrantes, aplica predeterminados y normaliza sus unidades de dominio. */
    MatchFactory.MatchParameters normalize(MatchFactory.MatchParameters request, Registration registration) {
        List<MatchFactory.MatchParameter> definitions = registration.parameters();
        Map<String, String> values = new HashMap<>(request.parameters());
        for (MatchFactory.MatchParameter definition : definitions) {
            String value = values.get(definition.id());
            if (value == null) value = definition.defaultValue();
            String normalized = definition.validate(value);
            if (definition.type() == MatchFactory.ParameterType.NUMBER) {
                normalized = Integer.toString(Math.multiplyExact(
                        Integer.parseInt(normalized), definition.multiplier()));
            }
            values.put(definition.id(), normalized);
        }
        Set<String> knownIds = definitions.stream()
                .map(MatchFactory.MatchParameter::id)
                .collect(java.util.stream.Collectors.toSet());
        if (!knownIds.containsAll(values.keySet())) {
            throw new DopoSoccerException("La solicitud contiene parámetros no reconocidos para la modalidad.");
        }
        return new MatchFactory.MatchParameters(
                request.playerMode(), request.matchType(), request.firstCountryId(), request.secondCountryId(),
                request.aiProfile(), values);
    }

    MatchFactory.MatchParameters restoreParameters(
            MatchFactory.MatchParameters request,
            Registration registration,
            MatchConfiguration configuration) {
        Map<String, String> values = valuesFromConfiguration(registration, configuration);
        return new MatchFactory.MatchParameters(
                request.playerMode(), request.matchType(), request.firstCountryId(), request.secondCountryId(),
                request.aiProfile(), values);
    }

    private Map<String, String> valuesFromConfiguration(
            Registration registration, MatchConfiguration configuration) {
        Map<String, MatchValue> savedParameters = configuration.parameters();
        Map<String, String> values = new HashMap<>();
        Set<String> knownIds = registration.parameters().stream()
                .map(MatchFactory.MatchParameter::id)
                .collect(java.util.stream.Collectors.toSet());
        if (!knownIds.containsAll(savedParameters.keySet())) {
            throw new DopoSoccerException("La configuración guardada contiene parámetros no reconocidos.");
        }
        for (MatchFactory.MatchParameter definition : registration.parameters()) {
            MatchValue stored = savedParameters.get(definition.id());
            if (stored == null) {
                throw new DopoSoccerException(
                        "Falta el parámetro " + definition.id() + " en la configuración guardada.");
            }
            String value = valueFor(definition, stored);
            definition.validate(definition.type() == MatchFactory.ParameterType.NUMBER
                    ? Integer.toString(Integer.parseInt(value) / definition.multiplier())
                    : value);
            values.put(definition.id(), value);
        }
        return values;
    }

    private String valueFor(MatchFactory.MatchParameter definition, MatchValue stored) {
        return switch (definition.type()) {
            case NUMBER -> {
                if (!(stored instanceof MatchValue.IntegerNumber number)
                        || number.value() % definition.multiplier() != 0) {
                    throw new DopoSoccerException(
                            "El parámetro guardado " + definition.id() + " debe ser un entero válido.");
                }
                yield Integer.toString(number.value());
            }
            case TEXT, SELECT -> {
                if (!(stored instanceof MatchValue.Text text)) {
                    throw new DopoSoccerException(
                            "El parámetro guardado " + definition.id() + " debe ser texto.");
                }
                yield text.value();
            }
            case BOOLEAN -> {
                if (!(stored instanceof MatchValue.Flag flag)) {
                    throw new DopoSoccerException(
                            "El parámetro guardado " + definition.id() + " debe ser booleano.");
                }
                yield Boolean.toString(flag.value());
            }
        };
    }

    void validateConfiguration(domain.model.game.MatchConfiguration configuration) {
        if (configuration == null) {
            throw new DopoSoccerException("La configuración del partido es obligatoria.");
        }
        Registration registration = find(configuration.type());
        if (registration == null) {
            throw new DopoSoccerException("No hay parámetros registrados para la modalidad solicitada.");
        }
        valuesFromConfiguration(registration, configuration);
        if (configuration.state().entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null)) {
            throw new DopoSoccerException("El estado guardado de la modalidad es inválido.");
        }
    }

    /** Metadatos de una modalidad registrados en el catálogo. */
    record Registration(
            MatchType type,
            String displayName,
            List<MatchFactory.MatchParameter> parameters,
            MatchFactory.MatchCreator creator) { }
}
