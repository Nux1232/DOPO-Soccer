package domain.model.game;

import domain.model.entity.Player;
import domain.model.exception.DopoSoccerException;

/** Applies the progression reward for a completed match. */
public final class MatchRewardService {
    public void award(Game game) {
        if (game == null || !game.isFinished()) {
            throw new DopoSoccerException("Solo se pueden entregar puntos al terminar un partido.");
        }
        Player first = game.firstPlayer();
        Player second = game.secondPlayer();
        if (game.winner() == 1) {
            first.stats().addPoints(100);
            second.stats().addPoints(25);
        } else if (game.winner() == 2) {
            second.stats().addPoints(100);
            first.stats().addPoints(25);
        } else {
            first.stats().addPoints(25);
            second.stats().addPoints(25);
        }
    }
}
