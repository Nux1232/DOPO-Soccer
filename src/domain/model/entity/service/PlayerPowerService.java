package domain.model.entity.service;

import domain.model.entity.Power;

/** Reglas de carga, activación y consumo del aura del jugador. */
public final class PlayerPowerService {
    private PlayerPowerService() { }

    public static double recharge(double current, double seconds, double maximum, double rechargeSeconds) {
        if (seconds <= 0) return current;
        return Math.min(maximum, current + seconds * maximum / rechargeSeconds);
    }

    public static boolean canActivate(Power power, double charge, double maximum, boolean stunned) {
        return power == Power.NONE && charge >= maximum && !stunned;
    }
}
