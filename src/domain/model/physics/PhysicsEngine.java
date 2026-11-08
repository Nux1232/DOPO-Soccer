package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.game.Game;
import presentation.controller.input.PlayerInput;

/**
 * Motor de física del partido: mueve a los jugadores y al balón, resuelve
 * las colisiones entre ellos y detecta los goles.
 *
 * Sistema de coordenadas:el origen (0, 0) está en la esquina
 * superior izquierda de la cancha. El eje x crece hacia la derecha y el
 * eje y crece hacia abajo, por lo que una velocidad vertical negativa
 * significa subir. La posición (x, y) de los jugadores y del balón es la de
 * su centro. Las posiciones se miden en píxeles, las velocidades en píxeles
 * por segundo y los tiempos en segundos.
 *
 * El motor no guarda estado propio: en cada actualización lee y modifica
 * los objetos del partido Game, Player y Ball.
 * La patada no se ejecuta dentro de update; se pide por separado
 * con kick.
 */
public final class PhysicsEngine {

    /** Aceleración de la gravedad, en píxeles por segundo al cuadrado. */
    private static final double GRAVITY = 1350;

    /**
     * Fracción de la velocidad que conserva el balón al rebotar contra el
     * suelo, el techo, el larguero o las paredes (0.72 = pierde el 28 %).
     */
    private static final double BALL_BOUNCE = 0.72;

    /** Coordenada y del centro de un jugador cuando está apoyado en el suelo. */
    private static final double PLAYER_GROUND = Game.GROUND_Y - Player.RADIUS;

    /**
     * Avanza la simulación un paso de tiempo.
     *
     * En orden: actualiza los temporizadores de los jugadores, los mueve
     * según sus controles, separa a los jugadores si se solapan, mueve el
     * balón, resuelve sus choques con los jugadores y, por último, revisa si
     * hubo gol.
     *
     * @param match partido que se va a actualizar
     * @param firstInput controles del primer jugador
     * @param secondInput controles del segundo jugador
     * @param deltaSeconds segundos transcurridos desde la actualización
     *        anterior, se limita a 1/30 s para que la física no dé saltos grandes
     */
    public void update(Game match, PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) {
        // Si el juego se traba un momento, no se simula todo ese tiempo de golpe:
        // un paso enorme haría que el balón atravesara paredes y jugadores.
        double delta = Math.min(deltaSeconds, 1.0 / 30.0);
        Player first = match.firstPlayer();
        Player second = match.secondPlayer();
        Ball ball = match.ball();

        first.tick(delta);
        second.tick(delta);
        // Durante la espera del saque, los jugadores no pueden controlarse.
        updatePlayer(first, firstInput, delta, !match.kickoffDelayActive());
        updatePlayer(second, secondInput, delta, !match.kickoffDelayActive());
        resolvePlayerCollision(first, second);

        updateBall(ball, delta);
        collideWithPlayer(ball, first);
        collideWithPlayer(ball, second);
        scoreIfGoal(match);
    }

