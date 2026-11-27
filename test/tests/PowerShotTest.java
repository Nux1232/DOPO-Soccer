package tests;

import domain.factory.CountryFactory;
import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.CountryCatalog;
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
    void catalogOwnsCountryChoicesAndCountriesCanReuseIndependentPowers() {
        Country southKorea = CountryCatalog.get("KR");
        Country sameCountry = CountryCatalog.get("south-korea");
        Country customCountry = new Country(
                "custom-country", "País de prueba", "#ffffff", "custom.png", southKorea.power());

        assertSame(southKorea, sameCountry);
        assertEquals(4, CountryCatalog.all().size());
        assertSame(southKorea.power(), customCountry.power());
        assertEquals("custom-country", customCountry.id());
    }

    @Test
    void countriesOwnPowerConfigurationAndExposeDistinctDamage() {
        Country southKorea = CountryFactory.create("KR");
        Country cameroon = CountryFactory.create("CM");
        Country nigeria = CountryFactory.create("NG");
        Country usa = CountryFactory.create("USA");

        // Daños base del Anexo 1: Corea=40, Camerún=60, Nigeria=40, USA=30
        assertEquals(40, southKorea.power().damage());
        assertEquals(60, cameroon.power().damage());
        assertEquals(40, nigeria.power().damage());
        assertEquals(30, usa.power().damage());
        assertEquals(1.5, southKorea.power().durationSeconds());
        assertEquals(3.0, cameroon.power().durationSeconds());
        assertEquals(3.0, nigeria.power().durationSeconds());
        assertEquals(2.5, usa.power().durationSeconds());
        assertEquals("Ko_power.gif", southKorea.power().visualAsset());
        assertEquals("Cm_power.gif", cameroon.power().visualAsset());
        assertEquals("Ng_power.gif", nigeria.power().visualAsset());
        assertEquals("usa-illusion.gif", usa.power().visualAsset());
        assertTrue(usa.power().renderCopies());
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
        assertSame(country.power(), ball.activePowerShot());
        assertSame(kicker, ball.powerShotOwner());
        assertEquals(country.power().horizontalSpeed(), ball.velocityX());
        assertEquals(country.power().verticalSpeed(), ball.velocityY());
    }

    @Test
    void touchingTheBallWithAuraLaunchesPowerWithoutPressingKick() throws Exception {
        PhysicsEngine physics = new PhysicsEngine();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("KR"),
                CountryFactory.create("CM"),
                physics);
        game.restoreKickoffDelay(0);

        Player player = game.firstPlayer();
        player.tick(Player.POWER_CHARGE_SECONDS);
        assertTrue(player.activatePower());
        game.ball().setPosition(player.x() + 20, player.y());

        physics.update(game, PlayerInput.NONE, PlayerInput.NONE, 1.0 / 60.0);

        assertEquals(PowerState.NONE, player.power());
        assertSame(player.country().power(), game.ball().activePowerShot());
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

        // Camerún Kick=1: daño = 60 × 1.0 = 60 → salud = 100 - 60 = 40 (Anexo 1/2)
        assertEquals(40, target.health(), 0.01);
        assertEquals(PlayerStatusEffect.ELECTRIFIED, target.statusEffect());
        assertFalse(target.canJump());
        assertFalse(target.canKick());
        // ELECTRIFIED: 50% de velocidad (Anexo 1)
        assertEquals(0.50, target.statusEffect().movementMultiplier());

        target.tick(4.0);
        assertEquals(PlayerStatusEffect.NONE, target.statusEffect());
        assertTrue(target.canJump());
    }

    @Test
    void activePowerExpiresAndRemovesItsVisualState() {
        Ball ball = new Ball(100, 100);
        Player owner = new LPlayer(CountryFactory.create("USA"), 100);
        Power power = owner.country().power();
        ball.startPowerShot(power, owner);

        ball.tickPowerShot(power.durationSeconds() - 0.01);
        assertSame(power, ball.activePowerShot());
        ball.tickPowerShot(0.02);

        assertNull(ball.activePowerShot());
        assertEquals(0, ball.powerShotTime());
        assertFalse(ball.powerEffectApplied());
    }
}
