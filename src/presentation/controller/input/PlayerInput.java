package presentation.controller.input;

/**
 * Controles de un jugador en un instante: qué acciones están activas en este
 * momento (mover a la izquierda, mover a la derecha, saltar, patear y
 * activar el poder).
 *
 * Es una clase pensada solo para guardar datos. Java
 * genera automáticamente el constructor, los métodos de lectura
 * left(), right(), jump(), kick() y
 * power() y equals, hashCode y toString.
 * Una vez creado, un PlayerInput no se puede modificar.
 *
 * La vista lee el teclado y crea un PlayerInput por jugador en
 * cada cuadro; el motor del juego y el motor de física lo usan sin saber
 * qué teclas concretas lo produjeron.
 *
 * @param left  si el jugador pide moverse a la izquierda
 * @param right  si el jugador pide moverse a la derecha
 * @param jump  si el jugador pide saltar
 * @param kick  si el jugador pide patear
 * @param power  si el jugador pide activar su poder
 */
public record PlayerInput(boolean left, boolean right, boolean jump, boolean kick, boolean power) {

    /** Controles sin ninguna acción activa (ninguna tecla presionada). */
    public static final PlayerInput NONE = new PlayerInput(false, false, false, false, false);

    /**
     * Calcula la dirección horizontal pedida por el jugador.
     *
     * Resta la izquierda de la derecha: si solo está activa la derecha
     * da 1; si solo está activa la izquierda da -1; si no hay ninguna o
     * están las dos a la vez, da 0.
     *
     * @return 1.0 (derecha), -1.0 (izquierda) o 0.0 (quieto)
     */
    public double horizontalDirection() {
        return (right ? 1 : 0) - (left ? 1 : 0);
    }
}