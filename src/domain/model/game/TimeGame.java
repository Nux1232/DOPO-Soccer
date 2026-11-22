
package domain.model.game;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsSimulation;
import domain.model.game.lifecycle.MatchClock;

/**
 * Partido que termina cuando se agota el tiempo.
 */
public final class TimeGame extends Game {

    private final MatchClock clock;
    private boolean suddenDeath;

    public TimeGame(
            PlayerMode playerMode,
            Country firstCountry,
            Country secondCountry,
            PhysicsSimulation physicsSimulation,
            int durationSeconds
    ) throws DopoSoccerException {

        super(playerMode, firstCountry, secondCountry, physicsSimulation);

        if (durationSeconds <= 0) {
            throw new DopoSoccerException(
                    DopoSoccerException.INVALID_MATCH_DURATION
            );
        }

        this.clock = new MatchClock(durationSeconds);
    }

    public int durationSeconds() {
        return clock.durationSeconds();
    }

    /** True when regulation ended tied and the match is in golden-goal overtime. */
    public boolean suddenDeath() {
        return suddenDeath;
    }

    @Override
    public void scoreGoal(int scoringPlayer) {
        if (isFinished()) return;
        super.scoreGoal(scoringPlayer);
        if (suddenDeath) {
            int result = compareScores();
            if (result != 0) finishMatch(result > 0 ? 1 : 2);
        }
    }

    public void restoreClock(double remaining, boolean suddenDeath) {
        clock.restoreRemaining(remaining);
        this.suddenDeath = suddenDeath;
    }

    public double remainingExactSeconds() { return clock.remainingExactSeconds(); }

    public int remainingSeconds() {
        return clock.remainingSeconds();
    }

    public String formattedTime() {
        return clock.formatted();
    }
    @Override
    public String modeDescription() {
        return suddenDeath ? "MUERTE SÚBITA — GOL DE ORO"
                : "TIEMPO RESTANTE: " + formattedTime();
    }

    @Override
    public void update(
            PlayerInput firstInput,
            PlayerInput secondInput,
            double deltaSeconds
    ) throws DopoSoccerException {

        if (isFinished()) {
            return;
        }

        if (deltaSeconds < 0) {
            throw new DopoSoccerException(
                    DopoSoccerException.NEGATIVE_TIME
            );
        }

        super.update(firstInput, secondInput, deltaSeconds);

        if (!isFinished() && !suddenDeath) {
            clock.advance(deltaSeconds);

            if (clock.expired()) {
                int result = compareScores();
                if (result == 0) {
                    // El empate al finalizar el tiempo reglamentario activa muerte súbita.
                    // El partido sigue activo hasta que se marque un gol.
                    suddenDeath = true;
                } else {
                    finishMatch(result > 0 ? 1 : 2);
                }
            }
        }
    }
}
