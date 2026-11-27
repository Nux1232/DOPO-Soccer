package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/**
 * Contrato general para una habilidad especial.
 * Las habilidades solo necesitan una identidad y su propia lógica de activación;
 * las propiedades de proyectil pertenecen a {@link ProjectilePower}.
 */
public interface Power {
    /** @return identificador estable del poder */
    String id();

    /**
     * Ejecuta la habilidad usando el contexto de la colisión o activación actual.
     *
     * @param context jugadores, balón y acciones disponibles para la habilidad
     * @throws DopoSoccerException si no se puede aplicar el efecto
     */
    void activate(PowerActivationContext context) throws DopoSoccerException;
}
