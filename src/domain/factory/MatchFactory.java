package domain.factory;

import domain.DopoSoccer.MatchRequest;
import domain.model.ai.AIProfile;
import domain.model.entity.Country;
import domain.model.entity.CountryCatalog;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchType;
import domain.model.game.MatchRestoreRequest;
import domain.model.game.MatchRestorer;
import domain.model.game.PlayerMode;
import domain.model.game.TimeGame;
import domain.model.physics.PhysicsEngine;
import domain.model.physics.PhysicsSimulation;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Construye partidos válidos y permite registrar constructores para modalidades adicionales. */
public final class MatchFactory implements MatchRestorer {
    private final MatchTypeRegistry matchTypes = new MatchTypeRegistry();
    private final Supplier<? extends PhysicsSimulation> physicsProvider;
    private final CountryCatalog countryCatalog;

    /** Crea una fábrica con la implementación de física predeterminada. */
    public MatchFactory() {
        this(CountryCatalog.defaultCatalog(), PhysicsEngine::new);
    }

    /**
     * Crea una fábrica con un proveedor de simulación física configurable.
     *
     * @param physicsProvider proveedor de implementaciones de física
     */
    public MatchFactory(Supplier<? extends PhysicsSimulation> physicsProvider) {
        this(CountryCatalog.defaultCatalog(), physicsProvider);
    }

    /**
     * Crea una fábrica con catálogo de países y proveedor de física inyectados.
     *
     * @param countryCatalog catálogo de países de esta aplicación
     * @param physicsProvider proveedor de implementaciones de física
     */
    public MatchFactory(
            CountryCatalog countryCatalog, Supplier<? extends PhysicsSimulation> physicsProvider) {
        this.countryCatalog = Objects.requireNonNull(countryCatalog,
                "El catálogo de países es obligatorio.");
        this.physicsProvider = Objects.requireNonNull(physicsProvider,
                "El proveedor de física es obligatorio.");
        registerBuiltInCreators();
    }

    /** @return países disponibles en el catálogo configurado */
    public List<Country> availableCountries() {
        return countryCatalog.countries();
    }

    /**
     * Registra o reemplaza el constructor para una modalidad integrada o personalizada.
     *
     * @param type identificador de la modalidad
     * @param creator constructor de la modalidad
     */
    public void register(MatchType type, MatchCreator creator) {
        MatchTypeRegistry.Registration current = matchTypes.find(type);
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
        matchTypes.register(type, displayName, parameters, creator);
    }

    /** @return modalidades registradas y sus metadatos para construir la selección de juego */
    public List<MatchTypeDescriptor> availableMatchTypes() {
        return matchTypes.descriptors();
    }

    private void registerBuiltInCreators() {
        register(MatchType.GOALS, "Por goles", List.of(
                new MatchParameter("goalLimit", "Límite de goles", "10", 1, 99, 1)),
                (request, first, second, physics) -> {
            int goalLimit = requiredInteger(request, "goalLimit");
            if (goalLimit <= 0) {
                throw new DopoSoccerException(DopoSoccerException.INVALID_GOAL_LIMIT);
            }
            return new GoalGame(request.playerMode(), first, second, physics, goalLimit);
        });
        register(MatchType.TIME, "Por tiempo", List.of(
                new MatchParameter("durationSeconds", "Duración (minutos)", "4", 1, 60, 60)),
                (request, first, second, physics) -> {
            int durationSeconds = requiredInteger(request, "durationSeconds");
            if (durationSeconds <= 0) {
                throw new DopoSoccerException(DopoSoccerException.INVALID_MATCH_DURATION);
            }
            return new TimeGame(request.playerMode(), first, second, physics, durationSeconds);
        });
    }

    private static int requiredInteger(MatchParameters request, String id) {
        try {
            return Integer.parseInt(request.parameters().get(id));
        } catch (NumberFormatException | NullPointerException exception) {
            throw new DopoSoccerException("Falta un parámetro numérico válido: " + id + ".", exception);
        }
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
        MatchTypeRegistry.Registration registration = matchTypes.find(request.matchType());
        if (registration == null) {
            throw new DopoSoccerException("No hay un constructor registrado para la modalidad solicitada.");
        }

        Country first = CountryFactory.create(request.firstCountryId(), countryCatalog);
        Country second = CountryFactory.create(request.secondCountryId(), countryCatalog);
        request = matchTypes.normalize(request, registration);
        return instantiate(request, registration, first, second);
    }

