package tests;

import domain.model.entity.Power;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.service.PlayerPowerService;
import domain.model.entity.service.PlayerProgression;
import domain.model.entity.service.PlayerStatusService;
import domain.model.game.lifecycle.GoalLimitRule;
import domain.model.game.lifecycle.MatchClock;
import domain.model.game.lifecycle.MatchLifecycle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainLifecycleServicesTest {
    @Test void matchLifecycleIsTerminalAndKeepsFirstResult() {
        MatchLifecycle lifecycle = new MatchLifecycle();
        assertEquals(MatchLifecycle.State.ACTIVE, lifecycle.state());
        lifecycle.finish(2);
        lifecycle.finish(1);
        assertEquals(MatchLifecycle.State.FINISHED, lifecycle.state());
        assertEquals(2, lifecycle.winner());
    }

    @Test void matchClockClampsAtZeroAndFormatsTime() {
        MatchClock clock = new MatchClock(65);
        assertEquals("01:05", clock.formatted());
        clock.advance(6.2);
        assertEquals("00:59", clock.formatted());
        clock.advance(100);
        assertTrue(clock.expired());
        assertEquals(0, clock.remainingSeconds());
    }

    @Test void goalLimitRuleDeterminesWinnerOnlyAtLimit() {
        assertEquals(-1, GoalLimitRule.winnerAtLimit(2, 1, 3));
        assertEquals(1, GoalLimitRule.winnerAtLimit(3, 1, 3));
        assertEquals(2, GoalLimitRule.winnerAtLimit(1, 3, 3));
    }

    @Test void powerChargeAndActivationRulesArePure() {
        assertEquals(50.0, PlayerPowerService.recharge(0, 5, 100, 10), 1e-9);
        assertEquals(100.0, PlayerPowerService.recharge(95, 2, 100, 10), 1e-9);
        assertFalse(PlayerPowerService.canActivate(Power.AURA, 100, 100, false));
        assertFalse(PlayerPowerService.canActivate(Power.NONE, 100, 100, true));
        assertTrue(PlayerPowerService.canActivate(Power.NONE, 100, 100, false));
    }

    @Test void timedStatusEffectExpiresCleanly() {
        var active = PlayerStatusService.tick(PlayerStatusEffect.CONFUSED, 2, 0.5);
        assertEquals(PlayerStatusEffect.CONFUSED, active.effect());
        assertEquals(1.5, active.remainingSeconds(), 1e-9);
        var expired = PlayerStatusService.tick(PlayerStatusEffect.CONFUSED, 0.2, 0.3);
        assertEquals(PlayerStatusEffect.NONE, expired.effect());
        assertEquals(0.0, expired.remainingSeconds(), 1e-9);
    }

    @Test void progressionIncreasesLevelAndDoublesUpgradeCost() {
        assertEquals(2, PlayerProgression.nextLevel(1, 10));
        assertEquals(1000, PlayerProgression.nextCost(500));
        assertThrows(IllegalStateException.class, () -> PlayerProgression.nextLevel(10, 10));
    }
}
