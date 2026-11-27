package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Comportamiento de un tiro especial mientras está asociado al balón. */
public interface BallShot {
    /** @return identificador estable del tiro */
    String id();

    /** @return duración del tiro y su efecto en segundos */
    double durationSeconds();

    /** @return restitución física del balón mientras el tiro está activo */
    double restitution();

    /** @return velocidad horizontal inicial en píxeles por segundo */
    double horizontalSpeed();

    /** @return velocidad vertical inicial en píxeles por segundo */
    double verticalSpeed();

    /** @return recurso visual opcional del tiro */
    String visualAsset();

    /** @return color usado como respaldo visual */
    String visualColor();

    /** @return si la vista representa el balón con copias ilusorias */
    boolean renderCopies();

    /** Aplica el efecto de impacto del tiro a su rival. */
    void applyImpact(Player owner, Player opponent) throws DopoSoccerException;
}
