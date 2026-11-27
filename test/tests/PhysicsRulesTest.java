package tests;

import domain.factory.CountryFactory;
import domain.model.entity.Ball;
import domain.model.entity.LPlayer;
import domain.model.entity.Player;
import domain.model.entity.RPlayer;
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
    void impactIsDelegatedToTheCountryPowerBehavior() throws Exception {
        Player owner = new LPlayer(CountryFactory.create("USA"), 100);
        Player opponent = new RPlayer(CountryFactory.create("CAMEROON"), 120);
        opponent.setPosition(120, 100);
        Ball ball = new Ball(120, 100);
        ball.startPowerShot(owner.country().power(), owner);
        PowerShotImpactHandler handler = new PowerShotImpactHandler();

        handler.apply(ball, owner, opponent);

        assertEquals(70, opponent.health());
        assertEquals(domain.model.entity.PlayerStatusEffect.CONFUSED, opponent.statusEffect());
        assertTrue(ball.powerEffectApplied());
    }
}
