package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.game.Game;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.contact.ContactConstraint;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;
import presentation.controller.input.PlayerInput;

public final class PhysicsEngine {
    private static final double PIXELS_PER_METER = 100;
    private static final double GRAVITY = 13.5;
    private static final double STEP_SECONDS = 1.0 / 60.0;
    private static final double MAX_FRAME_SECONDS = 0.1;
    private static final int MAX_STEPS_PER_FRAME = 6;
    private static final double PLAYER_BODY_HEIGHT = 0.8;
    private static final double PLAYER_BODY_WIDTH = 0.48;
    private static final double PLAYER_MODEL_OFFSET = 10;
    private static final double BALL_KICK_RANGE = 0.75;
    private static final long PLAYER_CATEGORY = 1;
    private static final long BALL_CATEGORY = 1 << 1;
    private static final long FIELD_CATEGORY = 1 << 2;
    private static final long GOAL_SENSOR_CATEGORY = 1 << 3;
    private static final long ACTORS_MASK = PLAYER_CATEGORY | BALL_CATEGORY;

    private final World<Body> world = new World<>();
    private final Player firstPlayer;
    private final Player secondPlayer;
    private final Ball ball;
    private final Game match;
    private final Body firstPlayerBody;
    private final Body secondPlayerBody;
    private final Body ballBody;
    private final Body floorBody;
    private final Body leftGoalSensor;
    private final Body rightGoalSensor;

    private double accumulatedTime;
    private boolean firstJumpWasPressed;
    private boolean secondJumpWasPressed;
    private boolean firstJumpQueued;
    private boolean secondJumpQueued;
    private boolean firstPowerWasPressed;
    private boolean secondPowerWasPressed;
    private int pendingGoalScorer;

