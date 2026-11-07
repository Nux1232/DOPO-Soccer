package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.game.Game;
import presentation.controller.input.PlayerInput;

public final class PhysicsEngine {
    private static final double GRAVITY = 1350;
    private static final double BALL_BOUNCE = 0.72;
    private static final double PLAYER_GROUND = Game.GROUND_Y - Player.RADIUS;

    public void update(Game match, PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) {
        double delta = Math.min(deltaSeconds, 1.0 / 30.0);
        Player first = match.firstPlayer();
        Player second = match.secondPlayer();
        Ball ball = match.ball();

        first.tick(delta);
        second.tick(delta);
        updatePlayer(first, firstInput, delta, !match.kickoffDelayActive());
        updatePlayer(second, secondInput, delta, !match.kickoffDelayActive());
        resolvePlayerCollision(first, second);

        updateBall(ball, delta);
        collideWithPlayer(ball, first);
        collideWithPlayer(ball, second);
        scoreIfGoal(match);
    }

    private void updatePlayer(Player player, PlayerInput input, double delta, boolean canControl) {
        double direction = canControl ? input.horizontalDirection() : 0;
        double speed = 190 + (player.stats().speed() - 5) * 18;
        player.setVelocity(direction * speed, player.velocityY());
        double x = clamp(player.x() + player.velocityX() * delta,
                Player.RADIUS, Game.WIDTH - Player.RADIUS);

        double velocityY = player.velocityY();
        if (canControl && input.jump() && player.grounded()) {
            velocityY = -(410 + (player.stats().jump() - 5) * 28);
            player.setGrounded(false);
        }
        velocityY += GRAVITY * delta;
        double y = player.y() + velocityY * delta;
        if (y >= PLAYER_GROUND) {
            y = PLAYER_GROUND;
            velocityY = 0;
            player.setGrounded(true);
        } else {
            player.setGrounded(false);
        }
        player.setPosition(x, y);
        player.setVelocity(direction * speed, velocityY);

        if (canControl && input.power()) {
            player.activatePower();
        }
    }

    private void resolvePlayerCollision(Player first, Player second) {
        double minimumDistance = Player.RADIUS * 2;
        double distance = Math.abs(first.x() - second.x());
        if (distance < minimumDistance) {
            double overlap = (minimumDistance - distance) / 2;
            double direction = first.x() <= second.x() ? -1 : 1;
            double firstX = clamp(first.x() + direction * overlap,
                    Player.RADIUS, Game.WIDTH - Player.RADIUS);
            double secondX = clamp(second.x() - direction * overlap,
                    Player.RADIUS, Game.WIDTH - Player.RADIUS);
            first.setPosition(firstX, first.y());
            second.setPosition(secondX, second.y());
        }
    }

    private void updateBall(Ball ball, double delta) {
        double velocityY = ball.velocityY() + GRAVITY * delta;
        double x = ball.x() + ball.velocityX() * delta;
        double y = ball.y() + velocityY * delta;
        double velocityX = ball.velocityX() * Math.pow(0.998, delta * 60);

        if (y + Ball.RADIUS >= Game.GROUND_Y) {
            y = Game.GROUND_Y - Ball.RADIUS;
            velocityY = -Math.abs(velocityY) * BALL_BOUNCE;
            velocityX *= 0.88;
            if (Math.abs(velocityY) < 70) {
                velocityY = 0;
            }
        }
        if (y - Ball.RADIUS <= 20) {
            y = 20 + Ball.RADIUS;
            velocityY = Math.abs(velocityY) * BALL_BOUNCE;
        }

        boolean insideLeftGoal = x < Game.GOAL_DEPTH;
        boolean insideRightGoal = x > Game.WIDTH - Game.GOAL_DEPTH;
        if ((insideLeftGoal || insideRightGoal) && y - Ball.RADIUS <= Game.GOAL_TOP
                && y + Ball.RADIUS >= Game.GOAL_TOP) {
            y = Game.GOAL_TOP + Ball.RADIUS;
            velocityY = Math.abs(velocityY) * BALL_BOUNCE;
        }

        boolean insideGoalOpening = y > Game.GOAL_TOP + Ball.RADIUS
                && y <= Game.GROUND_Y - Ball.RADIUS;
        if (x - Ball.RADIUS < 0 && !insideGoalOpening) {
            x = Ball.RADIUS;
            velocityX = Math.abs(velocityX) * BALL_BOUNCE;
        } else if (x + Ball.RADIUS > Game.WIDTH && !insideGoalOpening) {
            x = Game.WIDTH - Ball.RADIUS;
            velocityX = -Math.abs(velocityX) * BALL_BOUNCE;
        }
        ball.setPosition(x, y);
        ball.setVelocity(velocityX, velocityY);
    }

    private void collideWithPlayer(Ball ball, Player player) {
        double dx = ball.x() - player.x();
        double dy = ball.y() - (player.y() - 5);
        double distance = Math.hypot(dx, dy);
        double minimumDistance = Ball.RADIUS + Player.RADIUS;
        if (distance >= minimumDistance || distance == 0) {
            return;
        }
        double normalX = dx / distance;
        double normalY = dy / distance;
        ball.setPosition(player.x() + normalX * minimumDistance,
                player.y() - 5 + normalY * minimumDistance);
        double speed = Math.max(160, player.velocityX() * normalX + ball.velocityX() * 0.45);
        ball.setVelocity(normalX * speed, Math.min(ball.velocityY(), -100));
    }

    public void kick(Player player, Ball ball, int attackDirection) {
        if (player.kickCooldown() > 0
                || Math.hypot(ball.x() - player.x(), ball.y() - player.y()) > 85) {
            return;
        }
        double powerMultiplier = player.power() == domain.model.entity.Power.AURA ? 1.65 : 1;
        double kickStrength = (490 + player.stats().kick() * 32) * powerMultiplier;
        ball.setVelocity(attackDirection * kickStrength, -kickStrength * 0.46);
        player.startKickCooldown(0.48);
    }

    private void scoreIfGoal(Game match) {
        Ball ball = match.ball();
        boolean insideGoalHeight = ball.y() > Game.GOAL_TOP + Ball.RADIUS
                && ball.y() <= Game.GROUND_Y - Ball.RADIUS;
        if (!insideGoalHeight) {
            return;
        }
        if (ball.x() <= Ball.RADIUS) {
            match.scoreGoal(2);
        } else if (ball.x() >= Game.WIDTH - Ball.RADIUS) {
            match.scoreGoal(1);
        }
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
