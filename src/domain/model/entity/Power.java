package domain.model.entity;

import domain.model.exception.DopoSoccerException;
import java.util.Map;

/**
 * Contrato general para una habilidad especial.
 * Las habilidades solo necesitan una identidad y su propia lógica de activación;
 * las propiedades de proyectil pertenecen a {@link ProjectilePower}.
 */
public interface Power {
    /** @return identificador estable del poder */
    String id();

    /**
     * Ejecuta la habilidad usando el contexto de la colisión o activación actual.
     *
     * @param context jugador, rival opcional y dirección de activación
     * @throws DopoSoccerException si no se puede aplicar el efecto
     */
    void activate(PowerActivationContext context) throws DopoSoccerException;

    /**
     * Activa el poder en el contexto de una partida con acceso opcional al balón.
     * Los poderes normales usan {@link #activate(PowerActivationContext)}; los poderes
     * que crean tiros pueden sobrescribir este método y ejecutar el lanzador cuando corresponda.
     */
    default void activateInMatch(
            PowerActivationContext context, Ball ball, PowerShotLauncher launcher)
            throws DopoSoccerException {
        activate(context);
    }

    /**
     * Captura estado adicional del poder asociado al jugador para guardados.
     *
     * @param player jugador propietario
     * @return estado serializable como pares de texto
     */
    default Map<String, String> saveState(Player player) {
        return player.powerExtensionState();
    }

    /**
     * Restaura el estado adicional capturado por {@link #saveState(Player)}.
     *
     * @param player jugador cuyo estado se restaura
     * @param state valores guardados
     */
    default void restoreState(Player player, Map<String, String> state) {
        player.restorePowerExtensionState(state);
    }

    /**
     * Restaura un tiro propio del poder en el balón, si este tipo de poder admite tiros activos.
     *
     * @return {@code true} cuando el estado correspondía a este poder y fue restaurado
     */
    default boolean restoreActiveShot(
            Ball ball, Player owner, String powerId, double remainingSeconds, boolean effectApplied) {
        return false;
    }

    /** Acción física que el poder puede invocar para iniciar su tiro. */
    @FunctionalInterface
    interface PowerShotLauncher {
        /** Ejecuta el lanzamiento con la física configurada por el motor actual. */
        void launch(BallShot shot) throws DopoSoccerException;
    }
}
