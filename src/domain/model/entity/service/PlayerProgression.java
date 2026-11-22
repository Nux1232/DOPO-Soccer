package domain.model.entity.service;

/** Reglas puras de progresión de estadísticas. */
public final class PlayerProgression {
    private PlayerProgression() { }

    public static int nextLevel(int current, int max) {
        if (current >= max) throw new IllegalStateException("La estadística está al máximo.");
        return current + 1;
    }

    public static int nextCost(int currentCost) {
        return Math.multiplyExact(currentCost, 2);
    }
}