    private Game instantiate(
            MatchParameters request, MatchTypeRegistry.Registration registration, Country first, Country second) {
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

    /**
     * Reconstruye la instancia base de una modalidad guardada.
     * El servicio de persistencia restaura después el estado dinámico del partido.
     */
    @Override
    public Game restore(MatchRestoreRequest request) {
        if (request == null || request.configuration() == null) {
            throw new DopoSoccerException("Los datos para restaurar el partido son obligatorios.");
        }
        MatchType type = request.configuration().type();
        MatchTypeRegistry.Registration registration = matchTypes.find(type);
        if (registration == null) {
            throw new DopoSoccerException("No hay un constructor registrado para la modalidad solicitada.");
        }
        MatchParameters base = new MatchParameters(
                request.playerMode(),
                type,
                request.firstCountryId(),
                request.secondCountryId(),
                request.aiProfile(),
                Map.of());
        MatchParameters restored = matchTypes.restoreParameters(
                base, registration, request.configuration());
        validate(restored);
        Country first = CountryFactory.create(restored.firstCountryId(), countryCatalog);
        Country second = CountryFactory.create(restored.secondCountryId(), countryCatalog);
        return instantiate(restored, registration, first, second);
    }

    @Override
    public void validateConfiguration(domain.model.game.MatchConfiguration configuration) {
        matchTypes.validateConfiguration(configuration);
    }

    @Override
    public domain.model.game.MatchConfiguration migrateLegacyConfiguration(
            domain.model.game.MatchConfiguration configuration) {
        return matchTypes.migrateLegacyConfiguration(configuration);
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
                mode, matchType, firstCountry, secondCountry, aiProfile,
                legacyParameters(matchType, goalLimit, durationSeconds)));
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

    /**
     * Metadatos de modalidad expuestos para construir menús sin listas codificadas.
     *
     * @param type identificador de dominio de la modalidad
     * @param displayName nombre que se muestra al usuario
     * @param parameters definición de los campos que debe configurar la interfaz
     */
    public record MatchTypeDescriptor(MatchType type, String displayName, List<MatchParameter> parameters) { }

    /** Tipo de control generado para un parámetro de modalidad. */
    public enum ParameterType {
        /** Campo para introducir un número dentro del rango configurado. */
        NUMBER,
        /** Campo para introducir texto dentro de una longitud permitida. */
        TEXT,
        /** Control para activar o desactivar una opción. */
        BOOLEAN,
        /** Control para elegir entre opciones predefinidas. */
        SELECT
    }

