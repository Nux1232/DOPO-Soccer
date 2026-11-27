package domain.model.entity.service;

import domain.model.exception.DopoSoccerException;

/** Reglas sencillas para subir estadísticas y calcular el costo de mejora. */
public final class PlayerProgression {
    private PlayerProgression() { }

    /**
     * Calcula el siguiente nivel de una estadística validando que no supere el tope.
     *
     * @param current nivel actual
     * @param maximum nivel máximo permitido
     * @return nivel incrementado en una unidad
     * @throws DopoSoccerException si la estadística ya alcanzó el máximo
     */
    public static int nextLevel(int current, int maximum) {
        if (current >= maximum) {
            throw new DopoSoccerException(DopoSoccerException.STAT_AT_MAX);
        }
        return current + 1;
    }

    /**
     * Calcula el costo de la siguiente mejora duplicando el costo actual.
     *
     * @param currentCost costo de la mejora recién realizada
     * @return costo de la próxima mejora
     */
    public static int nextCost(int currentCost) {
        return currentCost * 2;
    }
}
