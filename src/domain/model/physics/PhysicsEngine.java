package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.Power;
import domain.model.entity.PowerShot;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

/**
 * Motor de física del partido basado en Dyn4j.
 *
 * Convierte las coordenadas lógicas de la cancha (en píxeles, con el eje Y
 * creciendo hacia abajo) al mundo físico de Dyn4j (en metros) a razón de
 * 100 píxeles por metro.
 */
public class PhysicsEngine implements PhysicsSimulation {

    /** Escala de conversión: 100 píxeles equivalen a 1 metro en Dyn4j. */
    private static final double PIXELS_PER_METER = 100.0;

    /** Gravedad vertical arcade hacia abajo (px/s^2 -> 14.0 m/s^2 en Dyn4j). */
    private static final double GRAVITY_Y_METERS = 14.0;

    /** Velocidad horizontal base de los jugadores (px/s). */
    private static final double PLAYER_MOVE_SPEED = 280.0;

    /** Velocidad vertical de salto de los jugadores (px/s, negativa = hacia arriba). */
    private static final double PLAYER_JUMP_SPEED = -560.0;

    /** Alcance máximo para patear el balón (en píxeles). */
    private static final double KICK_RANGE = Player.RADIUS + Ball.RADIUS + 30.0;

    /** Velocidades que imprime la patada al balón (px/s) y tiempo de espera (s). */
    private static final double KICK_SPEED_X = 560.0;
    private static final double KICK_SPEED_Y = -420.0;
    private static final double KICK_COOLDOWN_SECONDS = 0.42;

    /** Umbral mínimo de velocidad (px/s) debajo del cual el balón se detiene en el suelo. */
    private static final double BALL_STOP_SPEED_X = 8.0;
    private static final double BALL_STOP_SPEED_Y = 22.0;

    /** Mundo físico de Dyn4j y cuerpos dinámicos. */
    private final World<Body> world;
    private final Body ballBody;
    private final Body firstPlayerBody;
    private final Body secondPlayerBody;

    /**
     * Crea el mundo de Dyn4j con límites estáticos y cuerpos dinámicos.
     */
    public PhysicsEngine() {
        this.world = new World<>();
        this.world.setGravity(new Vector2(0, GRAVITY_Y_METERS));

        createStaticBoundaries();
        this.ballBody = createBallBody();
        this.firstPlayerBody = createPlayerBody();
        this.secondPlayerBody = createPlayerBody();

        this.world.addBody(ballBody);
        this.world.addBody(firstPlayerBody);
        this.world.addBody(secondPlayerBody);
    }

    /**
     * Avanza la simulación física un cuadro usando Dyn4j.
     */
    @Override
    public void update(
            Game game,
            PlayerInput firstInput,
            PlayerInput secondInput,
            double deltaSeconds) throws DopoSoccerException {
        double dt = Math.min(deltaSeconds, 1.0 / 30.0);
        if (dt <= 0) {
            return;
        }
        boolean kickoffLocked = game.kickoffDelayActive();

        Player first = game.firstPlayer();
        Player second = game.secondPlayer();
        Ball ball = game.ball();

        first.tick(dt);
        second.tick(dt);
        ball.tickPowerShot(dt);
        ballBody.getFixture(0).setRestitution(
                ball.activePowerShot() == PowerShot.TORNADO ? 0.95 : 0.72);

        // 1. Aplicar entradas de control a los jugadores
        applyPlayerInput(first, firstInput, kickoffLocked);
        applyPlayerInput(second, secondInput, kickoffLocked);

        // 2. Sincronizar estado del dominio -> cuerpos de Dyn4j
        syncPlayerToBody(first, firstPlayerBody);
        syncPlayerToBody(second, secondPlayerBody);
        syncBallToBody(ball, ballBody);

        // 3. Ejecutar el paso de simulación en Dyn4j
        world.step(1, dt);

        // 4. Leer las nuevas posiciones y velocidades de Dyn4j -> dominio
        syncBodyToPlayer(firstPlayerBody, first, kickoffLocked);
        syncBodyToPlayer(secondPlayerBody, second, kickoffLocked);
        syncBodyToBall(ballBody, ball);
        applyPowerShotImpact(ball, first, second);

        // 5. Verificar si se anotó un gol
        scoreIfGoal(game);
    }

    /**
     * Sobrecarga compatible con las pruebas unitarias para patear únicamente el balón.
     */
    public void kick(Player player, Ball ball, int direction) throws DopoSoccerException {
        kick(player, null, ball, direction);
    }