    /**
     * Definición declarativa de un campo presentado al configurar una modalidad.
     *
     * @param id identificador estable usado para enviar el valor al dominio
     * @param label etiqueta visible del campo
     * @param defaultValue valor inicial representado como texto
     * @param minimum límite inferior numérico o longitud mínima para texto
     * @param maximum límite superior numérico o longitud máxima para texto
     * @param multiplier factor para convertir un valor numérico a la unidad del dominio
     * @param type tipo de control que debe construir la interfaz
     * @param options valores permitidos para un control de selección; vacío para otros tipos
     */
    public record MatchParameter(String id, String label, String defaultValue,
            int minimum, int maximum, int multiplier, ParameterType type, List<String> options) {
        /** Crea un parámetro numérico sin opciones de selección.
         * @param id identificador estable usado para enviar el valor al dominio
         * @param label etiqueta visible del campo
         * @param defaultValue valor numérico inicial representado como texto
         * @param minimum límite inferior permitido
         * @param maximum límite superior permitido
         * @param multiplier factor para convertir el valor a la unidad del dominio
         */
        public MatchParameter(String id, String label, String defaultValue,
                int minimum, int maximum, int multiplier) {
            this(id, label, defaultValue, minimum, maximum, multiplier, ParameterType.NUMBER, List.of());
        }

        public MatchParameter {
            if (id == null || id.isBlank() || label == null || label.isBlank()
                    || defaultValue == null || type == null || options == null
                    || minimum < 0 || maximum < minimum || multiplier <= 0) {
                throw new DopoSoccerException("La definición de parámetro de modalidad es inválida.");
            }
            options = List.copyOf(options);
            switch (type) {
                case NUMBER -> {
                    if (!options.isEmpty()) {
                        throw new DopoSoccerException("Un parámetro numérico no debe incluir opciones.");
                    }
                    try {
                        int parsedDefault = Integer.parseInt(defaultValue);
                        if (parsedDefault < minimum || parsedDefault > maximum) {
                            throw new DopoSoccerException(
                                    "El valor predeterminado del parámetro está fuera de su rango.");
                        }
                        Math.multiplyExact(parsedDefault, multiplier);
                    } catch (NumberFormatException | ArithmeticException exception) {
                        throw new DopoSoccerException(
                                "El valor predeterminado del parámetro debe ser numérico.", exception);
                    }
                }
                case SELECT -> {
                    if (options.isEmpty() || !options.contains(defaultValue)) {
                        throw new DopoSoccerException(
                                "Un parámetro de selección requiere opciones y un valor predeterminado válido.");
                    }
                }
                case BOOLEAN -> {
                    if (!options.isEmpty()
                            || !("true".equalsIgnoreCase(defaultValue)
                            || "false".equalsIgnoreCase(defaultValue))) {
                        throw new DopoSoccerException("El valor predeterminado booleano es inválido.");
                    }
                }
                case TEXT -> {
                    if (!options.isEmpty() || defaultValue.length() < minimum
                            || defaultValue.length() > maximum) {
                        throw new DopoSoccerException("El texto predeterminado está fuera de su rango.");
                    }
                }
            }
        }

        /** Valida y normaliza un valor ingresado para este parámetro.
         * @param value valor proporcionado por la interfaz
         * @return valor validado; los booleanos se normalizan a minúsculas
         * @throws DopoSoccerException si el valor falta, no cumple el tipo o queda fuera de rango
         */
        public String validate(String value) {
            if (value == null) {
                throw new DopoSoccerException("Falta el parámetro " + label + ".");
            }
            switch (type) {
                case NUMBER -> {
                    try {
                        int parsed = Integer.parseInt(value);
                        if (parsed < minimum || parsed > maximum) {
                            throw new DopoSoccerException("El parámetro " + label
                                    + " está fuera del rango permitido.");
                        }
                        Math.multiplyExact(parsed, multiplier);
                    } catch (NumberFormatException | ArithmeticException exception) {
                        throw new DopoSoccerException("El parámetro " + label + " debe ser numérico.", exception);
                    }
                }
                case SELECT -> {
                    if (!options.contains(value)) {
                        throw new DopoSoccerException("La opción seleccionada para " + label + " no existe.");
                    }
                }
                case BOOLEAN -> {
                    if (!("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                        throw new DopoSoccerException("El parámetro " + label + " debe ser booleano.");
                    }
                    return value.toLowerCase(java.util.Locale.ROOT);
                }
                case TEXT -> {
                    if (value.length() < minimum || value.length() > maximum) {
                        throw new DopoSoccerException("El texto de " + label + " está fuera del rango permitido.");
                    }
                }
            }
            return value;
        }
    }

    /**
     * Parámetros tipados del dominio utilizados tanto al iniciar como al restaurar partidos.
     *
     * @param playerMode modo de participación
     * @param matchType modalidad de partido
     * @param firstCountryId identificador del país del primer jugador
     * @param secondCountryId identificador del país del segundo jugador
     * @param aiProfile perfil de IA asociado al partido
     * @param parameters valores de entrada indexados por el identificador de cada parámetro
     */
    public record MatchParameters(PlayerMode playerMode, MatchType matchType,
            String firstCountryId, String secondCountryId, AIProfile aiProfile, Map<String, String> parameters) {
        /** Crea parámetros compatibles con las modalidades que usan límites genéricos.
         * @param playerMode modo de participación
         * @param matchType modalidad de partido
         * @param firstCountryId identificador del país del primer jugador
         * @param secondCountryId identificador del país del segundo jugador
         * @param goalLimit límite de goles de una modalidad por goles
         * @param durationSeconds duración en segundos de una modalidad por tiempo
         * @param aiProfile perfil de IA asociado al partido
         */
        public MatchParameters(PlayerMode playerMode, MatchType matchType, String firstCountryId,
                String secondCountryId, int goalLimit, int durationSeconds, AIProfile aiProfile) {
            this(playerMode, matchType, firstCountryId, secondCountryId, aiProfile,
                    legacyParameters(matchType, goalLimit, durationSeconds));
        }

        public MatchParameters {
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        }
    }

    private static Map<String, String> legacyParameters(MatchType type, int goalLimit, int durationSeconds) {
        Map<String, String> parameters = new HashMap<>();
        if (MatchType.GOALS.equals(type) && goalLimit > 0) {
            parameters.put("goalLimit", Integer.toString(goalLimit));
        } else if (MatchType.TIME.equals(type) && durationSeconds > 0) {
            if (durationSeconds % 60 != 0) {
                throw new DopoSoccerException("La duración heredada debe expresarse en minutos completos.");
            }
            parameters.put("durationSeconds", Integer.toString(durationSeconds / 60));
        }
        return Map.copyOf(parameters);
    }
}
