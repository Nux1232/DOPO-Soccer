package domain.factory;

import domain.DopoSoccer.MatchRequest;
import domain.model.ai.AIProfile;
import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchType;
import domain.model.game.PlayerMode;
import domain.model.game.TimeGame;
import domain.model.physics.PhysicsEngine;
import domain.model.physics.PhysicsSimulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Construye partidos válidos y permite registrar constructores para modalidades adicionales. */
public final class MatchFactory {
    private final Map<MatchType, RegisteredMatch> creators = new HashMap<>();
    private final Supplier<? extends PhysicsSimulation> physicsProvider;

    /** Crea una fábrica con la implementación de física predeterminada. */
    public MatchFactory() {
        this(PhysicsEngine::new);
    }

    /**
     * Crea una fábrica con un proveedor de simulación física configurable.
     *
     * @param physicsProvider proveedor de implementaciones de física
     */
    public MatchFactory(Supplier<? extends PhysicsSimulation> physicsProvider) {
        this.physicsProvider = Objects.requireNonNull(physicsProvider,
                "El proveedor de física es obligatorio.");
        registerBuiltInCreators();
    }

    /**
     * Registra o reemplaza el constructor para una modalidad integrada o personalizada.
     *
     * @param type identificador de la modalidad
     * @param creator constructor de la modalidad
     */
    public void register(MatchType type, MatchCreator creator) {
        RegisteredMatch current = type == null ? null : creators.get(type);
        register(type,
                current == null ? (type == null ? null : type.id()) : current.displayName(),
                current == null ? List.of() : current.parameters(),
                creator);
    }

    /**
     * Registra una modalidad y sus metadatos de presentación.
     *
     * @param type identificador estable de la modalidad
     * @param displayName nombre visible en la interfaz
     * @param input configuración que la interfaz debe solicitar
     * @param creator constructor de la modalidad
     */
    public void register(MatchType type, String displayName, MatchInput input, MatchCreator creator) {
        if (input == null) {
            throw new DopoSoccerException("El nombre y la configuración de modalidad son obligatorios.");
        }
        register(type, displayName, input.parameters(), creator);
    }

    /**
     * Registra una modalidad con parámetros configurables desde su formulario.
     *
     * @param type identificador de modalidad
     * @param displayName nombre que verá el usuario
     * @param parameters definición de los campos a solicitar
     * @param creator constructor de modalidad
     */
    public void register(
            MatchType type, String displayName, List<MatchParameter> parameters, MatchCreator creator) {
        if (type == null || creator == null) {
            throw new DopoSoccerException("La modalidad y su constructor son obligatorios.");
        }
        if (displayName == null || displayName.isBlank() || parameters == null) {
            throw new DopoSoccerException("El nombre y la configuración de modalidad son obligatorios.");
        }
        if (parameters.stream().anyMatch(parameter -> parameter == null)
                || parameters.stream().map(MatchParameter::id).distinct().count() != parameters.size()) {
            throw new DopoSoccerException("Los parámetros deben ser válidos y tener identificadores únicos.");
        }
        creators.put(type, new RegisteredMatch(displayName.trim(), List.copyOf(parameters), creator));
    }

    /** @return modalidades registradas y sus metadatos para construir la selección de juego */
    public List<MatchTypeDescriptor> availableMatchTypes() {
        return creators.entrySet().stream()
                .map(entry -> new MatchTypeDescriptor(
                        entry.getKey(), entry.getValue().displayName(), entry.getValue().parameters()))
                .sorted((first, second) -> first.type().id().compareTo(second.type().id()))
                .toList();
    }

    private void registerBuiltInCreators() {
        register(MatchType.GOALS, "Por goles", List.of(
                new MatchParameter("goalLimit", "Límite de goles", "10", 1, 99, 1)),
                (request, first, second, physics) -> {
            if (request.goalLimit() <= 0) {
                throw new DopoSoccerException(DopoSoccerException.INVALID_GOAL_LIMIT);
            }
            return new GoalGame(request.playerMode(), first, second, physics, request.goalLimit());
        });
        register(MatchType.TIME, "Por tiempo", List.of(
                new MatchParameter("durationSeconds", "Duración (minutos)", "4", 1, 60, 60)),
                (request, first, second, physics) -> {
            if (request.durationSeconds() <= 0) {
                throw new DopoSoccerException(DopoSoccerException.INVALID_MATCH_DURATION);
            }
            return new TimeGame(request.playerMode(), first, second, physics, request.durationSeconds());
        });
    }

