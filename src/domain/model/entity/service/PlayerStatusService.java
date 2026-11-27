package domain.model.entity.service;

import domain.model.entity.PlayerStatusEffect;

/** Actualiza el tiempo restante de un efecto temporal aplicado a un jugador. */
public final class PlayerStatusService {
    private PlayerStatusService() { }

    /**
     * Resultado inmutable tras descontar tiempo de un efecto de estado.
     *
     * @param effect efecto resultante (o {@link PlayerStatusEffect#NONE} si expiró)
     * @param remainingSeconds segundos restantes del efecto
     */
    public record TickResult(PlayerStatusEffect effect, double remainingSeconds) { }

    /**
     * Descuenta el tiempo transcurrido al efecto activo y determina si expiró.
     *
     * @param effect efecto de estado actual
     * @param remaining segundos restantes antes del paso de tiempo
     * @param seconds segundos transcurridos
     * @return nuevo estado y tiempo restante
     */
    public static TickResult tick(PlayerStatusEffect effect, double remaining, double seconds) {
        double timeLeft = remaining - Math.max(0.0, seconds);
        if (timeLeft <= 0.0) {
            return new TickResult(PlayerStatusEffect.NONE, 0.0);
        }
        return new TickResult(effect, timeLeft);
    }
}
