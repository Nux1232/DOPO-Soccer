package tests;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.Power;
import domain.model.entity.PowerShot;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.physics.PhysicsEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerShotTest {

    @Test
    void countriesExposeTheirConfiguredPowerShots() {
        assertEquals(PowerShot.BLUE_AURA, Country.SOUTH_KOREA.powerShot());
        assertEquals(PowerShot.ELECTRIC_BOLT, Country.CAMEROON.powerShot());
        assertEquals(PowerShot.TORNADO, Country.NIGERIA.powerShot());
        assertEquals(PowerShot.ILLUSION, Country.USA.powerShot());
        assertEquals(40, PowerShot.BLUE_AURA.damage());
        assertEquals(60, PowerShot.ELECTRIC_BOLT.damage());
        assertEquals(40, PowerShot.TORNADO.damage());
        assertEquals(30, PowerShot.ILLUSION.damage());
    }

    @Test
    void specialKickConsumesAuraAndUsesCountryShotConfiguration() throws Exception {
        Player kicker = new Player(Country.SOUTH_KOREA, 100);
        kicker.setPosition(100, 100);
        kicker.tick(Player.POWER_CHARGE_SECONDS);
        assertTrue(kicker.activatePower());

        Ball ball = new Ball(120, 100);
        new PhysicsEngine().kick(kicker, ball, 1);

        assertEquals(Power.NONE, kicker.power());
        assertEquals(PowerShot.BLUE_AURA, ball.activePowerShot());
        assertEquals(kicker, ball.powerShotOwner());
        assertEquals(820, ball.velocityX());
        assertEquals(0, ball.velocityY());
    }

    @Test
    void specialShotImpactAppliesDamageAndTemporaryPlayerEffect() throws Exception {
        PhysicsEngine physics = new PhysicsEngine();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                Country.CAMEROON,
                Country.USA,
                physics);
        game.updateKickoffDelay(2);

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

        assertEquals(40, target.health(), 0.01);
        assertEquals(PlayerStatusEffect.ELECTRIFIED, target.statusEffect());
        assertFalse(target.canJump());
        assertEquals(0.5, target.statusEffect().movementMultiplier());

        target.tick(3.0);
        assertEquals(PlayerStatusEffect.NONE, target.statusEffect());
        assertTrue(target.canJump());
    }

    @Test
    void illusionShotExpiresAndRemovesItsVisualState() {
        Ball ball = new Ball(100, 100);
        Player owner = new Player(Country.USA, 100);
        ball.startPowerShot(PowerShot.ILLUSION, owner);

        ball.tickPowerShot(2.49);
        assertEquals(PowerShot.ILLUSION, ball.activePowerShot());
        ball.tickPowerShot(0.02);

        assertNull(ball.activePowerShot());
        assertEquals(0, ball.powerShotTime());
        assertFalse(ball.powerEffectApplied());
    }
}
