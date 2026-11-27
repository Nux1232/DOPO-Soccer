package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Contrato del poder especial que implementa cada selección o personaje. */
public interface Power {
    /** @return identificador único del poder */
    String id();

    /** @return nombre del recurso gráfico asociado al proyectil */
    String visualAsset();

    /** @return color hexadecimal utilizado para la representación visual */
    String visualColor();

    /** @return duración en segundos del proyectil y su efecto */
    double durationSeconds();

    /** @return daño base del tiro especial antes de aplicar multiplicadores */
    double damage();

    /** @return velocidad horizontal inicial en píxeles por segundo */
    double horizontalSpeed();

    /** @return velocidad vertical inicial en píxeles por segundo */
    double verticalSpeed();

    /** @return efecto temporal aplicado al oponente tras el impacto */
    PlayerStatusEffect hitEffect();

    /** @return coeficiente de restitución del balón durante el tiro de poder */
    double restitution();

    /** @return {@code true} si el balón debe mostrarse multiplicado visualmente */
    default boolean renderCopies() { return false; }

    /**
     * Ejecuta el efecto del poder cuando el proyectil impacta al rival.
     *
     * @param owner jugador que lanzó el poder
     * @param opponent jugador rival impactado
     * @throws DopoSoccerException si ocurre un error de validación al aplicar daño o efecto
     */
    void applyImpact(Player owner, Player opponent) throws DopoSoccerException;
}
