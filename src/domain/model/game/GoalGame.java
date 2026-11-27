
package domain.model.game;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsSimulation;
import domain.model.game.lifecycle.GoalLimitRule;

/**
 * Partido que termina cuando un jugador alcanza el límite de goles.
 */
public final class GoalGame extends Game {

    private final int goalLimit;

    /**
     * Crea un partido con victoria por límite de goles.
     *
     * @param playerMode modalidad de participación
     * @param firstCountry país del jugador izquierdo
     * @param secondCountry país del jugador derecho
     * @param physicsSimulation motor físico inyectado
     * @param goalLimit cantidad de goles necesaria para ganar
     * @throws DopoSoccerException si el límite de goles es menor o igual que cero
     */
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

    /** @return cantidad de goles requerida para finalizar el partido */
    public int goalLimit() {
        return goalLimit;
    }

    @Override
    public MatchConfiguration matchConfiguration() {
        return new MatchConfiguration(MatchType.GOALS, goalLimit, 240, 0, false);
    }

    @Override
    public void scoreGoal(int scoringPlayer) {
        if (isFinished()) {
            return;
        }

        super.scoreGoal(scoringPlayer);

        int winner = GoalLimitRule.winnerAtLimit(
                firstPlayer().goals(), secondPlayer().goals(), goalLimit);
        if (winner != -1) finishMatch(winner);
    }
    @Override
    public String modeDescription() {
        return "PRIMERO EN LLEGAR A " + goalLimit + " GOLES";
    }
}
