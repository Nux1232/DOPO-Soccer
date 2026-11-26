package domain.model.entity.service;

import domain.model.entity.PlayerStatusEffect;

/** Actualiza el tiempo restante de un efecto temporal. */
public final class PlayerStatusService {
    private PlayerStatusService() { }

    public record TickResult(PlayerStatusEffect effect, double remainingSeconds) { }

    public static TickResult tick(PlayerStatusEffect effect, double remaining, double seconds) {
        double timeLeft = remaining - Math.max(0.0, seconds);
        if (timeLeft <= 0.0) {
            return new TickResult(PlayerStatusEffect.NONE, 0.0);
        }
        return new TickResult(effect, timeLeft);
    }
}
