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

    public static Game create(MatchRequest request) throws DopoSoccerException {
        return create(request, PhysicsEngine::new);
    }

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
