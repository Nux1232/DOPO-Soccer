package domain.model.game;

import domain.model.exception.DopoSoccerException;

import java.io.Serial;
import java.io.Serializable;

/**
 * Objeto de valor serializable que describe las reglas y el progreso de un partido.
 *
 * @param type modalidad de finalización (goles o tiempo)
 * @param goalLimit límite de goles configurado
 * @param durationSeconds duración reglamentaria total en segundos
 * @param remainingSeconds tiempo restante en segundos
 * @param suddenDeath indica si el partido se encuentra en muerte súbita
 */
public record MatchConfiguration(
        MatchType type,
        int goalLimit,
        int durationSeconds,
        double remainingSeconds,
        boolean suddenDeath) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Valida que los parámetros de configuración del partido sean consistentes. */
    public MatchConfiguration {
        if (type == null || goalLimit <= 0 || durationSeconds <= 0 || remainingSeconds < 0) {
            throw new DopoSoccerException("La configuración del partido es inválida.");
        }
    }
}
