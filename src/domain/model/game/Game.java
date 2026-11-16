
package domain.model.game;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsEngine;
import domain.model.physics.PhysicsSimulation;

/**
 * Representa un partido de DOPO-SOCCER.
 *
 * Coordina el estado del partido y delega la simulación
 * física mediante una abstracción.
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

    private final PhysicsSimulation physicsSimulation;

    private double kickoffDelay;

    /**
     * Constructor de compatibilidad.
     * Conserva el funcionamiento de las pruebas y código existente.
     *
     * Para nuevas implementaciones se recomienda inyectar
     * una PhysicsSimulation explícitamente.
     */
    public Game(PlayerMode playerMode, Country firstCountry, Country secondCountry) throws DopoSoccerException {
        this(playerMode, firstCountry, secondCountry, new PhysicsEngine());
    }

    /**
     * Constructor con inyección de dependencias.
     */
    public Game(PlayerMode playerMode, Country firstCountry, Country secondCountry, PhysicsSimulation physicsSimulation) throws DopoSoccerException {
        if (playerMode == null || firstCountry == null || secondCountry == null || physicsSimulation == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        this.playerMode = playerMode;
        this.physicsSimulation = physicsSimulation;
        this.firstPlayer = new Player(firstCountry, WIDTH / 2 - 60);
        this.secondPlayer = new Player(secondCountry, WIDTH / 2 + 60);
        this.ball = new Ball(WIDTH / 2, 160);
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

    /**
     * Registra un gol y prepara el siguiente saque.
     */
    public void scoreGoal(int scoringPlayer) {
        if (scoringPlayer != 1 && scoringPlayer != 2) {
            return;
        }
        if (scoringPlayer == 1) {firstPlayer.scoreGoal();secondPlayer.onGoalConceded();
        } else {
            secondPlayer.scoreGoal();firstPlayer.onGoalConceded();
        }
        prepareKickoff();
    }

    public void updateKickoffDelay(double deltaSeconds) throws DopoSoccerException {

        if (deltaSeconds < 0) {
            throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME
            );
        }
        kickoffDelay = Math.max(0.0, kickoffDelay - deltaSeconds);
    }

    /**
     * Actualiza la simulación y procesa las patadas.
     */
    public void update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) throws DopoSoccerException {
        if (firstInput == null || secondInput == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        physicsSimulation.update(this, firstInput, secondInput, deltaSeconds);
        double delta = Math.min(deltaSeconds, 1.0 / 30.0);
        updateKickoffDelay(delta);
        if (!kickoffDelayActive()) {
            if (firstInput.kick()) {
                physicsSimulation.kick(firstPlayer, secondPlayer, ball, 1);
            }
            if (secondInput.kick()) {
                physicsSimulation.kick(secondPlayer, firstPlayer, ball, -1
                );
            }
        }
    }
    /**
     * Prepara el saque inicial.
     */
    private void prepareKickoff() {
        firstPlayer.moveTo(WIDTH / 2 - 60, GROUND_Y - Player.RADIUS);
        secondPlayer.moveTo(WIDTH / 2 + 60, GROUND_Y - Player.RADIUS);
        ball.setPosition(WIDTH / 2, 160);
        ball.setVelocity(0, 0);
        kickoffDelay = KICKOFF_DELAY_SECONDS;
    }
}
