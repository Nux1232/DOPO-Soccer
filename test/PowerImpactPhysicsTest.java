package tests;

import domain.factory.CountryFactory;
import domain.model.entity.Country;
import domain.model.entity.LPlayer;
import domain.model.entity.Player;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.RPlayer;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.physics.PhysicsEngine;
import domain.model.physics.PlayerInputHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerImpactPhysicsTest {

    @Test
    void powersDealMoreDamageAndApplyDistinctPhysicalKnockback() throws Exception {
        Player owner = new LPlayer(CountryFactory.create("USA"), 100);
        Player target = new RPlayer(CountryFactory.create("CAMEROON"), 700);

        Country southKorea = CountryFactory.create("KR");
        southKorea.applyImpact(owner, target);
        assertEquals(820, target.velocityX());
        assertEquals(-440, target.velocityY());
        assertEquals(0.55, target.impactLockTimer());
        assertEquals(40, target.health());

        target = new RPlayer(CountryFactory.create("CAMEROON"), 700);
        Country cameroon = CountryFactory.create("CM");
        cameroon.applyImpact(owner, target);
        assertEquals(640, target.velocityX());
        assertEquals(-300, target.velocityY());
        assertEquals(0.4, target.impactLockTimer());
        assertEquals(16, target.health());
        assertFalse(target.canKick());
        assertEquals(0.25, target.statusEffect().movementMultiplier());

        target = new RPlayer(CountryFactory.create("CAMEROON"), 700);
        Country nigeria = CountryFactory.create("NG");
        nigeria.applyImpact(owner, target);
        assertEquals(420, target.velocityX());
        assertEquals(-900, target.velocityY());
        assertEquals(PlayerStatusEffect.UNCONSCIOUS, target.statusEffect());

        target = new RPlayer(CountryFactory.create("CAMEROON"), 700);
        Country usa = CountryFactory.create("USA");
        usa.applyImpact(owner, target);
        assertEquals(560, target.velocityX());
        assertEquals(-500, target.velocityY());
        assertEquals(PlayerStatusEffect.CONFUSED, target.statusEffect());
    }

    @Test
    void physicsPreservesImpactMomentumAgainstPlayerInput() throws Exception {
        PhysicsEngine physics = new PhysicsEngine();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("KR"),
                CountryFactory.create("CM"),
                physics);
        game.restoreKickoffDelay(0);

        Player target = game.secondPlayer();
        target.setPosition(700, 300);
        CountryFactory.create("KR").applyImpact(game.firstPlayer(), target);

        physics.update(
                game,
                PlayerInput.NONE,
                new PlayerInput(true, false, false, false, false),
                1.0 / 60.0);

        assertTrue(target.x() > 700);
        assertTrue(target.velocityX() > 0);
        assertTrue(target.velocityY() < 0);
        assertTrue(target.impactLocked());
    }

    @Test
    void confusionReversesMovementAfterTheImpactLaunch() {
        Player player = new LPlayer(CountryFactory.create("USA"), 100);
        PlayerInputHandler inputHandler = new PlayerInputHandler();
        player.applyStatusEffect(PlayerStatusEffect.CONFUSED, 2.0);

        inputHandler.apply(player, new PlayerInput(false, true, false, false, false), false);

        assertEquals(-182, player.velocityX());
        assertEquals(-1, player.facingDirection());
    }
}