    /**
     * Ejecuta la patada de un jugador.
     * - Si está aturdido o en espera (cooldown), no hace nada.
     * - Si el balón está al alcance, impulsa únicamente el balón (Tabla 4, Anexo 2).
     * - Si no toca el balón pero el rival está al alcance del pie, le aplica
     *   10 * multiplicadorKick de daño.
     */
    @Override
    public void kick(Player kicker, Player opponent, Ball ball, int direction) throws DopoSoccerException {
        if (kicker.kickCooldown() > 0 || !kicker.canKick()) {
            return;
        }
        int dir = direction >= 0 ? 1 : -1;
        double dxBall = ball.x() - kicker.x();
        double dyBall = ball.y() - kicker.y();
        double distBall = Math.hypot(dxBall, dyBall);

        if (distBall <= KICK_RANGE) {
            PowerShot powerShot = kicker.power() == Power.AURA
                    ? kicker.country().powerShot() : null;
            if (powerShot != null && kicker.consumePower()) {
                ball.startPowerShot(powerShot, kicker);
                ball.setVelocity(
                        dir * powerShot.horizontalSpeed(),
                        powerShot.verticalSpeed());
                ballBody.getFixture(0).setRestitution(
                        powerShot == PowerShot.TORNADO ? 0.95 : 0.72);
            } else {
                ball.clearPowerShot();
                ball.setVelocity(dir * KICK_SPEED_X, KICK_SPEED_Y);
                ballBody.getFixture(0).setRestitution(0.72);
            }
            syncBallToBody(ball, ballBody);
            kicker.startKickCooldown(KICK_COOLDOWN_SECONDS);
            return;
        }

        if (opponent != null) {
            double dxOpp = opponent.x() - kicker.x();
            double dyOpp = opponent.y() - kicker.y();
            double distOpp = Math.hypot(dxOpp, dyOpp);
            boolean facingOpponent = (dir > 0 && dxOpp >= -Player.RADIUS) || (dir < 0 && dxOpp <= Player.RADIUS);
            if (distOpp <= KICK_RANGE + Player.RADIUS * 0.5 && facingOpponent) {
                opponent.takeDamage(kicker.kickDamage());
                kicker.startKickCooldown(KICK_COOLDOWN_SECONDS);
            }
        }
    }

    // ==================== Configuración de Cuerpos Dyn4j ====================

    private void createStaticBoundaries() {
        double w = toMeters(Game.WIDTH);
        double groundY = toMeters(Game.GROUND_Y);
        double goalTopY = toMeters(Game.GOAL_TOP);
        double goalDepth = toMeters(Game.GOAL_DEPTH);
        double thickness = 1.0;

        // Suelo (borde superior exactamente en Y = Game.GROUND_Y)
        Body ground = new Body();
        BodyFixture groundFixture = ground.addFixture(Geometry.createRectangle(w + 4.0, thickness));
        groundFixture.setRestitution(0.68);
        groundFixture.setFriction(0.65);
        ground.translate(w / 2.0, groundY + thickness / 2.0);
        ground.setMass(MassType.INFINITE);
        world.addBody(ground);

        // Techo (borde inferior en Y = 0)
        Body ceiling = new Body();
        BodyFixture ceilingFixture = ceiling.addFixture(Geometry.createRectangle(w + 4.0, thickness));
        ceilingFixture.setRestitution(0.6);
        ceiling.translate(w / 2.0, -thickness / 2.0);
        ceiling.setMass(MassType.INFINITE);
        world.addBody(ceiling);

        // Pared superior izquierda (por encima del arco izquierdo: de Y = 0 a Y = GOAL_TOP)
        Body leftUpperWall = new Body();
        BodyFixture leftWallFix = leftUpperWall.addFixture(Geometry.createRectangle(thickness, goalTopY));
        leftWallFix.setRestitution(0.75);
        leftUpperWall.translate(-thickness / 2.0, goalTopY / 2.0);
        leftUpperWall.setMass(MassType.INFINITE);
        world.addBody(leftUpperWall);

        // Pared superior derecha (por encima del arco derecho: de Y = 0 a Y = GOAL_TOP)
        Body rightUpperWall = new Body();
        BodyFixture rightWallFix = rightUpperWall.addFixture(Geometry.createRectangle(thickness, goalTopY));
        rightWallFix.setRestitution(0.75);
        rightUpperWall.translate(w + thickness / 2.0, goalTopY / 2.0);
        rightUpperWall.setMass(MassType.INFINITE);
        world.addBody(rightUpperWall);

        // Paredes traseras de los arcos (en X = 0 y X = Game.WIDTH)
        double goalHeight = groundY - goalTopY;
        Body leftGoalBack = new Body();
        leftGoalBack.addFixture(Geometry.createRectangle(thickness, goalHeight)).setRestitution(0.5);
        leftGoalBack.translate(-thickness / 2.0, goalTopY + goalHeight / 2.0);
        leftGoalBack.setMass(MassType.INFINITE);
        world.addBody(leftGoalBack);

        Body rightGoalBack = new Body();
        rightGoalBack.addFixture(Geometry.createRectangle(thickness, goalHeight)).setRestitution(0.5);
        rightGoalBack.translate(w + thickness / 2.0, goalTopY + goalHeight / 2.0);
        rightGoalBack.setMass(MassType.INFINITE);
        world.addBody(rightGoalBack);

        // Travesaños de los arcos (horizontales en Y = GOAL_TOP)
        Body leftCrossbar = new Body();
        leftCrossbar.addFixture(Geometry.createRectangle(goalDepth, 0.08)).setRestitution(0.75);
        leftCrossbar.translate(goalDepth / 2.0, goalTopY);
        leftCrossbar.setMass(MassType.INFINITE);
        world.addBody(leftCrossbar);

        Body rightCrossbar = new Body();
        rightCrossbar.addFixture(Geometry.createRectangle(goalDepth, 0.08)).setRestitution(0.75);
        rightCrossbar.translate(w - goalDepth / 2.0, goalTopY);
        rightCrossbar.setMass(MassType.INFINITE);
        world.addBody(rightCrossbar);
    }

