package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Contrato del poder especial que implementa cada personaje/país. */
public interface Power {
    String id();
    String visualAsset();
    String visualColor();
    double durationSeconds();
    double damage();
    double horizontalSpeed();
    double verticalSpeed();
    PlayerStatusEffect hitEffect();
    double restitution();
    default boolean renderCopies() { return false; }
    void applyImpact(Player owner, Player opponent) throws DopoSoccerException;
}
