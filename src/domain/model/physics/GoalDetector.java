package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.game.Game;
import java.util.OptionalInt;

/** Regla de anotación intercambiable para una cancha. */
public interface GoalDetector {
    OptionalInt scoringPlayer(Ball ball);

    static GoalDetector standardField() {
        return ball -> {
            if (ball.y() < Game.GOAL_TOP || ball.y() > Game.GROUND_Y) return OptionalInt.empty();
            if (ball.x() - Ball.RADIUS <= Game.GOAL_DEPTH) return OptionalInt.of(2);
            if (ball.x() + Ball.RADIUS >= Game.WIDTH - Game.GOAL_DEPTH) return OptionalInt.of(1);
            return OptionalInt.empty();
        };
    }
}