    /**
     * Crea un partido con los constructores registrados.
     *
     * @param request configuración del partido solicitada
     * @return instancia concreta de partido
     * @throws DopoSoccerException si algún parámetro es inválido o falta información requerida
     */
    public Game createMatch(MatchParameters request) throws DopoSoccerException {
        validate(request);
        RegisteredMatch registration = creators.get(request.matchType());
        if (registration == null) {
            throw new DopoSoccerException("No hay un constructor registrado para la modalidad solicitada.");
        }

        Country first = CountryFactory.create(request.firstCountryId());
        Country second = CountryFactory.create(request.secondCountryId());
        request = normalizeParameters(request, registration.parameters());
        PhysicsSimulation physics = physicsProvider.get();
        if (physics == null) {
            throw new DopoSoccerException("El proveedor de física devolvió null.");
        }
        Game game = registration.creator().create(request, first, second, physics);
        if (game == null) {
            throw new DopoSoccerException("El constructor de la modalidad devolvió null.");
        }
        return game;
    }

    private static MatchParameters normalizeParameters(
            MatchParameters request, List<MatchParameter> definitions) {
        Map<String, String> values = new HashMap<>(request.parameters());
        HashSet<String> knownIds = new HashSet<>();
        for (MatchParameter definition : definitions) {
            knownIds.add(definition.id());
            String value = values.get(definition.id());
            if (value == null) {
                if ("goalLimit".equals(definition.id())) {
                    value = Integer.toString(request.goalLimit());
                } else if ("durationSeconds".equals(definition.id())) {
                    value = Integer.toString(request.durationSeconds() / definition.multiplier());
                } else {
                    value = definition.defaultValue();
                }
            }
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < definition.minimum() || parsed > definition.maximum()) {
                    throw new DopoSoccerException("El parámetro " + definition.label()
                            + " está fuera del rango permitido.");
                }
                int configured = Math.multiplyExact(parsed, definition.multiplier());
                if ("goalLimit".equals(definition.id())) request = new MatchParameters(
                        request.playerMode(), request.matchType(), request.firstCountryId(),
                        request.secondCountryId(), configured, request.durationSeconds(),
                        request.aiProfile(), values);
                if ("durationSeconds".equals(definition.id())) request = new MatchParameters(
                        request.playerMode(), request.matchType(), request.firstCountryId(),
                        request.secondCountryId(), request.goalLimit(), configured,
                        request.aiProfile(), values);
                values.put(definition.id(), value);
            } catch (NumberFormatException | ArithmeticException exception) {
                throw new DopoSoccerException("El parámetro " + definition.label() + " debe ser numérico.", exception);
            }
        }
        if (!knownIds.containsAll(values.keySet())) {
            throw new DopoSoccerException("La solicitud contiene parámetros no reconocidos para la modalidad.");
        }
        return new MatchParameters(request.playerMode(), request.matchType(), request.firstCountryId(),
                request.secondCountryId(), request.goalLimit(), request.durationSeconds(),
                request.aiProfile(), values);
    }

    /**
     * Crea un partido con la fábrica predeterminada.
     *
     * @param request configuración del partido solicitada
     * @return instancia concreta de partido lista para iniciar
     */
    public static Game create(MatchRequest request) throws DopoSoccerException {
        return create(request, PhysicsEngine::new);
    }

    /**
     * Crea un partido usando el proveedor de física indicado y los constructores predeterminados.
     *
     * @param request configuración del partido solicitada
     * @param physicsProvider proveedor de la implementación de {@link PhysicsSimulation}
     * @return instancia concreta de partido lista para iniciar
     */
    public static Game create(
            MatchRequest request,
            Supplier<? extends PhysicsSimulation> physicsProvider) throws DopoSoccerException {
        return new MatchFactory(physicsProvider).createMatch(parametersFrom(request));
    }

    /**
     * Crea un partido utilizando directamente los tipos internos del dominio.
     *
     * @param mode modo de participación (PvP o PvM)
     * @param matchType modalidad del partido (goles o tiempo)
     * @param firstCountry identificador del país izquierdo
     * @param secondCountry identificador del país derecho
     * @param goalLimit límite de goles si la modalidad es por goles
     * @param durationSeconds duración en segundos si la modalidad es por tiempo
     * @param aiProfile perfil de inteligencia artificial de la máquina
     * @return partido configurado y listo para ejecutarse
     * @throws DopoSoccerException si algún parámetro requerido es {@code null} o inválido
     */
    public static Game create(
            PlayerMode mode,
            MatchType matchType,
            String firstCountry,
            String secondCountry,
            int goalLimit,
            int durationSeconds,
            AIProfile aiProfile) throws DopoSoccerException {
        if (mode == null || matchType == null || aiProfile == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        return new MatchFactory().createMatch(new MatchParameters(
                mode, matchType, firstCountry, secondCountry, goalLimit, durationSeconds, aiProfile));
    }

    private static MatchParameters parametersFrom(MatchRequest request) throws DopoSoccerException {
        if (request == null || request.playerMode() == null || request.matchType() == null
                || request.firstCountry() == null || request.secondCountry() == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        try {
            return new MatchParameters(
                    PlayerMode.valueOf(request.playerMode().name()),
                    MatchType.of(request.matchType()),
                    request.firstCountry(),
                    request.secondCountry(),
                    request.goalLimit(),
                    request.durationSeconds(),
                    AIProfile.valueOf(request.aiProfile() == null ? "EXPERT" : request.aiProfile()),
                    request.parameters());
        } catch (IllegalArgumentException exception) {
            throw new DopoSoccerException("La modalidad o el perfil seleccionado no existe.");
        }
    }

    private static void validate(MatchParameters request) throws DopoSoccerException {
        if (request == null || request.playerMode() == null || request.matchType() == null
                || request.firstCountryId() == null || request.secondCountryId() == null
                || request.aiProfile() == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
    }

    /** Constructor de una modalidad registrada en esta fábrica. */
    @FunctionalInterface
    public interface MatchCreator {
        /** Construye un partido con una solicitud ya validada estructuralmente. */
        Game create(MatchParameters request, Country first, Country second,
                PhysicsSimulation physics) throws DopoSoccerException;
    }

    /** Parámetro configurable que la interfaz debe presentar para una modalidad. */
    public enum MatchInput {
        /** La modalidad configura el límite de goles. */
        GOALS,
        /** La modalidad configura la duración en segundos. */
        TIME,
        /** La modalidad no requiere parámetros adicionales de interfaz. */
        NONE;

        private List<MatchParameter> parameters() {
            return switch (this) {
                case GOALS -> List.of(new MatchParameter("goalLimit", "Límite de goles", "10", 1, 99, 1));
                case TIME -> List.of(new MatchParameter("durationSeconds", "Duración (minutos)", "4", 1, 60, 60));
                case NONE -> List.of();
            };
        }
    }

    /** Metadatos de modalidad expuestos para construir menús sin listas codificadas. */
    public record MatchTypeDescriptor(MatchType type, String displayName, List<MatchParameter> parameters) { }

    /** Definición de campo numérico presentado al configurar una modalidad. */
    public record MatchParameter(String id, String label, String defaultValue,
            int minimum, int maximum, int multiplier) {
        public MatchParameter {
            if (id == null || id.isBlank() || label == null || label.isBlank()
                    || defaultValue == null || minimum < 0 || maximum < minimum || multiplier <= 0) {
                throw new DopoSoccerException("La definición de parámetro de modalidad es inválida.");
            }
            try {
                int parsedDefault = Integer.parseInt(defaultValue);
                if (parsedDefault < minimum || parsedDefault > maximum) {
                    throw new DopoSoccerException(
                            "El valor predeterminado del parámetro está fuera de su rango.");
                }
                Math.multiplyExact(parsedDefault, multiplier);
            } catch (NumberFormatException | ArithmeticException exception) {
                throw new DopoSoccerException("El valor predeterminado del parámetro debe ser numérico.", exception);
            }
        }
    }

    private record RegisteredMatch(String displayName, List<MatchParameter> parameters, MatchCreator creator) { }

    /** Parámetros tipados del dominio utilizados tanto al iniciar como al restaurar partidos. */
    public record MatchParameters(PlayerMode playerMode, MatchType matchType,
            String firstCountryId, String secondCountryId, int goalLimit, int durationSeconds,
            AIProfile aiProfile, Map<String, String> parameters) {
        public MatchParameters(PlayerMode playerMode, MatchType matchType, String firstCountryId,
                String secondCountryId, int goalLimit, int durationSeconds, AIProfile aiProfile) {
            this(playerMode, matchType, firstCountryId, secondCountryId, goalLimit, durationSeconds,
                    aiProfile, Map.of());
        }

        public MatchParameters {
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        }
    }
}