    /**
     * Mueve a un jugador: velocidad horizontal según su estadística de
     * velocidad, salto según su estadística de salto, gravedad y suelo.
     * También activa el poder si el jugador lo pide.
     *
     * @param player jugador a mover
     * @param input controles del jugador
     * @param delta segundos transcurridos
     * @param canControl false si el jugador no puede moverse ahora
     **/
    private void updatePlayer(Player player, PlayerInput input, double delta, boolean canControl) {
        // Movimiento horizontal: dirección (-1, 0 o 1) por la velocidad del jugador.
        double direction = canControl ? input.horizontalDirection() : 0;
        double speed = 190 + (player.stats().speed() - 5) * 18;
        player.setVelocity(direction * speed, player.velocityY());
        // clamp: la posición no puede salirse de la cancha.
        double x = clamp(player.x() + player.velocityX() * delta,
                Player.RADIUS, Game.WIDTH - Player.RADIUS);

        // Salto: solo si lo pide y está en el suelo. Como y crece hacia abajo,
        // subir es una velocidad negativa.
        double velocityY = player.velocityY();
        if (canControl && input.jump() && player.grounded()) {
            velocityY = -(410 + (player.stats().jump() - 5) * 28);
            player.setGrounded(false);
        }
        // Gravedad: la velocidad vertical cambia, y luego la posición cambia con ella.
        velocityY += GRAVITY * delta;
        double y = player.y() + velocityY * delta;
        if (y >= PLAYER_GROUND) {
            // Tocó el suelo: se queda sobre él y se detiene la caída.
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

    /**
     * Separa a los dos jugadores si se solapan, empujando a cada uno la
     * mitad de la superposición. Solo se compara la distancia horizontal.
     *
     * @param first primer jugador
     * @param second segundo jugador
     */
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

    /**
     * Mueve el balón: gravedad, rozamiento y rebotes contra el suelo, el
     * techo, el larguero de los arcos y las paredes. Las paredes solo
     * rebotan el balón fuera de la abertura de los arcos; por esa abertura
     * el balón puede entrar para marcar gol.
     *
     * @param ball balón a mover
     * @param delta segundos transcurridos
     */
    private void updateBall(Ball ball, double delta) {
        double velocityY = ball.velocityY() + GRAVITY * delta;
        double x = ball.x() + ball.velocityX() * delta;
        double y = ball.y() + velocityY * delta;
        // Rozamiento del aire: pierde un 0,2 % de velocidad horizontal por cada
        // 1/60 de segundo (el exponente ajusta el efecto al tiempo transcurrido).
        double velocityX = ball.velocityX() * Math.pow(0.998, delta * 60);

        // Suelo: el balón rebota hacia arriba perdiendo energía y se frena en x.
        if (y + Ball.RADIUS >= Game.GROUND_Y) {
            y = Game.GROUND_Y - Ball.RADIUS;
            velocityY = -Math.abs(velocityY) * BALL_BOUNCE;
            velocityX *= 0.88;
            // Si el rebote es muy pequeño, el balón se queda quieto en el suelo.
            if (Math.abs(velocityY) < 70) {
                velocityY = 0;
            }
        }
        // Techo de la cancha (y = 20): el balón rebota hacia abajo.
        if (y - Ball.RADIUS <= 20) {
            y = 20 + Ball.RADIUS;
            velocityY = Math.abs(velocityY) * BALL_BOUNCE;
        }

        // Larguero: dentro de la zona de un arco, el balón rebota contra la
        // altura GOAL_TOP.
        boolean insideLeftGoal = x < Game.GOAL_DEPTH;
        boolean insideRightGoal = x > Game.WIDTH - Game.GOAL_DEPTH;
        if ((insideLeftGoal || insideRightGoal) && y - Ball.RADIUS <= Game.GOAL_TOP
                && y + Ball.RADIUS >= Game.GOAL_TOP) {
            y = Game.GOAL_TOP + Ball.RADIUS;
            velocityY = Math.abs(velocityY) * BALL_BOUNCE;
        }

        // Paredes laterales: rebotan el balón, salvo que esté a la altura de
        // la abertura del arco, por donde puede entrar.
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

    /**
     * Resuelve el choque entre el balón y un jugador. Si se solapan, el
     * balón se coloca justo fuera del jugador y sale rebotado en la
     * dirección contraria al choque, siempre con algo de velocidad hacia arriba.
     * Se usa como centro del jugador un punto 5 píxeles por encima de su centro.
     *
     * @param ball balón
     * @param player jugador con el que puede chocar
     */
    private void collideWithPlayer(Ball ball, Player player) {
        double dx = ball.x() - player.x();
        double dy = ball.y() - (player.y() - 5);
        double distance = Math.hypot(dx, dy);
        double minimumDistance = Ball.RADIUS + Player.RADIUS;
        if (distance >= minimumDistance || distance == 0) {
            return;
        }
        // (normalX, normalY) es la dirección del jugador hacia el balón, con longitud 1.
        double normalX = dx / distance;
        double normalY = dy / distance;
        ball.setPosition(player.x() + normalX * minimumDistance,
                player.y() - 5 + normalY * minimumDistance);
        double speed = Math.max(160, player.velocityX() * normalX + ball.velocityX() * 0.45);
        ball.setVelocity(normalX * speed, Math.min(ball.velocityY(), -100));
    }

    /**
     * Intenta patear el balón. No hace nada si el jugador aún está en
     * espera de patada o si el balón está a más de 85 píxeles de su centro.
     *
     * La fuerza de la patada depende de la estadística de patada y se
     * multiplica por 1,65 si el jugador tiene el poder (aura) activo.
     * Después de patear, el jugador debe esperar 0,48 segundos.
     *
     * @param player jugador que patea
     * @param ball balón
     * @param attackDirection dirección horizontal hacia el arco rival.
     */
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

    /**
     * Revisa si el balón entró a un arco. Si entró en el arco izquierdo, el
     * gol es del jugador 2; si entró en el derecho, del jugador 1. El gol se
     * registra en el partido, que también reinicia las posiciones.
     *
     * @param match partido en curso
     */
    private void scoreIfGoal(Game match) {
        Ball ball = match.ball();
        // Solo cuenta si el balón está a la altura de la abertura del arco.
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

    /**
     * Limita un valor a un rango.
     *
     * @param value valor a limitar
     * @param minimum límite inferior
     * @param maximum límite superior
     * @return el mismo valor si está dentro del rango; si no, el límite más cercano
     */
    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}