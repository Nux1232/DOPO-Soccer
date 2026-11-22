package domain.model.physics;

/** Parámetros configurables del motor físico. Los valores estándar preservan el gameplay. */
public record PhysicsConfig(
        double pixelsPerMeter, double gravityY, double kickSpeedX, double kickSpeedY,
        double kickCooldownSeconds, double ballStopSpeedX, double ballStopSpeedY,
        double kickRangeBonus, double maxStepSeconds) {
    public PhysicsConfig {
        if (pixelsPerMeter <= 0 || gravityY < 0 || kickCooldownSeconds < 0
                || ballStopSpeedX < 0 || ballStopSpeedY < 0 || kickRangeBonus < 0
                || maxStepSeconds <= 0) {
            throw new IllegalArgumentException("Configuración física inválida");
        }
    }
    public static PhysicsConfig standard() {
        return new PhysicsConfig(100.0, 14.0, 560.0, -420.0, 0.42, 8.0, 22.0, 30.0, 1.0 / 30.0);
    }
}
