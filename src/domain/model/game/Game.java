package domain.model.game;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;

/**
 * Representa un partido en curso: los dos jugadores, el balón y las
 * medidas de la cancha.
 *
 * Esta clase crea los elementos del partido, registra los goles y
 * prepara el saque inicial después de cada gol. No dibuja nada ni calcula
 * colisiones: eso lo hacen otras clases.
 */
public final class Game {

    /** Ancho de la cancha. */
    public static final double WIDTH = 960;

    /** Alto de la cancha. */
    public static final double HEIGHT = 540;

    /** Coordenada vertical (y) del suelo de la cancha. */
    public static final double GROUND_Y = 480;

    /** Coordenada vertical (y) del borde superior de los arcos. */
    public static final double GOAL_TOP = 365;

    /** Profundidad horizontal de cada arco, medida desde el borde de la cancha. */
    public static final double GOAL_DEPTH = 90;

    /** Modalidad de jugadores del partido. */
    private final PlayerMode playerMode;

    /** Primer jugador, que empieza a la izquierda del centro. */
    private final Player firstPlayer;

    /** Segundo jugador, que empieza a la derecha del centro. */
    private final Player secondPlayer;

    /** Balón del partido. */
    private final Ball ball;

    /** Segundos que faltan antes de poder jugar después del saque. */
    private double kickoffDelay = 1.1;

    /**
     * Crea un partido con la modalidad y los personajes elegidos.
     * Coloca a los jugadores cerca del centro de la cancha y al balón
     * en el centro.
     *
     * @param playerMode modalidad de jugadores.
     * @param firstCountry personaje del primer jugador.
     * @param secondCountry personaje del segundo jugador.
     * @throws IllegalArgumentException si alguno de los argumentos es null.
     */
    public Game(PlayerMode playerMode, Country firstCountry, Country secondCountry) {
        if (playerMode == null || firstCountry == null || secondCountry == null) {
            throw new IllegalArgumentException("La modalidad y los países son obligatorios.");
        }
        this.playerMode = playerMode;
        firstPlayer = new Player(firstCountry, WIDTH / 2 - 60);
        secondPlayer = new Player(secondCountry, WIDTH / 2 + 60);
        ball = new Ball(WIDTH / 2, 40);
    }

    /**
     * Devuelve la modalidad de jugadores del partido.
     *
     * @return la modalidad
     */
    public PlayerMode playerMode() {
        return playerMode;
    }

    /**
     * Devuelve el primer jugador.
     *
     * @return el primer jugador
     */
    public Player firstPlayer() {
        return firstPlayer;
    }

    /**
     * Devuelve el segundo jugador.
     *
     * @return el segundo jugador
     */
    public Player secondPlayer() {
        return secondPlayer;
    }

    /**
     * Devuelve el balón del partido.
     *
     * @return el balón
     */
    public Ball ball() {
        return ball;
    }

    /**
     * Indica si todavía hay que esperar antes de poder jugar.
     *
     * @return true si la espera del saque no ha terminado
     */
    public boolean kickoffDelayActive() {
        return kickoffDelay > 0;
    }

    /**
     * Registra un gol del jugador indicado y prepara el saque siguiente.
     * Si el número no es 1 ni 2, no hace nada.
     *
     * @param playerNumber número del jugador que anotó (1 o 2)
     */
    public void scoreGoal(int playerNumber) {
        if (playerNumber != 1 && playerNumber != 2) {
            return;
        }
        Player scorer = playerNumber == 1 ? firstPlayer : secondPlayer;
        scorer.scoreGoal();
        prepareKickoff();
    }

    /**
     * Descuenta tiempo a la espera del saque. El tiempo restante nunca
     * baja de cero.
     *
     * @param seconds segundos transcurridos; no puede ser negativo
     * @throws IllegalArgumentException si el tiempo es negativo
     */
    public void updateKickoffDelay(double seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        kickoffDelay = Math.max(0, kickoffDelay - seconds);
    }

    /**
     * Prepara el saque: coloca a los jugadores y al balón cerca del
     * centro, deja el balón sin velocidad y reinicia la espera.
     */
    private void prepareKickoff() {
        firstPlayer.moveTo(WIDTH / 2 - 60, 38);
        secondPlayer.moveTo(WIDTH / 2 + 60, 38);
        ball.setPosition(WIDTH / 2, 25);
        ball.setVelocity(0, 0);
        kickoffDelay = 1.1;
    }
}