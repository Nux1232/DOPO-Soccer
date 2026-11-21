
package domain.model.game;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsSimulation;

/**
 * Partido que termina cuando un jugador alcanza el límite de goles.
 */
public final class GoalGame extends Game {

    private final int goalLimit;

    public GoalGame(
            PlayerMode playerMode,
            Country firstCountry,
            Country secondCountry,
            PhysicsSimulation physicsSimulation,
            int goalLimit
    ) throws DopoSoccerException {

        super(playerMode, firstCountry, secondCountry, physicsSimulation);

        if (goalLimit <= 0) {
            throw new DopoSoccerException(
                    DopoSoccerException.INVALID_GOAL_LIMIT
            );
        }

        this.goalLimit = goalLimit;
    }

    public int goalLimit() {
        return goalLimit;
    }

    @Override
    public void scoreGoal(int scoringPlayer) {
        if (isFinished()) {
            return;
        }

        super.scoreGoal(scoringPlayer);

        if (firstPlayer().goals() >= goalLimit) {
            finishMatch(1);
        } else if (secondPlayer().goals() >= goalLimit) {
            finishMatch(2);
        }
    }
    @Override
    public String modeDescription() {
        return "PRIMERO EN LLEGAR A " + goalLimit + " GOLES";
    }
}
