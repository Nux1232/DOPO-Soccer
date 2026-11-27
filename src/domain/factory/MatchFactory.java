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

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Construye partidos válidos y permite registrar constructores para modalidades adicionales. */
public final class MatchFactory {
    private final Map<MatchType, MatchCreator> creators = new EnumMap<>(MatchType.class);
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

    /** Registra o reemplaza el constructor para una modalidad. */
    public void register(MatchType type, MatchCreator creator) {
        if (type == null || creator == null) {
            throw new DopoSoccerException("La modalidad y su constructor son obligatorios.");
        }
        creators.put(type, creator);
    }

    private void registerBuiltInCreators() {
        register(MatchType.GOALS, (request, first, second, physics) -> {
            if (request.goalLimit() <= 0) {
                throw new DopoSoccerException(DopoSoccerException.INVALID_GOAL_LIMIT);
            }
            return new GoalGame(request.playerMode(), first, second, physics, request.goalLimit());
        });
        register(MatchType.TIME, (request, first, second, physics) -> {
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
        MatchCreator creator = creators.get(request.matchType());
        if (creator == null) {
            throw new DopoSoccerException("No hay un constructor registrado para la modalidad solicitada.");
        }

        Country first = CountryFactory.create(request.firstCountryId());
        Country second = CountryFactory.create(request.secondCountryId());
        PhysicsSimulation physics = physicsProvider.get();
        if (physics == null) {
            throw new DopoSoccerException("El proveedor de física devolvió null.");
        }
        Game game = creator.create(request, first, second, physics);
        if (game == null) {
            throw new DopoSoccerException("El constructor de la modalidad devolvió null.");
        }
        return game;
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
                    MatchType.valueOf(request.matchType().name()),
                    request.firstCountry(),
                    request.secondCountry(),
                    request.goalLimit(),
                    request.durationSeconds(),
                    AIProfile.valueOf(request.aiProfile() == null ? "EXPERT" : request.aiProfile()));
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

    /** Parámetros tipados del dominio utilizados tanto al iniciar como al restaurar partidos. */
    public record MatchParameters(PlayerMode playerMode, MatchType matchType,
            String firstCountryId, String secondCountryId, int goalLimit, int durationSeconds,
            AIProfile aiProfile) { }
}
