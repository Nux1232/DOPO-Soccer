package tests;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;
import domain.model.entity.PowerShot;
import domain.model.physics.GoalDetector;
import domain.model.physics.PowerShotImpactHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsRulesTest {

    @Test
    void standardGoalDetectorIdentifiesBothGoals() {
        GoalDetector detector = GoalDetector.standardField();
        assertEquals(2, detector.scoringPlayer(new Ball(30, 400)).orElseThrow());
        assertEquals(1, detector.scoringPlayer(new Ball(930, 400)).orElseThrow());
        assertTrue(detector.scoringPlayer(new Ball(480, 400)).isEmpty());
        assertTrue(detector.scoringPlayer(new Ball(30, 250)).isEmpty());
    }

    @Test
    void impactIsDelegatedToThePowerWithoutPhysicsEngineChanges() throws Exception {
        Player owner = new Player(Country.USA, 100);
        Player opponent = new Player(Country.CAMEROON, 120);
        Ball ball = new Ball(120, 100);
        ball.startPowerShot(PowerShot.ILLUSION, owner);
        PowerShotImpactHandler handler = new PowerShotImpactHandler();

        handler.apply(ball, owner, opponent);

        assertEquals(70, opponent.health());
        assertEquals(domain.model.entity.PlayerStatusEffect.CONFUSED, opponent.statusEffect());
        assertTrue(ball.powerEffectApplied());
    }
}
