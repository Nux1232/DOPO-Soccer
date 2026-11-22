package domain.model.game.lifecycle;

/** Regla pura para determinar si el límite de goles termina el encuentro. */
public final class GoalLimitRule {
    private GoalLimitRule() { }

    public static int winnerAtLimit(int firstGoals, int secondGoals, int limit) {
        if (firstGoals >= limit) return 1;
        if (secondGoals >= limit) return 2;
        return -1; // Partido aún activo
    }
}
