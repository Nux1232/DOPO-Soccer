package domain.model.game.lifecycle;

/** Regla pura para determinar si el límite de goles termina el encuentro. */
public final class GoalLimitRule {
    private GoalLimitRule() { }

    /**
     * Evalúa si alguno de los jugadores alcanzó el límite de goles.
     *
     * @param firstGoals goles del primer jugador
     * @param secondGoals goles del segundo jugador
     * @param limit cantidad de goles necesaria para ganar
     * @return {@code 1} si gana el jugador 1, {@code 2} si gana el jugador 2 o {@code -1} si el partido continúa
     */
    public static int winnerAtLimit(int firstGoals, int secondGoals, int limit) {
        if (firstGoals >= limit) return 1;
        if (secondGoals >= limit) return 2;
        return -1; // Partido aún activo
    }
}
