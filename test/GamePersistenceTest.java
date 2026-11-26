package tests;

import domain.factory.CountryFactory;
import domain.model.ai.AIProfile;
import domain.model.entity.PlayerStatusEffect;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchType;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.game.TimeGame;
import domain.model.persistence.MatchPersistenceService;
import domain.model.persistence.MatchSaveStore;
import domain.model.physics.PhysicsEngine;
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
    void serializesTypedMatchStateAndRebuildsThePhysicsEngine() throws Exception {
        MatchPersistenceService persistence = new MatchPersistenceService(
                new MatchSaveStore(temporaryDirectory.resolve("match.ser")));
        GoalGame game = new GoalGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("CM"),
                CountryFactory.create("USA"),
                new PhysicsEngine(),
                7);
        game.scoreGoal(1);
        game.firstPlayer().stats().addPoints(600);
        game.firstPlayer().applyStatusEffect(PlayerStatusEffect.ELECTRIFIED, 2.5);
        game.firstPlayer().applyKnockback(240, -300, 0.3);
        game.ball().setPosition(321, 240);
        game.ball().setVelocity(120, -30);
        game.ball().startPowerShot(game.firstPlayer().country(), game.firstPlayer());
        game.ball().markPowerEffectApplied();

        persistence.save(game, AIProfile.OFFENSIVE, false);

        assertTrue(persistence.exists());
        MatchPersistenceService.LoadedMatch saved = persistence.load();
        Game restored = saved.game();

        GoalGame restoredGoalGame = assertInstanceOf(GoalGame.class, restored);
        assertEquals(7, restoredGoalGame.goalLimit());
        assertEquals(1, restored.firstPlayer().goals());
        assertEquals(600, restored.firstPlayer().stats().points());
        assertEquals(PlayerStatusEffect.ELECTRIFIED, restored.firstPlayer().statusEffect());
        assertEquals(2.5, restored.firstPlayer().statusEffectTimer());
        assertEquals(0.3, restored.firstPlayer().impactLockTimer());
        assertEquals(321, restored.ball().x());
        assertEquals(240, restored.ball().y());
        assertEquals(120, restored.ball().velocityX());
        assertEquals(-30, restored.ball().velocityY());
        assertSame(restored.firstPlayer().country(), restored.ball().activePowerShot());
        assertSame(restored.firstPlayer(), restored.ball().powerShotOwner());
        assertTrue(restored.ball().powerEffectApplied());
        assertEquals(AIProfile.OFFENSIVE, saved.aiProfile());
        assertDoesNotThrow(() -> restored.update(PlayerInput.NONE, PlayerInput.NONE, 1.0 / 60.0));
    }

    @Test
    void serializesRemainingTimeAndSuddenDeathState() throws Exception {
        MatchPersistenceService persistence = new MatchPersistenceService(
                temporaryDirectory.resolve("time-match.ser"));
        TimeGame game = new TimeGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("KR"),
                CountryFactory.create("NG"),
                new PhysicsEngine(),
                120);
        game.update(PlayerInput.NONE, PlayerInput.NONE, 5);

        persistence.save(game, AIProfile.EXPERT, false);

        TimeGame restored = assertInstanceOf(TimeGame.class, persistence.load().game());
        assertEquals(115, restored.remainingSeconds());
        assertEquals(MatchType.TIME, new MatchSaveStore(temporaryDirectory.resolve("time-match.ser"))
                .load().matchType());
    }
}
