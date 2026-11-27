package domain.factory;

import domain.DopoSoccer.MatchKind;
import domain.DopoSoccer.MatchRequest;
import domain.DopoSoccer.PlayerModeKind;
import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.PlayerMode;
import domain.model.game.TimeGame;
import domain.model.physics.PhysicsEngine;
import domain.model.ai.AIProfile;
import domain.model.physics.PhysicsSimulation;

import java.util.Objects;
import java.util.function.Supplier;

/** Construye partidos válidos y encapsula la selección de implementación. */
public final class MatchFactory {
    private MatchFactory() { }

    /**
     * Crea un partido a partir de la solicitud recibida usando el motor físico por defecto.
     *
     * @param request configuración del partido solicitada
     * @return instancia concreta de partido ({@link GoalGame} o {@link TimeGame})
     * @throws DopoSoccerException si algún parámetro es inválido o falta información requerida
     */
    public static Game create(MatchRequest request) throws DopoSoccerException {
        return create(request, PhysicsEngine::new);
    }

    /**
     * Crea un partido inyectando un proveedor personalizado de simulación física.
     *
     * @param request configuración del partido solicitada
     * @param physicsProvider proveedor de la implementación de {@link PhysicsSimulation}
     * @return instancia concreta de partido lista para iniciar
     * @throws DopoSoccerException si la configuración es inválida o el proveedor retorna {@code null}
     */
    public static Game create(
            MatchRequest request,
            Supplier<? extends PhysicsSimulation> physicsProvider) throws DopoSoccerException {
        validate(request);
        Objects.requireNonNull(physicsProvider, "El proveedor de física es obligatorio.");
        PlayerMode mode = PlayerMode.valueOf(request.playerMode().name());
        try {
            AIProfile.valueOf(request.aiProfile() == null ? "EXPERT" : request.aiProfile());
        } catch (IllegalArgumentException exception) {
            throw new DopoSoccerException("El perfil de máquina seleccionado no existe.");
        }
        Country first = CountryFactory.create(request.firstCountry());
        Country second = CountryFactory.create(request.secondCountry());
        PhysicsSimulation physics = physicsProvider.get();
        if (physics == null) {
            throw new DopoSoccerException("El proveedor de física devolvió null.");
        }

        if (request.matchType() == MatchKind.GOALS) {
            return new GoalGame(mode, first, second, physics, request.goalLimit());
        }
        return new TimeGame(mode, first, second, physics, request.durationSeconds());
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
            domain.model.game.MatchType matchType,
            String firstCountry,
            String secondCountry,
            int goalLimit,
            int durationSeconds,
            AIProfile aiProfile) throws DopoSoccerException {
        if (mode == null || matchType == null || aiProfile == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        MatchKind kind = MatchKind.valueOf(matchType.name());
        return create(new MatchRequest(
                PlayerModeKind.valueOf(mode.name()), kind, firstCountry, secondCountry,
                goalLimit, durationSeconds, aiProfile.name()));
    }

    private static void validate(MatchRequest request) throws DopoSoccerException {
        if (request == null || request.playerMode() == null || request.matchType() == null
                || request.firstCountry() == null || request.secondCountry() == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        if (request.matchType() == MatchKind.GOALS && request.goalLimit() <= 0) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_GOAL_LIMIT);
        }
        if (request.matchType() == MatchKind.TIME && request.durationSeconds() <= 0) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_MATCH_DURATION);
        }
    }
}
