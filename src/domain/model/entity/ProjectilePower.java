package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Capacidad adicional de un poder que se ejecuta como un proyectil asociado al balón. */
public interface ProjectilePower extends Power {
    /** @return nombre del recurso gráfico del proyectil */
    String visualAsset();

    /** @return color hexadecimal de respaldo para el renderizado */
    String visualColor();

    /** @return duración del proyectil y su efecto, en segundos */
    double durationSeconds();

    /** @return daño base antes de aplicar multiplicadores del jugador */
    double damage();

    /** @return velocidad horizontal inicial, en píxeles por segundo */
    double horizontalSpeed();

    /** @return velocidad vertical inicial, en píxeles por segundo */
    double verticalSpeed();

    /** @return efecto temporal aplicado al rival al impactar */
    PlayerStatusEffect hitEffect();

    /** @return coeficiente de restitución del balón durante el proyectil */
    double restitution();

    /** @return {@code true} si la presentación debe mostrar copias ilusorias del balón */
    default boolean renderCopies() { return false; }

    /**
     * Aplica el efecto del proyectil al jugador impactado.
     *
     * @param owner jugador que lanzó el proyectil
     * @param opponent jugador rival impactado
     * @throws DopoSoccerException si no se puede aplicar el impacto
     */
    void applyImpact(Player owner, Player opponent) throws DopoSoccerException;

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
    default void activate(PowerActivationContext context) throws DopoSoccerException {
        throw new DopoSoccerException(
                "Un poder de proyectil debe activarse con el contexto físico de la partida.");
    }

    /** Adaptador que permite lanzar el proyectil sin acoplar la habilidad a Dyn4j. */
    @FunctionalInterface
    interface ProjectileLauncher {
        /** Lanza el proyectil en la simulación actual. */
        void launch(ProjectilePower power) throws DopoSoccerException;
    }
}
