package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/**
 * Estado y capacidades disponibles durante la activación de un poder.
 *
 * @param owner jugador que activó la habilidad
 * @param opponent jugador rival, o {@code null} si no hay rival en la activación
 * @param direction dirección horizontal del balón o del jugador
 */
public record PowerActivationContext(
        Player owner,
        Player opponent,
        int direction) {

    public PowerActivationContext {
        if (owner == null) {
            throw new DopoSoccerException("El contexto de activación del poder está incompleto.");
        }
        direction = direction < 0 ? -1 : 1;
    }
}