    public PhysicsEngine(Game match) {
        this.match = match;
        firstPlayer = match.firstPlayer();
        secondPlayer = match.secondPlayer();
        ball = match.ball();

        world.setGravity(new Vector2(0, GRAVITY));
        floorBody = createField();
        leftGoalSensor = createGoalSensor(true);
        rightGoalSensor = createGoalSensor(false);

        firstPlayerBody = createPlayerBody(firstPlayer);
        secondPlayerBody = createPlayerBody(secondPlayer);
        ballBody = createBallBody(ball);
        world.addBody(firstPlayerBody);
        world.addBody(secondPlayerBody);
        world.addBody(ballBody);

        world.addContactListener(new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collision,
                              org.dyn4j.dynamics.contact.Contact contact) {
                Body body1 = collision.getBody1();
                Body body2 = collision.getBody2();
                if (isPair(body1, body2, ballBody, leftGoalSensor)) {
                    pendingGoalScorer = 2;
                } else if (isPair(body1, body2, ballBody, rightGoalSensor)) {
                    pendingGoalScorer = 1;
                }
            }
        });
    }

    public double update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds,
                         boolean controlsEnabled) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0) {
            throw new IllegalArgumentException("El delta de física debe ser finito y no negativo.");
        }

        double elapsed = Math.min(deltaSeconds, MAX_FRAME_SECONDS);
        firstPlayer.tick(elapsed);
        secondPlayer.tick(elapsed);

        updateInputEdges(firstInput, secondInput, controlsEnabled);
        accumulatedTime += elapsed;

        int steps = 0;
        while (accumulatedTime + 1e-10 >= STEP_SECONDS && steps < MAX_STEPS_PER_FRAME) {
            applyPlayerControl(firstPlayer, firstPlayerBody, firstInput,
                    controlsEnabled, firstJumpQueued);
            applyPlayerControl(secondPlayer, secondPlayerBody, secondInput,
                    controlsEnabled, secondJumpQueued);
            firstJumpQueued = false;
            secondJumpQueued = false;

            world.update(STEP_SECONDS, STEP_SECONDS, 1);
            accumulatedTime -= STEP_SECONDS;
            steps++;
            processGoal();
        }

        if (steps == MAX_STEPS_PER_FRAME && accumulatedTime >= STEP_SECONDS) {
            accumulatedTime %= STEP_SECONDS;
        }

        syncPlayer(firstPlayer, firstPlayerBody);
        syncPlayer(secondPlayer, secondPlayerBody);
        syncBall();
        return elapsed;
    }

    public void kick(Player player, int attackDirection) {
        Body playerBody = player == firstPlayer ? firstPlayerBody
                : player == secondPlayer ? secondPlayerBody : null;
        if (playerBody == null) {
            throw new IllegalArgumentException("El jugador no pertenece a este partido.");
        }
        if (attackDirection != -1 && attackDirection != 1) {
            throw new IllegalArgumentException("La dirección del golpe debe ser -1 o 1.");
        }
        if (player.kickCooldown() > 0) {
            return;
        }

        Vector2 playerCenter = playerBody.getWorldCenter();
        Vector2 ballCenter = ballBody.getWorldCenter();
        if (Math.hypot(ballCenter.x - playerCenter.x, ballCenter.y - playerCenter.y)
                > BALL_KICK_RANGE) {
            return;
        }

        double powerMultiplier = player.power() == domain.model.entity.Power.AURA ? 1.5 : 1;
        double impulseStrength = (3.2 + player.stats().kick() * 0.25) * powerMultiplier;
        Vector2 impulse = new Vector2(
                Math.copySign(impulseStrength, attackDirection),
                -impulseStrength * 0.45);
        Vector2 impactPoint = new Vector2(ballCenter.x,
                ballCenter.y + Ball.RADIUS / PIXELS_PER_METER * 0.65);
        ballBody.applyImpulse(impulse, impactPoint);
        player.startKickCooldown(0.48);
        syncBall();
    }

    private void updateInputEdges(PlayerInput firstInput, PlayerInput secondInput,
                                  boolean controlsEnabled) {
        boolean firstJumpPressed = firstInput.jump() && !firstJumpWasPressed;
        boolean secondJumpPressed = secondInput.jump() && !secondJumpWasPressed;
        boolean firstPowerPressed = firstInput.power() && !firstPowerWasPressed;
        boolean secondPowerPressed = secondInput.power() && !secondPowerWasPressed;

        firstJumpWasPressed = firstInput.jump();
        secondJumpWasPressed = secondInput.jump();
        firstPowerWasPressed = firstInput.power();
        secondPowerWasPressed = secondInput.power();

        if (!controlsEnabled) {
            firstJumpQueued = false;
            secondJumpQueued = false;
            return;
        }

        firstJumpQueued |= firstJumpPressed;
        secondJumpQueued |= secondJumpPressed;
        if (firstPowerPressed) {
            firstPlayer.activatePower();
        }
        if (secondPowerPressed) {
            secondPlayer.activatePower();
        }
    }

    private void applyPlayerControl(Player player, Body body, PlayerInput input,
                                    boolean controlsEnabled, boolean jumpQueued) {
        if (!controlsEnabled) {
            return;
        }

        if (jumpQueued && isGrounded(body)) {
            double jumpSpeed = 4.1 + (player.stats().jump() - 5) * 0.28;
            body.applyImpulse(new Vector2(0, -body.getMass().getMass() * jumpSpeed));
        }

        double powerMultiplier = player.power() == domain.model.entity.Power.AURA ? 1.2 : 1;
        double maximumSpeed = (1.9 + (player.stats().speed() - 5) * 0.18)
                * powerMultiplier;
        double targetVelocity = input.horizontalDirection() * maximumSpeed;
        double acceleration = clamp((targetVelocity - body.getLinearVelocity().x) * 8,
                -24, 24);
        body.applyForce(new Vector2(body.getMass().getMass() * acceleration, 0));
    }

    private Body createPlayerBody(Player player) {
        Body body = createDynamicBody(
                Geometry.createCapsule(PLAYER_BODY_WIDTH, PLAYER_BODY_HEIGHT),
                player.x() / PIXELS_PER_METER,
                (player.y() - PLAYER_MODEL_OFFSET) / PIXELS_PER_METER,
                PLAYER_CATEGORY, ACTORS_MASK | FIELD_CATEGORY);
        body.getFixture(0).setDensity(55);
        body.getFixture(0).setFriction(0.8);
        body.getFixture(0).setRestitution(0.02);
        body.setMass(MassType.NORMAL);
        body.setLinearDamping(0.08);
        body.setAngularDamping(10);
        return body;
    }

    private Body createBallBody(Ball initialBall) {
        Body body = createDynamicBody(
                Geometry.createCircle(Ball.RADIUS / PIXELS_PER_METER),
                initialBall.x() / PIXELS_PER_METER,
                initialBall.y() / PIXELS_PER_METER,
                BALL_CATEGORY, ACTORS_MASK | FIELD_CATEGORY | GOAL_SENSOR_CATEGORY);
        body.getFixture(0).setDensity(12);
        body.getFixture(0).setFriction(0.35);
        body.getFixture(0).setRestitution(0.78);
        body.setMass(MassType.NORMAL);
        body.setLinearDamping(0.03);
        body.setAngularDamping(0.08);
        body.setBullet(true);
        return body;
    }

    private Body createDynamicBody(Convex shape, double x, double y,
                                   long category, long mask) {
        Body body = new Body();
        BodyFixture fixture = body.addFixture(shape);
        fixture.setFilter(new CategoryFilter(category, mask));
        body.setMass(MassType.NORMAL);
        body.translate(x, y);
        return body;
    }

    private Body createField() {
        double width = Game.WIDTH / PIXELS_PER_METER;
        double height = Game.HEIGHT / PIXELS_PER_METER;
        double goalDepth = Game.GOAL_DEPTH / PIXELS_PER_METER;
        double goalTop = Game.GOAL_TOP / PIXELS_PER_METER;
        double floorY = Game.GROUND_Y / PIXELS_PER_METER;

        Body ground = addStaticRectangle(width, 0.2,
                width / 2, floorY + 0.1, PLAYER_CATEGORY, 0.8, 0);
        addStaticRectangle(width + 2 * goalDepth, 0.2,
                width / 2, floorY + 0.1, BALL_CATEGORY, 0.35, 0.72);
        addStaticRectangle(width, 0.12, width / 2, 0.2,
                BALL_CATEGORY, 0.6, 0.55);

        addStaticRectangle(0.12, goalTop - 0.2, 0, (goalTop + 0.2) / 2,
                BALL_CATEGORY, 0.7, 0.72);
        addStaticRectangle(0.12, goalTop - 0.2, width, (goalTop + 0.2) / 2,
                BALL_CATEGORY, 0.7, 0.72);
        addStaticRectangle(0.12, height, 0, height / 2,
                PLAYER_CATEGORY, 0.8, 0.02);
        addStaticRectangle(0.12, height, width, height / 2,
                PLAYER_CATEGORY, 0.8, 0.02);

        addGoalFrame(true, width, goalDepth, goalTop, floorY);
        addGoalFrame(false, width, goalDepth, goalTop, floorY);
        return ground;
    }

    private void addGoalFrame(boolean leftGoal, double width, double depth,
                              double goalTop, double floorY) {
        double goalCenterX = leftGoal ? 0 : width;
        double inward = leftGoal ? 1 : -1;
        addStaticRectangle(depth, 0.1, goalCenterX + inward * depth / 2,
                goalTop, ACTORS_MASK, 0.7, 0.72);
        addStaticRectangle(0.1, floorY - goalTop, goalCenterX + inward * depth,
                (goalTop + floorY) / 2, BALL_CATEGORY, 0.45, 0.72);
    }

    private Body createGoalSensor(boolean leftGoal) {
        double width = Game.WIDTH / PIXELS_PER_METER;
        double depth = Game.GOAL_DEPTH / PIXELS_PER_METER;
        double goalTop = (Game.GOAL_TOP + Ball.RADIUS) / PIXELS_PER_METER;
        double goalBottom = (Game.GROUND_Y - Ball.RADIUS) / PIXELS_PER_METER;
        double sensorWidth = depth * 0.7;
        double ballRadius = Ball.RADIUS / PIXELS_PER_METER;
        double sensorX = leftGoal
                ? -sensorWidth / 2 - 2 * ballRadius
                : width + sensorWidth / 2 + 2 * ballRadius;
        double sensorY = (goalTop + goalBottom) / 2;

        Body sensor = new Body();
        BodyFixture fixture = sensor.addFixture(
                Geometry.createRectangle(sensorWidth, goalBottom - goalTop));
        fixture.setSensor(true);
        fixture.setFilter(new CategoryFilter(GOAL_SENSOR_CATEGORY, BALL_CATEGORY));
        sensor.setMass(MassType.INFINITE);
        sensor.translate(sensorX, sensorY);
        world.addBody(sensor);
        return sensor;
    }

    private Body addStaticRectangle(double width, double height, double centerX, double centerY,
                                    long mask, double friction, double restitution) {
        Body body = new Body();
        BodyFixture fixture = body.addFixture(Geometry.createRectangle(width, height));
        fixture.setFilter(new CategoryFilter(FIELD_CATEGORY, mask));
        fixture.setFriction(friction);
        fixture.setRestitution(restitution);
        body.setMass(MassType.INFINITE);
        body.translate(centerX, centerY);
        world.addBody(body);
        return body;
    }

    private void processGoal() {
        if (pendingGoalScorer == 0) {
            return;
        }

        int scorer = pendingGoalScorer;
        pendingGoalScorer = 0;
        accumulatedTime = 0;
        if (scorer == 1 || scorer == 2) {
            if (!match.kickoffDelayActive()) {
                match.scoreGoal(scorer);
                resetBodies();
            }
        }
    }

    private void resetBodies() {
        moveBodyTo(firstPlayerBody, firstPlayer.x(),
                firstPlayer.y() - PLAYER_MODEL_OFFSET);
        moveBodyTo(secondPlayerBody, secondPlayer.x(),
                secondPlayer.y() - PLAYER_MODEL_OFFSET);
        moveBodyTo(ballBody, ball.x(), ball.y());
        firstPlayer.setGrounded(false);
        secondPlayer.setGrounded(false);
    }

    private void moveBodyTo(Body body, double x, double y) {
        double targetX = x / PIXELS_PER_METER;
        double targetY = y / PIXELS_PER_METER;
        body.translate(targetX - body.getTransform().getTranslationX(),
                targetY - body.getTransform().getTranslationY());
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setAtRest(false);
    }

    private void syncPlayer(Player player, Body body) {
        double x = body.getTransform().getTranslationX() * PIXELS_PER_METER;
        double y = body.getTransform().getTranslationY() * PIXELS_PER_METER
                + PLAYER_MODEL_OFFSET;
        player.setPosition(x, y);
        player.setVelocity(body.getLinearVelocity().x * PIXELS_PER_METER,
                body.getLinearVelocity().y * PIXELS_PER_METER);
        player.setGrounded(isGrounded(body));
    }

    private boolean isGrounded(Body body) {
        for (ContactConstraint<Body> contact : world.getContacts(body)) {
            if (contact.getBody1() == body && contact.getBody2() == floorBody
                    || contact.getBody2() == body && contact.getBody1() == floorBody) {
                return true;
            }
        }
        return false;
    }

    private void syncBall() {
        double x = ballBody.getTransform().getTranslationX() * PIXELS_PER_METER;
        double y = ballBody.getTransform().getTranslationY() * PIXELS_PER_METER;
        ball.setPosition(x, y);
        ball.setVelocity(ballBody.getLinearVelocity().x * PIXELS_PER_METER,
                ballBody.getLinearVelocity().y * PIXELS_PER_METER);
    }

    private boolean isPair(Body first, Body second, Body expectedFirst, Body expectedSecond) {
        return first == expectedFirst && second == expectedSecond
                || first == expectedSecond && second == expectedFirst;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
