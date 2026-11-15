package domain.model.game;

/**
 * Controles de un jugador en un instante: qué acciones están activas en este
 * momento (mover a la izquierda, mover a la derecha, saltar, patear y
 * activar el poder).
 *
 * @param left  true si el jugador pide moverse a la izquierda
 * @param right true si el jugador pide moverse a la derecha
 * @param jump  true si el jugador pide saltar
 * @param kick  true si el jugador pide patear
 * @param power true si el jugador pide activar su poder
 */
public record PlayerInput(boolean left, boolean right, boolean jump, boolean kick, boolean power) {

    /** Controles sin ninguna acción activa (ninguna tecla presionada). */
    public static final PlayerInput NONE = new PlayerInput(false, false, false, false, false);

    /**
     * Calcula la dirección horizontal pedida por el jugador.
     *
     * @return 1.0 (derecha), -1.0 (izquierda) o 0.0 (quieto)
     */
    public double horizontalDirection() {
        return (right ? 1 : 0) - (left ? 1 : 0);
    }
}