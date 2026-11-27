package domain.model.game;

import domain.model.entity.Player;
import domain.model.exception.DopoSoccerException;

/** Aplica la recompensa de puntos de progresión al concluir un partido. */
public final class MatchRewardService {
    /**
     * Entrega 100 puntos al ganador y 25 puntos al perdedor (o 25 a cada uno en caso de empate).
     *
     * @param game partido finalizado sobre el cual calcular recompensas
     * @throws DopoSoccerException si el partido es {@code null} o aún no termina
     */
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
