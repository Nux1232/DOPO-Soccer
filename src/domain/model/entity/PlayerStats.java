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

    /**
     * Restaura la progresión persistente validando los rangos a través de {@link PlayerProfile}.
     *
     * @param profile perfil persistente a restaurar
     * @throws DopoSoccerException si el perfil es nulo
     */
    public void restore(PlayerProfile profile) {
        if (profile == null) throw new DopoSoccerException("Perfil requerido");
        speed=profile.speed(); kick=profile.kick(); jump=profile.jump(); survival=profile.survival();
        points=profile.points(); nextUpgradeCost=profile.nextUpgradeCost();
    }

    /**
     * Construye una instantánea inmutable de las estadísticas actuales.
     *
     * @return perfil serializable con la progresión del jugador
     */
    public PlayerProfile profile() {
        return new PlayerProfile(speed,kick,jump,survival,points,nextUpgradeCost);
    }

    /** @return nivel actual de velocidad (1 a 10) */
    public int speed() {
        return speed;
    }

    /** @return nivel actual de patada (1 a 10) */
    public int kick() {
        return kick;
    }

    /** @return nivel actual de salto (1 a 10) */
    public int jump() {
        return jump;
    }

    /** @return nivel actual de supervivencia (1 a 10) */
    public int survival() {
        return survival;
    }

    /** @return puntos disponibles para comprar mejoras */
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
     * @throws DopoSoccerException si {@code amount} es negativo
     */
    public void addPoints(int amount) throws DopoSoccerException {
        if (amount < 0) {
            throw new DopoSoccerException(DopoSoccerException.NEGATIVE_POINTS);
        }
        points += amount;
    }

    /** Mejora en 1 punto la estadística de velocidad consumiendo puntos acumulados. */
    public void improveSpeed() {
        speed = improve(speed);
    }

    /** Mejora en 1 punto la estadística de patada consumiendo puntos acumulados. */
    public void improveKick() {
        kick = improve(kick);
    }

    /** Mejora en 1 punto la estadística de salto consumiendo puntos acumulados. */
    public void improveJump() {
        jump = improve(jump);
    }

    /** Mejora en 1 punto la estadística de supervivencia consumiendo puntos acumulados. */
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