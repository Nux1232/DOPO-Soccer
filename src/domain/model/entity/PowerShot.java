package domain.model.entity;

/**
 * Configuración de los tiros especiales. Los recursos visuales se cargan
 * desde presentation usando el nombre de archivo definido por cada tiro.
 */
public enum PowerShot {
    BLUE_AURA("Ko_power.gif", "#36a9ff", 1.5, 8, 820, 0,
            PlayerStatusEffect.PUSHED),
    ELECTRIC_BOLT("Cm_power.gif", "#ffe34a", 3.0, 12, 760, 0,
            PlayerStatusEffect.ELECTRIFIED),
    TORNADO("Ng_power.gif", "#48d99a", 3.0, 8, 650, -120,
            PlayerStatusEffect.UNCONSCIOUS),
    ILLUSION("usa-illusion.gif", "#c5a0ff", 2.5, 6, 700, -50,
            PlayerStatusEffect.CONFUSED);

    private static final double SHOT_DAMAGE_MULTIPLIER = 5.0;

    private final String visualAsset;
    private final String visualColor;
    private final double durationSeconds;
    private final double baseDamage;
    private final double horizontalSpeed;
    private final double verticalSpeed;
    private final PlayerStatusEffect hitEffect;

    PowerShot(
            String visualAsset,
            String visualColor,
            double durationSeconds,
            double baseDamage,
            double horizontalSpeed,
            double verticalSpeed,
            PlayerStatusEffect hitEffect) {
        this.visualAsset = visualAsset;
        this.visualColor = visualColor;
        this.durationSeconds = durationSeconds;
        this.baseDamage = baseDamage;
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
        this.hitEffect = hitEffect;
    }

    public String visualAsset() {
        return visualAsset;
    }

    public String visualColor() {
        return visualColor;
    }

    public double durationSeconds() {
        return durationSeconds;
    }

    public double damage() {
        return baseDamage * SHOT_DAMAGE_MULTIPLIER;
    }

    public double horizontalSpeed() {
        return horizontalSpeed;
    }

    public double verticalSpeed() {
        return verticalSpeed;
    }

    public PlayerStatusEffect hitEffect() {
        return hitEffect;
    }
}
