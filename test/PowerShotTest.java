package tests;

import domain.factory.CountryFactory;
import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.LPlayer;
import domain.model.entity.Player;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.Power;
import domain.model.entity.PowerState;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.physics.PhysicsEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerShotTest {

    @Test
    void countriesOwnPowerConfigurationAndExposeDistinctDamage() {
        Country southKorea = CountryFactory.create("KR");
        Country cameroon = CountryFactory.create("CM");
        Country nigeria = CountryFactory.create("NG");
        Country usa = CountryFactory.create("USA");

        assertEquals(60, southKorea.damage());
        assertEquals(84, cameroon.damage());
        assertEquals(60, nigeria.damage());
        assertEquals(48, usa.damage());
        assertEquals("Ko_power.gif", southKorea.visualAsset());
        assertEquals("Cm_power.gif", cameroon.visualAsset());
        assertEquals("Ng_power.gif", nigeria.visualAsset());
        assertEquals("usa-illusion.gif", usa.visualAsset());
        assertTrue(usa.renderCopies());
    }

    @Test
    void specialKickUsesTheCountryPowerAndConsumesAura() throws Exception {
        Country country = CountryFactory.create("KR");
        Player kicker = new LPlayer(country, 100);
        kicker.setPosition(100, 100);
        kicker.tick(Player.POWER_CHARGE_SECONDS);
        assertTrue(kicker.activatePower());

        Ball ball = new Ball(120, 100);
        new PhysicsEngine().kick(kicker, ball, 1);

        assertEquals(PowerState.NONE, kicker.power());
        assertSame(country, ball.activePowerShot());
        assertSame(kicker, ball.powerShotOwner());
        assertEquals(country.horizontalSpeed(), ball.velocityX());
        assertEquals(country.verticalSpeed(), ball.velocityY());
    }

    @Test
    void countryPowerImpactAppliesDamageAndTemporaryEffect() throws Exception {
        PhysicsEngine physics = new PhysicsEngine();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("CM"),
                CountryFactory.create("USA"),
                physics);
        game.restoreKickoffDelay(0);

        Player attacker = game.firstPlayer();
        Player target = game.secondPlayer();
        attacker.setPosition(300, 300);
        target.setPosition(400, 300);
        attacker.tick(Player.POWER_CHARGE_SECONDS);
        attacker.activatePower();

        Ball ball = game.ball();
        ball.setPosition(300, 300);
        physics.kick(attacker, target, ball, 1);
        ball.setPosition(target.x() - 5, target.y());

        physics.update(game, PlayerInput.NONE, PlayerInput.NONE, 1.0 / 60.0);

        assertEquals(16, target.health(), 0.01);
        assertEquals(PlayerStatusEffect.ELECTRIFIED, target.statusEffect());
        assertFalse(target.canJump());
        assertFalse(target.canKick());
        assertEquals(0.25, target.statusEffect().movementMultiplier());

        target.tick(4.0);
        assertEquals(PlayerStatusEffect.NONE, target.statusEffect());
        assertTrue(target.canJump());
    }

    @Test
    void activePowerExpiresAndRemovesItsVisualState() {
        Ball ball = new Ball(100, 100);
        Player owner = new LPlayer(CountryFactory.create("USA"), 100);
        Power power = owner.country();
        ball.startPowerShot(power, owner);

        ball.tickPowerShot(power.durationSeconds() - 0.01);
        assertSame(power, ball.activePowerShot());
        ball.tickPowerShot(0.02);

        assertNull(ball.activePowerShot());
        assertEquals(0, ball.powerShotTime());
        assertFalse(ball.powerEffectApplied());
    }
}
