package tests;

import domain.factory.CountryFactory;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.game.TimeGame;
import domain.model.physics.PhysicsSimulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeGameSuddenDeathTest {
    private static final PhysicsSimulation NO_PHYSICS = new PhysicsSimulation() {
        @Override public void update(Game game, PlayerInput first, PlayerInput second, double dt) { }
        @Override public void kick(Player kicker, Player opponent, Ball ball, int direction) { }
    };

    private TimeGame newGame() throws DopoSoccerException {
        return new TimeGame(PlayerMode.PLAYER_VS_PLAYER, CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"), NO_PHYSICS, 1);
    }

    @Test void tiedRegulationTimeTransitionsToSuddenDeath() throws DopoSoccerException {
        TimeGame game = newGame();
        game.update(PlayerInput.NONE, PlayerInput.NONE, 1.0);
        assertTrue(game.suddenDeath());
        assertFalse(game.isFinished());
        assertEquals(0, game.winner());
        assertEquals(0, game.remainingSeconds());
    }

    @Test void suddenDeathContinuesUntilGoalAndGoldenGoalWins() throws DopoSoccerException {
        TimeGame game = newGame();
        game.update(PlayerInput.NONE, PlayerInput.NONE, 1.0);
        game.update(PlayerInput.NONE, PlayerInput.NONE, 20.0);
        assertFalse(game.isFinished(), "No debe terminar por tiempo adicional en overtime");
        assertTrue(game.suddenDeath());
        game.scoreGoal(2);
        assertTrue(game.isFinished());
        assertEquals(2, game.winner());
        assertEquals(1, game.secondPlayer().goals());
    }

    @Test void goalScoredAfterRegulationTieImmediatelyEndsMatch() throws DopoSoccerException {
        TimeGame game = newGame();
        game.update(PlayerInput.NONE, PlayerInput.NONE, 1.0);
        game.scoreGoal(1);
        assertTrue(game.isFinished());
        assertEquals(1, game.winner());
    }

    @Test void nonTiedScoreAtTimeExpiryEndsNormally() throws DopoSoccerException {
        TimeGame game = newGame();
        game.scoreGoal(1);
        game.update(PlayerInput.NONE, PlayerInput.NONE, 1.0);
        assertTrue(game.isFinished());
        assertFalse(game.suddenDeath());
        assertEquals(1, game.winner());
    }

    @Test void finishedMatchIgnoresFurtherGoalsAndUpdates() throws DopoSoccerException {
        TimeGame game = newGame();
        game.update(PlayerInput.NONE, PlayerInput.NONE, 1.0);
        game.scoreGoal(1);
        game.scoreGoal(2);
        game.update(PlayerInput.NONE, PlayerInput.NONE, 50.0);
        assertEquals(1, game.winner());
        assertEquals(1, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }
}
