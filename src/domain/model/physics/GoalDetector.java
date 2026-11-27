package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.game.Game;
import java.util.OptionalInt;

/** Regla de anotación intercambiable para detectar goles en una cancha. */
public interface GoalDetector {
    /**
     * Evalúa la posición actual del balón para determinar si se anotó un gol.
     *
     * @param ball balón del partido
     * @return jugador anotador ({@code 1} o {@code 2}), o vacío si no hubo gol
     */
    OptionalInt scoringPlayer(Ball ball);

    /**
     * Construye el detector de goles estándar acorde con las dimensiones de {@link Game}.
     *
     * @return detector estándar para los arcos izquierdo y derecho
     */
    static GoalDetector standardField() {
        return ball -> {
            if (ball.y() < Game.GOAL_TOP || ball.y() > Game.GROUND_Y) return OptionalInt.empty();
            if (ball.x() - Ball.RADIUS <= Game.GOAL_DEPTH) return OptionalInt.of(2);
            if (ball.x() + Ball.RADIUS >= Game.WIDTH - Game.GOAL_DEPTH) return OptionalInt.of(1);
            return OptionalInt.empty();
        };
    }
}
