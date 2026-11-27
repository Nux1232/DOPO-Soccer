package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.Power;
import domain.model.entity.PowerState;
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

    private final PhysicsConfig config;
    private final double kickRange;

    /** Mundo físico de Dyn4j y cuerpos dinámicos. */
    private final World<Body> world;
    private final Body ballBody;
    private final Body firstPlayerBody;
    private final Body secondPlayerBody;
    private final PlayerInputHandler inputHandler;
    private final GoalDetector goalDetector;
    private final PowerShotImpactHandler powerShotImpactHandler;

    /**
     * Crea un motor físico con la configuración y reglas estándar del partido.
     */
    public PhysicsEngine() {
        this(PhysicsConfig.standard(), new PlayerInputHandler(), GoalDetector.standardField(),
                new PowerShotImpactHandler());
    }

    /**
     * Crea un motor físico con parámetros de simulación personalizados.
     *
     * @param config configuración física del campo y de la patada
     */
    public PhysicsEngine(PhysicsConfig config) {
        this(config, new PlayerInputHandler(), GoalDetector.standardField(),
                new PowerShotImpactHandler());
    }

    /**
     * Crea un motor cuyas reglas de control, gol e impacto pueden sustituirse
     * sin alterar la integración física de Dyn4j.
     *
     * @param inputHandler manejador de entradas del jugador
     * @param goalDetector regla de detección de goles
     * @param powerShotImpactHandler manejador de impactos de tiros especiales
     */
    public PhysicsEngine(
            PlayerInputHandler inputHandler,
            GoalDetector goalDetector,
            PowerShotImpactHandler powerShotImpactHandler) {
        this(PhysicsConfig.standard(), inputHandler, goalDetector, powerShotImpactHandler);
    }

    /**
     * Crea un motor físico completo con configuración y colaboradores explícitos.
     *
     * @param config configuración física
     * @param inputHandler manejador de entradas
     * @param goalDetector detector de goles
     * @param powerShotImpactHandler manejador de impactos especiales
     * @throws DopoSoccerException si alguna dependencia es nula
     */
    public PhysicsEngine(PhysicsConfig config, PlayerInputHandler inputHandler,
            GoalDetector goalDetector, PowerShotImpactHandler powerShotImpactHandler) {
        if (config == null || inputHandler == null || goalDetector == null || powerShotImpactHandler == null) {
            throw new DopoSoccerException("Las dependencias del motor no pueden ser null");
        }
        this.config = config;
        this.kickRange = Player.RADIUS + Ball.RADIUS + config.kickRangeBonus();
        this.inputHandler = inputHandler;
        this.goalDetector = goalDetector;
        this.powerShotImpactHandler = powerShotImpactHandler;
        this.world = new World<>();
        this.world.setGravity(new Vector2(0, config.gravityY()));

        createStaticBoundaries();
        this.ballBody = createBallBody();
        this.firstPlayerBody = createPlayerBody();
        this.secondPlayerBody = createPlayerBody();

        this.world.addBody(ballBody);
        this.world.addBody(firstPlayerBody);
        this.world.addBody(secondPlayerBody);
    }

    /**
     * Avanza la simulación física un cuadro usando Dyn4j, procesa el disparo de poder
     * al tocar el balón, verifica impactos especiales y detecta goles.
     *
     * @param game partido activo
     * @param firstInput controles del primer jugador
     * @param secondInput controles del segundo jugador
     * @param deltaSeconds tiempo transcurrido en segundos
     * @throws DopoSoccerException si ocurre un error de validación en el dominio
     */
    @Override
    public void update(
            Game game,
            PlayerInput firstInput,
            PlayerInput secondInput,
            double deltaSeconds) throws DopoSoccerException {
        double dt = Math.min(deltaSeconds, config.maxStepSeconds());
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
                ball.activePowerShot() == null ? 0.72 : ball.activePowerShot().restitution());

        // 1. Aplicar entradas de control a los jugadores
        inputHandler.apply(first, firstInput, kickoffLocked);
        inputHandler.apply(second, secondInput, kickoffLocked);

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

        if (!kickoffLocked) {
            triggerPowerShotOnTouch(first, ball);
            triggerPowerShotOnTouch(second, ball);
        }
        powerShotImpactHandler.apply(ball, first, second);

        // 5. Verificar si se anotó un gol
        goalDetector.scoringPlayer(ball).ifPresent(game::scoreGoal);
    }

    private void triggerPowerShotOnTouch(Player player, Ball ball) {
        if (player.power() != PowerState.AURA || player.stunned()) {
            return;
        }
        double dist = Math.hypot(ball.x() - player.x(), ball.y() - player.y());
        if (dist > Player.RADIUS + Ball.RADIUS + 2.0) {
            return;
        }
        int dir = Math.abs(ball.x() - player.x()) > 1e-6
                ? (ball.x() > player.x() ? 1 : -1)
                : player.facingDirection();
        launchPowerShot(player, ball, dir);
    }

    private void launchPowerShot(Player player, Ball ball, int dir) {
        Power powerShot = player.country().power();
        if (powerShot != null && player.consumePower()) {
            player.setFacingDirection(dir);
            ball.startPowerShot(powerShot, player);
            ball.setVelocity(dir * powerShot.horizontalSpeed(), powerShot.verticalSpeed());
            ballBody.getFixture(0).setRestitution(powerShot.restitution());
            syncBallToBody(ball, ballBody);
        }
    }

    /**
     * Sobrecarga compatible con las pruebas unitarias para patear únicamente el balón.
     *
     * @param player jugador que patea
     * @param ball balón del partido
     * @param direction dirección horizontal de la patada
     * @throws DopoSoccerException si ocurre un error de validación
     */
    public void kick(Player player, Ball ball, int direction) throws DopoSoccerException {
        kick(player, null, ball, direction);
    }

    /**
     * Ejecuta la patada de un jugador.
     * Si el jugador está aturdido o en espera (cooldown), no hace nada.
     * Si el balón está al alcance, impulsa únicamente el balón (Tabla 4, Anexo 2).
     * Si no toca el balón pero el rival está al alcance del pie, le aplica
     * {@code 10 * multiplicadorKick} de daño.
     *
     * @param kicker jugador que ejecuta la patada
     * @param opponent jugador contrario (puede ser {@code null} en pruebas aisladas)
     * @param ball balón del partido
     * @param direction dirección horizontal de la patada
     * @throws DopoSoccerException si ocurre un error de validación
     */
    @Override
    public void kick(Player kicker, Player opponent, Ball ball, int direction) throws DopoSoccerException {
        if (kicker.kickCooldown() > 0 || !kicker.canKick()) {
            return;
        }
        int dir = direction >= 0 ? 1 : -1;
        kicker.setFacingDirection(dir);
        double dxBall = ball.x() - kicker.x();
        double dyBall = ball.y() - kicker.y();
        double distBall = Math.hypot(dxBall, dyBall);

        if (distBall <= kickRange) {
            if (kicker.power() == PowerState.AURA) {
                launchPowerShot(kicker, ball, dir);
            } else {
                ball.clearPowerShot();
                ball.setVelocity(dir * config.kickSpeedX(), config.kickSpeedY());
                ballBody.getFixture(0).setRestitution(0.72);
                syncBallToBody(ball, ballBody);
            }
            kicker.startKickCooldown(config.kickCooldownSeconds());
            kicker.startKickAnimation();
            return;
        }

        if (opponent != null) {
            double dxOpp = opponent.x() - kicker.x();
            double dyOpp = opponent.y() - kicker.y();
            double distOpp = Math.hypot(dxOpp, dyOpp);
            boolean facingOpponent = (dir > 0 && dxOpp >= -Player.RADIUS) || (dir < 0 && dxOpp <= Player.RADIUS);
            if (distOpp <= kickRange + Player.RADIUS * 0.5 && facingOpponent) {
                opponent.takeDamage(kicker.kickDamage());
                kicker.startKickCooldown(config.kickCooldownSeconds());
                kicker.startKickAnimation();
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
                double bounceMultiplier = ball.activePowerShot() == null ? 0.72 : ball.activePowerShot().restitution();
                vy = -vy * bounceMultiplier;
            }
            // Si el rebote vertical ya es muy pequeño, detener el rebote en el suelo
            if (Math.abs(vy) < config.ballStopSpeedY()) {
                vy = 0.0;
                // Fricción de rodadura sobre el césped para que el balón se detenga
                vx *= 0.94;
                if (Math.abs(vx) < config.ballStopSpeedX()) {
                    vx = 0.0;
                }
            }
        }
        ball.setPosition(x, y);
        ball.setVelocity(vx, vy);
    }

    private double toMeters(double pixels) {
        return pixels / config.pixelsPerMeter();
    }

    private double toPixels(double meters) {
        return meters * config.pixelsPerMeter();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