    private Body createBallBody() {
        Body body = new Body();
        BodyFixture fixture = body.addFixture(Geometry.createCircle(toMeters(Ball.RADIUS)));
        fixture.setDensity(1.0);
        fixture.setRestitution(0.72);
        fixture.setFriction(0.45);
        body.setLinearDamping(0.35);
        body.setAngularDamping(0.8);
        body.setMass(MassType.NORMAL);
        return body;
    }

    private Body createPlayerBody() {
        Body body = new Body();
        double radiusM = toMeters(Player.RADIUS);
        // El radio exacto coincide con Player.RADIUS para no enterrarse en el suelo
        BodyFixture fixture = body.addFixture(Geometry.createCircle(radiusM));
        fixture.setDensity(5.0);
        fixture.setRestitution(0.0);
        fixture.setFriction(0.0);
        body.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        return body;
    }

    private void applyPlayerInput(Player player, PlayerInput input, boolean kickoffLocked) {
        if (kickoffLocked || player.stunned()) {
            player.setVelocity(0, player.velocityY());
            return;
        }
        if (input.power() && player.statusEffect().canKick()) {
            player.activatePower();
        }
        if (player.statusEffect() == PlayerStatusEffect.PUSHED) {
            return;
        }
        if (!player.statusEffect().canMove()) {
            player.setVelocity(0, player.velocityY());
            return;
        }
        double speedMultiplier = 1.0 + 0.10 * (player.stats().speed() - 1);
        double vx = input.horizontalDirection() * PLAYER_MOVE_SPEED * speedMultiplier
                * player.statusEffect().movementMultiplier();
        double vy = player.velocityY();

        boolean onGround = player.grounded() || player.y() + Player.RADIUS >= Game.GROUND_Y - 2.0;
        if (input.jump() && player.canJump() && onGround) {
            double jumpMultiplier = 1.0 + 0.10 * (player.stats().jump() - 1);
            vy = PLAYER_JUMP_SPEED * jumpMultiplier;
            player.setPosition(player.x(), Game.GROUND_Y - Player.RADIUS - 1.5);
            player.setGrounded(false);
        }
        player.setVelocity(vx, vy);
    }

    private void syncPlayerToBody(Player player, Body body) {
        body.getTransform().setTranslation(toMeters(player.x()), toMeters(player.y()));
        body.setLinearVelocity(toMeters(player.velocityX()), toMeters(player.velocityY()));
        body.setAtRest(false);
    }

