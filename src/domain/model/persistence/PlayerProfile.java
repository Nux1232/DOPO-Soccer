package domain.model.persistence;

import domain.model.exception.DopoSoccerException;

import java.io.Serial;
import java.io.Serializable;

/**
 * Datos permanentes de progresión del jugador. No almacena estados temporales del partido.
 *
 * @param speed nivel de velocidad (1 a 10)
 * @param kick nivel de fuerza/daño de patada (1 a 10)
 * @param jump nivel de altura de salto (1 a 10)
 * @param survival nivel de resistencia/vida máxima (1 a 10)
 * @param points puntos disponibles acumulados
 * @param nextUpgradeCost costo en puntos de la próxima mejora
 */
public record PlayerProfile(int speed, int kick, int jump, int survival, int points, int nextUpgradeCost)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Valida que todas las estadísticas y costos estén dentro de los rangos permitidos. */
    public PlayerProfile {
        if (speed < 1 || speed > 10 || kick < 1 || kick > 10
                || jump < 1 || jump > 10 || survival < 1 || survival > 10
                || points < 0 || nextUpgradeCost < 1) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_PROFILE);
        }
    }
}
