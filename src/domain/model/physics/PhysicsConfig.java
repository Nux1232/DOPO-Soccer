package domain.model.physics;
import domain.model.exception.DopoSoccerException;

/**
 * Parámetros configurables del motor físico. Los valores estándar preservan la jugabilidad.
 *
 * @param pixelsPerMeter factor de conversión entre píxeles lógicos y metros físicos
 * @param gravityY aceleración vertical de gravedad en unidades físicas
 * @param kickSpeedX velocidad horizontal aplicada al balón al patear
 * @param kickSpeedY velocidad vertical aplicada al balón al patear
 * @param kickCooldownSeconds tiempo mínimo de espera entre patadas consecutivas
 * @param ballStopSpeedX umbral horizontal para detener el balón casi inmóvil
 * @param ballStopSpeedY umbral vertical para detener el rebote residual del balón
 * @param kickRangeBonus alcance adicional para patear jugadores y balón
 * @param maxStepSeconds paso máximo de integración permitido por actualización
 */
public record PhysicsConfig(
        double pixelsPerMeter, double gravityY, double kickSpeedX, double kickSpeedY,
        double kickCooldownSeconds, double ballStopSpeedX, double ballStopSpeedY,
        double kickRangeBonus, double maxStepSeconds) {
    /** Valida que las magnitudes físicas de configuración sean consistentes. */
    public PhysicsConfig {
        if (pixelsPerMeter <= 0 || gravityY < 0 || kickCooldownSeconds < 0
                || ballStopSpeedX < 0 || ballStopSpeedY < 0 || kickRangeBonus < 0
                || maxStepSeconds <= 0) {
            throw new DopoSoccerException("Configuración física inválida");
        }
    }

    /**
     * Devuelve la configuración física predeterminada del partido.
     *
     * @return instancia estándar de {@link PhysicsConfig}
     */
    public static PhysicsConfig standard() {
        return new PhysicsConfig(100.0, 14.0, 560.0, -420.0, 0.42, 8.0, 22.0, 30.0, 1.0 / 30.0);
    }
}
