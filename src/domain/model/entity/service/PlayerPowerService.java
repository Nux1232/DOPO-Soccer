package domain.model.entity.service;

import domain.model.entity.PowerState;

/** Maneja la carga y las condiciones para activar el poder. */
public final class PlayerPowerService {
    private PlayerPowerService() { }

    public static double recharge(double current, double seconds, double maximum, double rechargeSeconds) {
        if (seconds <= 0) {
            return current;
        }
        double amount = seconds * maximum / rechargeSeconds;
        return Math.min(maximum, current + amount);
    }

    public static boolean canActivate(PowerState power, double charge, double maximum, boolean stunned) {
        return power == PowerState.NONE && charge >= maximum && !stunned;
    }
}
