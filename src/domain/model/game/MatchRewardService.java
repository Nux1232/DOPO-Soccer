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
        first.stats().addPoints(rewardFor(game, 1));
        second.stats().addPoints(rewardFor(game, 2));
    }

    /**
     * Calcula la recompensa sin modificar el partido, para poder persistirla antes de aplicarla.
     *
     * @param game partido finalizado
     * @param playerNumber jugador 1 o 2
     * @return puntos que recibirá ese jugador
     */
    public int rewardFor(Game game, int playerNumber) {
        if (game == null || !game.isFinished() || (playerNumber != 1 && playerNumber != 2)) {
            throw new DopoSoccerException("No se puede calcular la recompensa solicitada.");
        }
        return game.winner() == 0 || game.winner() != playerNumber ? 25 : 100;
    }
}
