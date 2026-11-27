package domain.model.entity;

/**
 * Efectos temporales aplicados a un jugador por los tiros especiales.
 */
public enum PlayerStatusEffect {
    NONE(1.0, true, true, true),
    PUSHED(0.0, false, false, false),
    ELECTRIFIED(0.50, true, false, false),
    UNCONSCIOUS(0.0, false, false, false),
    CONFUSED(0.65, true, true, false);

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

    public double movementMultiplier() {
        return movementMultiplier;
    }

    public boolean canMove() {
        return canMove;
    }

    public boolean canJump() {
        return canJump;
    }

    public boolean canKick() {
        return canKick;
    }
}
