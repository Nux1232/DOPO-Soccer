package tests;

import domain.factory.CountryFactory;
import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.CountryCatalog;
import domain.model.entity.LPlayer;
import domain.model.entity.Player;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.ProjectilePower;
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
        assertEquals("Camerún", CountryCatalog.get("CM").displayName());
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
        assertEquals(40, projectile(southKorea).damage());
        assertEquals(60, projectile(cameroon).damage());
        assertEquals(40, projectile(nigeria).damage());
        assertEquals(30, projectile(usa).damage());
        assertEquals(1.5, projectile(southKorea).durationSeconds());
        assertEquals(3.0, projectile(cameroon).durationSeconds());
        assertEquals(3.0, projectile(nigeria).durationSeconds());
        assertEquals(2.5, projectile(usa).durationSeconds());
        assertEquals("Ko_power.gif", projectile(southKorea).visualAsset());
        assertEquals("Cm_power.gif", projectile(cameroon).visualAsset());
        assertEquals("Ng_power.gif", projectile(nigeria).visualAsset());
        assertEquals("usa-illusion.gif", projectile(usa).visualAsset());
        assertTrue(projectile(usa).renderCopies());
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
        assertSame(projectile(country), ball.activePowerShot());
        assertSame(kicker, ball.powerShotOwner());
        assertEquals(projectile(country).horizontalSpeed(), ball.velocityX());
        assertEquals(projectile(country).verticalSpeed(), ball.velocityY());
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
        assertSame(projectile(player.country()), game.ball().activePowerShot());
    }

    @Test
    void nonProjectilePowersCanImplementTheirOwnActivationBehavior() throws Exception {
        Power power = new Power() {
            @Override
            public String id() {
                return "temporary-confusion";
            }

            @Override
            public void activate(domain.model.entity.PowerActivationContext context) {
                context.owner().applyStatusEffect(PlayerStatusEffect.CONFUSED, 2.0);
            }
        };
        Country ownerCountry = new Country("custom", "Personalizado", "#ffffff", "custom.png", power);
        PhysicsEngine physics = new PhysicsEngine();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                ownerCountry,
                CountryFactory.create("CM"),
                physics);
        game.restoreKickoffDelay(0);
        Player owner = game.firstPlayer();
        owner.tick(Player.POWER_CHARGE_SECONDS);
        assertTrue(owner.activatePower());
        game.ball().setPosition(owner.x() + 20, owner.y());

        physics.update(game, PlayerInput.NONE, PlayerInput.NONE, 1.0 / 60.0);

        assertEquals(PowerState.NONE, owner.power());
        assertEquals(PlayerStatusEffect.CONFUSED, owner.statusEffect());
        assertNull(game.ball().activePowerShot());
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
        ProjectilePower power = (ProjectilePower) owner.country().power();
        ball.startPowerShot(power, owner);

        ball.tickPowerShot(power.durationSeconds() - 0.01);
        assertSame(power, ball.activePowerShot());
        ball.tickPowerShot(0.02);

        assertNull(ball.activePowerShot());
        assertEquals(0, ball.powerShotTime());
        assertFalse(ball.powerEffectApplied());
    }

    private ProjectilePower projectile(Country country) {
        return (ProjectilePower) country.power();
    }
}
