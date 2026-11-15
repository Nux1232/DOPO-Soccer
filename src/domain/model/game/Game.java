package domain.model.game;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;
import domain.model.physics.PhysicsEngine;

/**
 * Representa un partido de DOPOSoccer y coordina su estado y física.
 */
public class Game {

    public static final double WIDTH = 960.0;
    public static final double HEIGHT = 540.0;
    public static final double GROUND_Y = 470.0;
    public static final double GOAL_DEPTH = 48.0;
    public static final double GOAL_TOP = 310.0;
    private static final double KICKOFF_DELAY_SECONDS = 1.0;

    private final PlayerMode playerMode;
    private final Player firstPlayer;
    private final Player secondPlayer;
    private final Ball ball;
    private final PhysicsEngine physicsEngine;
    private double kickoffDelay;

    public Game(PlayerMode playerMode, Country firstCountry, Country secondCountry) {
        if (playerMode == null || firstCountry == null || secondCountry == null) {
            throw new IllegalArgumentException("La modalidad y los personajes son obligatorios.");
        }
        this.playerMode = playerMode;
        this.firstPlayer = new Player(firstCountry, WIDTH / 2 - 60);
        this.secondPlayer = new Player(secondCountry, WIDTH / 2 + 60);
        this.ball = new Ball(WIDTH / 2, 160);
        this.physicsEngine = new PhysicsEngine();
        prepareKickoff();
    }

    public PlayerMode playerMode() {
        return playerMode;
    }

    public Player firstPlayer() {
        return firstPlayer;
    }

    public Player secondPlayer() {
        return secondPlayer;
    }

    public Ball ball() {
        return ball;
    }

    public boolean kickoffDelayActive() {
        return kickoffDelay > 0;
    }

    public void scoreGoal(int scoringPlayer) {
        if (scoringPlayer != 1 && scoringPlayer != 2) {
            return;
        }
        if (scoringPlayer == 1) {
            firstPlayer.scoreGoal();
        } else {
            secondPlayer.scoreGoal();
        }
        prepareKickoff();
    }

    public void updateKickoffDelay(double deltaSeconds) {
        if (deltaSeconds < 0) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        kickoffDelay = Math.max(0.0, kickoffDelay - deltaSeconds);
    }

    /**
     * Avanza el estado del partido un cuadro.
     *
     * @param firstInput   controles del jugador 1
     * @param secondInput  controles del jugador 2
     * @param deltaSeconds segundos transcurridos desde el cuadro anterior
     */
    public void update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) {
        physicsEngine.update(this, firstInput, secondInput, deltaSeconds);
        double delta = Math.min(deltaSeconds, 1.0 / 30.0);
        updateKickoffDelay(delta);
        if (!kickoffDelayActive()) {
            if (firstInput.kick()) {
                physicsEngine.kick(firstPlayer, ball, 1);
            }
            if (secondInput.kick()) {
                physicsEngine.kick(secondPlayer, ball, -1);
            }
        }
    }

    private void prepareKickoff() {
        firstPlayer.moveTo(WIDTH / 2 - 60, GROUND_Y - Player.RADIUS);
        secondPlayer.moveTo(WIDTH / 2 + 60, GROUND_Y - Player.RADIUS);
        ball.setPosition(WIDTH / 2, 160);
        ball.setVelocity(0, 0);
        kickoffDelay = KICKOFF_DELAY_SECONDS;
    }
}