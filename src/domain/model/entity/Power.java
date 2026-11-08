package domain.model.entity;

/**
 * Estado del poder de un jugador.
 *
 * Indica únicamente si el jugador tiene un poder activo en este momento.
 * No distingue qué tipo de tiro de poder es (por ejemplo, el de cada país).
 */
public enum Power {

    /** El jugador no tiene ningún poder activo. */
    NONE,

    /** El jugador tiene el aura del poder activada. */
    AURA
}