    private void syncBodyToPlayer(Body body, Player player, boolean kickoffLocked) {
        Vector2 pos = body.getTransform().getTranslation();
        Vector2 vel = body.getLinearVelocity();

        double newX = kickoffLocked ? player.x() : clamp(toPixels(pos.x), Player.RADIUS, Game.WIDTH - Player.RADIUS);
        double newY = clamp(toPixels(pos.y), Player.RADIUS, Game.GROUND_Y - Player.RADIUS);

        double vx = kickoffLocked ? 0.0 : toPixels(vel.x);
        double vy = toPixels(vel.y);

        // Solo marcar grounded si cayó o está descansando en el suelo (vy >= 0)
        if (newY >= Game.GROUND_Y - Player.RADIUS - 0.5 && vy >= 0) {
            newY = Game.GROUND_Y - Player.RADIUS;
            vy = 0.0;
            player.setGrounded(true);
        } else {
            player.setGrounded(false);
        }

        player.setPosition(newX, newY);
        player.setVelocity(vx, vy);
    }

    private void syncBallToBody(Ball ball, Body body) {
        body.getTransform().setTranslation(toMeters(ball.x()), toMeters(ball.y()));
        body.setLinearVelocity(toMeters(ball.velocityX()), toMeters(ball.velocityY()));
        body.setAngularVelocity(0);
        body.setAtRest(false);
    }

    private void syncBodyToBall(Body body, Ball ball) {
        Vector2 pos = body.getTransform().getTranslation();
        Vector2 vel = body.getLinearVelocity();

        double x = clamp(toPixels(pos.x), Ball.RADIUS, Game.WIDTH - Ball.RADIUS);
        double y = toPixels(pos.y);
        double vx = toPixels(vel.x);
        double vy = toPixels(vel.y);

        if (y + Ball.RADIUS >= Game.GROUND_Y - 0.5) {
            y = Game.GROUND_Y - Ball.RADIUS;
            if (vy > 0) {
                double bounceMultiplier = ball.activePowerShot() == PowerShot.TORNADO ? 0.95 : 0.72;
                vy = -vy * bounceMultiplier;
            }
            // Si el rebote vertical ya es muy pequeño, detener el rebote en el suelo
            if (Math.abs(vy) < BALL_STOP_SPEED_Y) {
                vy = 0.0;
                // Fricción de rodadura sobre el césped para que el balón se detenga
                vx *= 0.94;
                if (Math.abs(vx) < BALL_STOP_SPEED_X) {
                    vx = 0.0;
                }
            }
        }
        ball.setPosition(x, y);
        ball.setVelocity(vx, vy);
    }

    private void scoreIfGoal(Game game) {
        Ball ball = game.ball();
        if (ball.y() >= Game.GOAL_TOP && ball.y() <= Game.GROUND_Y) {
            if (ball.x() - Ball.RADIUS <= Game.GOAL_DEPTH) {
                game.scoreGoal(2);
            } else if (ball.x() + Ball.RADIUS >= Game.WIDTH - Game.GOAL_DEPTH) {
                game.scoreGoal(1);
            }
        }
    }

    private void applyPowerShotImpact(Ball ball, Player first, Player second)
            throws DopoSoccerException {
        PowerShot shot = ball.activePowerShot();
        Player owner = ball.powerShotOwner();
        if (shot == null || owner == null || ball.powerEffectApplied()) {
            return;
        }

        Player opponent = owner == first ? second : first;
        double distance = Math.hypot(ball.x() - opponent.x(), ball.y() - opponent.y());
        if (distance > Ball.RADIUS + Player.RADIUS) {
            return;
        }

        ball.markPowerEffectApplied();
        opponent.takeDamage(shot.damage());
        if (opponent.stunned()) {
            return;
        }

        opponent.applyStatusEffect(shot.hitEffect(), shot.durationSeconds());
        if (shot == PowerShot.BLUE_AURA) {
            double goalDirection = opponent.x() < Game.WIDTH / 2 ? -1.0 : 1.0;
            opponent.setVelocity(goalDirection * 600.0, -180.0);
        } else if (shot == PowerShot.TORNADO) {
            double launchDirection = opponent.x() < Game.WIDTH / 2 ? -1.0 : 1.0;
            opponent.setVelocity(launchDirection * 120.0, -560.0);
        }
    }

    private static double toMeters(double pixels) {
        return pixels / PIXELS_PER_METER;
    }

    private static double toPixels(double meters) {
        return meters * PIXELS_PER_METER;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}