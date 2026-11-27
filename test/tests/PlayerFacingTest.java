package tests;

import domain.factory.CountryFactory;

import domain.model.entity.Ball;
import domain.model.entity.LPlayer;
import domain.model.entity.Player;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import domain.model.game.PlayerMode;
import domain.model.physics.PhysicsEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerFacingTest {

    @Test
    void kickAnimationAndFacingFollowKickDirection() throws Exception {
        Player player = new LPlayer(CountryFactory.create("south-korea"), 200);
        player.setPosition(200, 200);
        Ball ball = new Ball(180, 200);

        new PhysicsEngine().kick(player, ball, -1);

        assertEquals(-1, player.facingDirection());
        assertTrue(player.kicking());
        assertTrue(ball.velocityX() < 0);

        player.tick(Player.KICK_ANIMATION_SECONDS);
        assertFalse(player.kicking());
    }

    @Test
    void runningDirectionChangesFacingAndNeutralInputKeepsIt() throws Exception {
        PhysicsEngine physics = new PhysicsEngine();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("nigeria"),
                CountryFactory.create("usa"),
                physics);
        game.updateKickoffDelay(2);
        Player player = game.firstPlayer();

        physics.update(
                game,
                new PlayerInput(true, false, false, false, false),
                PlayerInput.NONE,
                1.0 / 60.0);
        assertEquals(-1, player.facingDirection());

        physics.update(game, PlayerInput.NONE, PlayerInput.NONE, 1.0 / 60.0);
        assertEquals(-1, player.facingDirection());
    }
}
