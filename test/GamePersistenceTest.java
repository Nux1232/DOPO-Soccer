package test;

import domain.model.entity.Country;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.PowerShot;
import domain.model.game.Game;
import domain.model.game.GameMatchConfig;
import domain.model.game.GoalGame;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.game.TimeGame;
import domain.model.physics.PhysicsEngine;
import domain.persistence.GamePersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamePersistenceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void savesAndRestoresMatchStateAndRecreatesPhysicsEngine() throws Exception {
        GamePersistence persistence = new GamePersistence(temporaryDirectory.resolve("match.ser"));
        GameMatchConfig config = new GameMatchConfig();
        config.setGameMode(PlayerMode.PLAYER_VS_PLAYER);
        config.setPlayer1Character(Country.CAMEROON);

        GoalGame game = new GoalGame(
                PlayerMode.PLAYER_VS_PLAYER,
                Country.CAMEROON,
                Country.USA,
                new PhysicsEngine(),
                7);
        game.scoreGoal(1);
        game.firstPlayer().stats().addPoints(600);
        game.firstPlayer().applyStatusEffect(PlayerStatusEffect.ELECTRIFIED, 2.5);
        game.ball().setPosition(321, 240);
        game.ball().setVelocity(120, -30);
        game.ball().startPowerShot(PowerShot.ILLUSION, game.firstPlayer());
        game.ball().markPowerEffectApplied();

        persistence.save(game, config);

        assertTrue(persistence.hasSavedMatch());
        GamePersistence.SavedMatch savedMatch = persistence.load();
        Game restored = savedMatch.game();

        GoalGame restoredGoalGame = assertInstanceOf(GoalGame.class, restored);
        assertEquals(7, restoredGoalGame.goalLimit());
        assertEquals(1, restored.firstPlayer().goals());
        assertEquals(600, restored.firstPlayer().stats().points());
        assertEquals(PlayerStatusEffect.ELECTRIFIED, restored.firstPlayer().statusEffect());
        assertEquals(2.5, restored.firstPlayer().statusEffectTimer());
        assertEquals(321, restored.ball().x());
        assertEquals(240, restored.ball().y());
        assertEquals(120, restored.ball().velocityX());
        assertEquals(-30, restored.ball().velocityY());
        assertSame(PowerShot.ILLUSION, restored.ball().activePowerShot());
        assertSame(restored.firstPlayer(), restored.ball().powerShotOwner());
        assertTrue(restored.ball().powerEffectApplied());
        assertSame(Country.CAMEROON, restored.firstPlayer().country());
        assertSame(Country.CAMEROON, savedMatch.matchConfig().getPlayer1Character());
        assertDoesNotThrow(() -> restored.update(PlayerInput.NONE, PlayerInput.NONE, 1.0 / 60.0));
    }

    @Test
    void savesRemainingTimeForTimeMatches() throws Exception {
        GamePersistence persistence = new GamePersistence(temporaryDirectory.resolve("time-match.ser"));
        GameMatchConfig config = new GameMatchConfig();
        TimeGame game = new TimeGame(
                PlayerMode.PLAYER_VS_PLAYER,
                Country.SOUTH_KOREA,
                Country.NIGERIA,
                new PhysicsEngine(),
                120);
        game.update(PlayerInput.NONE, PlayerInput.NONE, 5);

        persistence.save(game, config);

        TimeGame restored = assertInstanceOf(TimeGame.class, persistence.load().game());
        assertEquals(115, restored.remainingSeconds());
    }
}
