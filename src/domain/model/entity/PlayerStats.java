package domain.model.entity;
import domain.model.exception.DopoSoccerException;
import domain.model.entity.service.PlayerProgression;
import domain.model.persistence.PlayerProfile;

/**
 * Estadísticas de un jugador: velocidad, patada, salto y supervivencia,
 * junto con los puntos acumulados que se gastan para mejorarlas.
 *
 * Según el enunciado, todas empiezan en 1 y suben hasta 10.
 * El precio del primer punto de mejora es 500 y se duplica con cada mejora
 * (500, 1000, 2000, ...).
 */
public final class PlayerStats {

    public static final int INITIAL_LEVEL = 1;
    public static final int MAX_LEVEL = 10;
    public static final int INITIAL_UPGRADE_COST = 500;

    /** Nivel de velocidad de desplazamiento (1 a 10). */
    private int speed = INITIAL_LEVEL;

    /** Nivel de patada (1 a 10). */
    private int kick = INITIAL_LEVEL;

    /** Nivel de salto (1 a 10). */
    private int jump = INITIAL_LEVEL;

    /** Nivel de supervivencia / vida máxima (1 a 10). */
    private int survival = INITIAL_LEVEL;

    /** Puntos acumulados disponibles para comprar mejoras. */
    private int points;

    /** Costo en puntos de la próxima mejora (empieza en 500 y se duplica). */
    private int nextUpgradeCost = INITIAL_UPGRADE_COST;

    /** Restores persistent progression, validating values through PlayerProfile. */
    public void restore(PlayerProfile profile) {
        if (profile == null) throw new DopoSoccerException("Perfil requerido");
        speed=profile.speed(); kick=profile.kick(); jump=profile.jump(); survival=profile.survival();
        points=profile.points(); nextUpgradeCost=profile.nextUpgradeCost();
    }
    public PlayerProfile profile() {
        return new PlayerProfile(speed,kick,jump,survival,points,nextUpgradeCost);
    }

    public int speed() {
        return speed;
    }

    public int kick() {
        return kick;
    }

    public int jump() {
        return jump;
    }

    public int survival() {
        return survival;
    }

    public int points() {
        return points;
    }

    /**
     * Devuelve el costo en puntos de la siguiente mejora.
     *
     * @return costo actual (500, 1000, 2000, ...)
     */
    public int nextUpgradeCost() {
        return nextUpgradeCost;
    }

    /**
     * Suma puntos al jugador (por ejemplo, 100 al ganar o 25 al perder).
     *
     * @param amount puntos a sumar (no negativo)
     */
    public void addPoints(int amount) throws DopoSoccerException {
        if (amount < 0) {
            throw new DopoSoccerException(DopoSoccerException.NEGATIVE_POINTS);
        }
        points += amount;
    }

    public void improveSpeed() {
        speed = improve(speed);
    }

    public void improveKick() {
        kick = improve(kick);
    }

    public void improveJump() {
        jump = improve(jump);
    }

    public void improveSurvival() {
        survival = improve(survival);
    }

    private int improve(int currentValue) {
        if (currentValue >= MAX_LEVEL) {
            throw new DopoSoccerException(DopoSoccerException.STAT_AT_MAX);
        }
        if (points < nextUpgradeCost) {
            throw new DopoSoccerException(DopoSoccerException.INSUFFICIENT_POINTS);
        }
        points -= nextUpgradeCost;
        nextUpgradeCost = PlayerProgression.nextCost(nextUpgradeCost);
        return PlayerProgression.nextLevel(currentValue, MAX_LEVEL);
    }
}