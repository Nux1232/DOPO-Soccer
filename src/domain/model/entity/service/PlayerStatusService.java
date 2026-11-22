package domain.model.entity.service;

import domain.model.entity.PlayerStatusEffect;

/** Operaciones de ciclo de vida para efectos temporales de estado. */
public final class PlayerStatusService {
    private PlayerStatusService() { }

    public record TickResult(PlayerStatusEffect effect, double remainingSeconds) { }

    public static TickResult tick(PlayerStatusEffect effect, double remaining, double seconds) {
        double next = Math.max(0.0, remaining - Math.max(0.0, seconds));
        return next == 0.0
                ? new TickResult(PlayerStatusEffect.NONE, 0.0)
                : new TickResult(effect, next);
    }
}
