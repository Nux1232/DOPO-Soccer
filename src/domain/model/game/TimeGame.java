
package domain.model.game;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsSimulation;

/**
 * Partido que termina cuando se agota el tiempo.
 */
public final class TimeGame extends Game {

    private final int durationSeconds;
    private double remainingSeconds;

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

        this.durationSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public int remainingSeconds() {
        return (int) Math.ceil(remainingSeconds);
    }

    public String formattedTime() {
        int total = remainingSeconds();
        int minutes = total / 60;
        int seconds = total % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
    @Override
    public String modeDescription() {
        return "TIEMPO RESTANTE: " + formattedTime();
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

        if (!isFinished()) {
            remainingSeconds = Math.max(
                    0,
                    remainingSeconds - deltaSeconds
            );

            if (remainingSeconds <= 0) {
                int result = compareScores();
                finishMatch(result > 0 ? 1 : result < 0 ? 2 : 0);
            }
        }
    }
}
