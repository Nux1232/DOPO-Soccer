package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Capacidad adicional de un poder que se ejecuta como un proyectil asociado al balón. */
public interface ProjectilePower extends Power, BallShot {

    /** @return daño base antes de aplicar multiplicadores del jugador */
    double damage();

    /** @return velocidad horizontal inicial, en píxeles por segundo */
    double horizontalSpeed();

    /** @return velocidad vertical inicial, en píxeles por segundo */
    double verticalSpeed();

    /** @return efecto temporal aplicado al rival al impactar */
    PlayerStatusEffect hitEffect();

    /**
     * Ejecuta el comportamiento de proyectil usando las capacidades físicas disponibles.
     *
     * @param context información general del jugador que activa el poder
     * @param ball balón asociado al proyectil
     * @param launcher adaptador del motor para aplicar el lanzamiento
     */
    default void activateProjectile(
            PowerActivationContext context,
            Ball ball,
            ProjectileLauncher launcher) throws DopoSoccerException {
        if (context == null || ball == null || launcher == null) {
            throw new DopoSoccerException("El contexto físico del proyectil está incompleto.");
        }
        launcher.launch(this);
    }

    @Override
    default void activateInMatch(
            PowerActivationContext context, Ball ball, PowerShotLauncher launcher)
            throws DopoSoccerException {
        if (launcher == null) {
            throw new DopoSoccerException("El lanzador del poder es obligatorio.");
        }
        activateProjectile(context, ball, launcher::launch);
    }

    @Override
    default void activate(PowerActivationContext context) throws DopoSoccerException {
        throw new DopoSoccerException(
                "Un poder de proyectil debe activarse con el contexto físico de la partida.");
    }

    @Override
    default boolean restoreActiveShot(
            Ball ball, Player owner, String powerId, double remainingSeconds, boolean effectApplied) {
        if (ball == null || owner == null || !id().equals(powerId)) return false;
        ball.restorePowerShot(this, owner, remainingSeconds, effectApplied);
        return true;
    }

    /** Adaptador que permite lanzar el proyectil sin acoplar la habilidad a Dyn4j. */
    @FunctionalInterface
    interface ProjectileLauncher {
        /** Lanza el proyectil en la simulación actual. */
        void launch(ProjectilePower power) throws DopoSoccerException;
    }
}
