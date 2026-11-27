package domain.model.entity.service;

import domain.model.entity.PowerState;

/** Maneja la carga y las condiciones para activar el poder. */
public final class PlayerPowerService {
    private PlayerPowerService() { }

    /**
     * Incrementa la barra de poder proporcionalmente al tiempo transcurrido.
     *
     * @param current carga actual de la barra
     * @param seconds segundos transcurridos
     * @param maximum capacidad máxima de carga
     * @param rechargeSeconds duración total requerida para recargar de 0 al máximo
     * @return nueva carga acotada al máximo
     */
    public static double recharge(double current, double seconds, double maximum, double rechargeSeconds) {
        if (seconds <= 0) {
            return current;
        }
        double amount = seconds * maximum / rechargeSeconds;
        return Math.min(maximum, current + amount);
    }

    /**
     * Verifica si el jugador cumple las condiciones para activar su aura de poder.
     *
     * @param power estado actual del aura
     * @param charge porcentaje de carga actual
     * @param maximum carga requerida para activar
     * @param stunned {@code true} si el jugador está aturdido
     * @return {@code true} si puede activar el poder
     */
    public static boolean canActivate(PowerState power, double charge, double maximum, boolean stunned) {
        return power == PowerState.NONE && charge >= maximum && !stunned;
    }
}
