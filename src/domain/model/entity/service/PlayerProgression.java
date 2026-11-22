package domain.model.entity.service;

import domain.model.exception.DopoSoccerException;

/** Reglas sencillas para subir estadísticas. */
public final class PlayerProgression {
    private PlayerProgression() { }

    public static int nextLevel(int current, int maximum) {
        if (current >= maximum) {
            throw new DopoSoccerException(DopoSoccerException.STAT_AT_MAX);
        }
        return current + 1;
    }

    public static int nextCost(int currentCost) {
        return currentCost * 2;
    }
}
