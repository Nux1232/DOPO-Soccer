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

/** Construye partidos válidos y encapsula la selección de implementación. */
public final class MatchFactory {
    private MatchFactory() { }

    public static Game create(MatchRequest request) throws DopoSoccerException {
        validate(request);
        PlayerMode mode = PlayerMode.valueOf(request.playerMode().name());
        Country first = CountryFactory.create(request.firstCountry());
        Country second = CountryFactory.create(request.secondCountry());
        PhysicsEngine physics = new PhysicsEngine();

        if (request.matchType() == MatchKind.GOALS) {
            return new GoalGame(mode, first, second, physics, request.goalLimit());
        }
        return new TimeGame(mode, first, second, physics, request.durationSeconds());
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
