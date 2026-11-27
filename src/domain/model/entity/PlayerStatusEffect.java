package domain.model.entity;

/**
 * Efectos temporales de los tiros especiales, junto con las restricciones de control
 * y el multiplicador de movimiento que corresponde a cada efecto.
 */
public enum PlayerStatusEffect {
    /** Sin alteración de estado. */
    NONE(1.0, true, true, true),
    /** Empujado hacia su propio arco por Aura Azul. */
    PUSHED(0.0, false, false, false),
    /** Electrocutado por Rayo: no puede saltar y se mueve al 50 % de su velocidad. */
    ELECTRIFIED(0.50, true, false, false),
    /** Inconsciente por Tornado: lanzado al aire sin poder moverse, saltar ni patear. */
    UNCONSCIOUS(0.0, false, false, false),
    /**
     * Ilusión visual: presenta varios balones idénticos sin alterar los controles,
     * el movimiento ni las acciones disponibles del jugador.
     */
    CONFUSED(1.0, true, true, true);

    private final double movementMultiplier;
    private final boolean canMove;
    private final boolean canJump;
    private final boolean canKick;

    PlayerStatusEffect(
            double movementMultiplier,
            boolean canMove,
            boolean canJump,
            boolean canKick) {
        this.movementMultiplier = movementMultiplier;
        this.canMove = canMove;
        this.canJump = canJump;
        this.canKick = canKick;
    }

    /**
     * Factor multiplicador aplicado a la velocidad de desplazamiento horizontal.
     *
     * @return multiplicador entre {@code 0.0} y {@code 1.0}
     */
    public double movementMultiplier() {
        return movementMultiplier;
    }

    /**
     * Indica si el jugador puede desplazarse horizontalmente bajo este efecto.
     *
     * @return {@code true} si el movimiento está permitido
     */
    public boolean canMove() {
        return canMove;
    }

    /**
     * Indica si el jugador puede saltar bajo este efecto.
     *
     * @return {@code true} si el salto está permitido
     */
    public boolean canJump() {
        return canJump;
    }

    /**
     * Indica si el jugador puede patear bajo este efecto.
     *
     * @return {@code true} si la patada está permitida
     */
    public boolean canKick() {
        return canKick;
    }
}
