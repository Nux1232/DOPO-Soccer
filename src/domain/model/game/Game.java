
package domain.model.game;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;
import domain.model.entity.PlayerSide;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsEngine;
import domain.model.physics.PhysicsSimulation;
import domain.model.game.lifecycle.MatchLifecycle;
import java.util.Map;

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
    private final MatchLifecycle lifecycle = new MatchLifecycle();
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
     * Constructor principal con inyección explícita del motor de simulación física.
     *
     * @param playerMode modalidad de participación (PvP o PvM)
     * @param firstCountry país del primer jugador (izquierdo)
     * @param secondCountry país del segundo jugador (derecho)
     * @param physicsSimulation implementación de la simulación física del partido
     * @throws DopoSoccerException si algún parámetro requerido es {@code null}
     */
    public Game(PlayerMode playerMode, Country firstCountry, Country secondCountry, PhysicsSimulation physicsSimulation) throws DopoSoccerException {
        if (playerMode == null || firstCountry == null || secondCountry == null || physicsSimulation == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        this.playerMode = playerMode;
        this.physicsSimulation = physicsSimulation;
        this.firstPlayer = new Player(firstCountry, WIDTH / 2 - 60, PlayerSide.LEFT);
        this.secondPlayer = new Player(secondCountry, WIDTH / 2 + 60, PlayerSide.RIGHT);

        this.ball = new Ball(WIDTH / 2, 160);
        prepareKickoff();
    }

    /** @return modalidad de participación del partido */
    public PlayerMode playerMode() {
        return playerMode;
    }

    /** @return jugador ubicado en el lado izquierdo */
    public Player firstPlayer() {
        return firstPlayer;
    }

    /** @return jugador ubicado en el lado derecho */
    public Player secondPlayer() {
        return secondPlayer;
    }

    /** @return balón del partido */
    public Ball ball() {
        return ball;
    }

    /** @return segundos restantes de la pausa previa al saque */
    public double kickoffDelaySeconds() { return kickoffDelay; }

    /**
     * Restaura el tiempo restante de pausa de saque al cargar una partida guardada.
     *
     * @param seconds segundos restantes de espera
     */
    public void restoreKickoffDelay(double seconds) { kickoffDelay = Math.max(0, seconds); }

    /**
     * Restaura el estado finalizado del ciclo de vida del partido.
     *
     * @param winner identificador del ganador ({@code 0} empate, {@code 1} o {@code 2})
     */
    public void restoreLifecycle(int winner) { if (winner >= 0 && winner <= 2) lifecycle.finish(winner); }

    /** @return {@code true} mientras el saque inicial o tras gol sigue bloqueado */
    public boolean kickoffDelayActive() {
        return kickoffDelay > 0;
    }

    /**
     * Registra un gol para el jugador indicado y prepara el siguiente saque.
     *
     * @param scoringPlayer número del jugador anotador ({@code 1} o {@code 2})
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

    /**
     * Descuenta el tiempo transcurrido al retraso activo de saque.
     *
     * @param deltaSeconds segundos transcurridos
     * @throws DopoSoccerException si el tiempo es negativo
     */
    public void updateKickoffDelay(double deltaSeconds) throws DopoSoccerException {

        if (deltaSeconds < 0) {
            throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME
            );
        }
        kickoffDelay = Math.max(0.0, kickoffDelay - deltaSeconds);
    }

    /**
     * Actualiza la simulación física, reduce el retraso de saque y procesa las patadas.
     *
     * @param firstInput acciones del primer jugador
     * @param secondInput acciones del segundo jugador
     * @param deltaSeconds segundos transcurridos desde el cuadro anterior
     * @throws DopoSoccerException si las entradas son {@code null} o el tiempo es negativo
     */
    public void update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) throws DopoSoccerException {
        if (firstInput == null || secondInput == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        if (deltaSeconds < 0) {
            throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME);
        }
        if (lifecycle.isFinished()) {
            return;
        }
        physicsSimulation.update(this, firstInput, secondInput, deltaSeconds);
        double delta = Math.min(deltaSeconds, 1.0 / 30.0);
        updateKickoffDelay(delta);
        if (!kickoffDelayActive()) {
            if (firstInput.kick()) {
                physicsSimulation.kick(
                        firstPlayer, secondPlayer, ball, firstPlayer.facingDirection());
            }
            if (secondInput.kick()) {
                physicsSimulation.kick(
                        secondPlayer, firstPlayer, ball, secondPlayer.facingDirection());
            }
        }
    }
    /**
     * Indica si el partido ya finalizó.
     *
     * @return {@code true} si el encuentro terminó
     */
    public boolean isFinished() {
        return lifecycle.isFinished();
    }

    /**
     * Devuelve el ganador del encuentro.
     *
     * @return {@code 0} para empate, {@code 1} para el primer jugador o {@code 2} para el segundo
     */
    public int winner() {
        return lifecycle.winner();
    }

    /**
     * Finaliza anticipadamente el partido determinando el resultado según el marcador actual.
     */
    public void endMatchNow() {
        if (lifecycle.isFinished()) {
            return;
        }
        int result = compareScores();
        finishMatch(result > 0 ? 1 : (result < 0 ? 2 : 0));
    }

    protected void finishMatch(int winner) {
        lifecycle.finish(winner);
    }

    protected int compareScores() {
        int firstGoals = firstPlayer.goals();
        int secondGoals = secondPlayer.goals();

        return Integer.compare(firstGoals, secondGoals);
    }

    /**
     * Prepara el saque inicial.
     */
    private void prepareKickoff() {
        firstPlayer.moveTo(WIDTH / 2 - 60, GROUND_Y - Player.RADIUS);
        secondPlayer.moveTo(WIDTH / 2 + 60, GROUND_Y - Player.RADIUS);
        ball.setPosition(WIDTH / 2, 160);
        ball.setVelocity(0, 0);
        ball.clearPowerShot();
        kickoffDelay = KICKOFF_DELAY_SECONDS;
    }

    /**
     * Devuelve la descripción textual de la modalidad o estado temporal del partido.
     *
     * @return descripción visible en el marcador
     */
    public String modeDescription() {
        return "PARTIDO LIBRE";
    }

    /**
     * Construye una instantánea de configuración para persistencia.
     *
     * @return configuración actual del partido
     */
    public MatchConfiguration matchConfiguration() {
        return new MatchConfiguration(
                MatchType.GOALS, Map.of("goalLimit", new MatchValue.IntegerNumber(10)));
    }

    /**
     * Restaura los parámetros específicos del modo tras cargar una partida.
     *
     * @param configuration configuración serializada del partido
     * @throws DopoSoccerException si la configuración no corresponde al tipo de partido
     */
    public void restoreMatchConfiguration(MatchConfiguration configuration) {
        if (configuration == null || !MatchType.GOALS.equals(configuration.type())) {
            throw new DopoSoccerException("La configuración guardada no corresponde a este partido.");
        }
    }
}
