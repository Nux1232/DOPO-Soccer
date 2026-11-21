package domain.model.entity;

import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import java.util.List;

/** Poder especial con comportamiento polimórfico al impactar. */
public abstract class PowerShot {
    public static final PowerShot BLUE_AURA = new BlueAuraPower();
    public static final PowerShot ELECTRIC_BOLT = new ElectricBoltPower();
    public static final PowerShot TORNADO = new TornadoPower();
    public static final PowerShot ILLUSION = new IllusionPower();
    private static final List<PowerShot> AVAILABLE = List.of(BLUE_AURA, ELECTRIC_BOLT, TORNADO, ILLUSION);
    private static final double SHOT_DAMAGE_MULTIPLIER = 5.0;

    private final String id, visualAsset, visualColor;
    private final double durationSeconds, baseDamage, horizontalSpeed, verticalSpeed, restitution;
    private final PlayerStatusEffect hitEffect;

    protected PowerShot(String id, String visualAsset, String visualColor, double durationSeconds,
                        double baseDamage, double horizontalSpeed, double verticalSpeed,
                        PlayerStatusEffect hitEffect, double restitution) {
        this.id = id; this.visualAsset = visualAsset; this.visualColor = visualColor;
        this.durationSeconds = durationSeconds; this.baseDamage = baseDamage;
        this.horizontalSpeed = horizontalSpeed; this.verticalSpeed = verticalSpeed;
        this.hitEffect = hitEffect; this.restitution = restitution;
    }
    public final String id() { return id; }
    public final String visualAsset() { return visualAsset; }
    public final String visualColor() { return visualColor; }
    public final double durationSeconds() { return durationSeconds; }
    public final double damage() { return baseDamage * SHOT_DAMAGE_MULTIPLIER; }
    public final double horizontalSpeed() { return horizontalSpeed; }
    public final double verticalSpeed() { return verticalSpeed; }
    public final PlayerStatusEffect hitEffect() { return hitEffect; }
    public final double restitution() { return restitution; }
    public boolean renderCopies() { return false; }

    public void applyImpact(Player owner, Player opponent) throws DopoSoccerException {
        opponent.takeDamage(damage());
        if (!opponent.stunned()) opponent.applyStatusEffect(hitEffect, durationSeconds);
    }
    public static List<PowerShot> available() { return AVAILABLE; }
    public static PowerShot[] values() { return AVAILABLE.toArray(PowerShot[]::new); }

    private static final class BlueAuraPower extends PowerShot {
        private BlueAuraPower() { super("blue-aura", "Ko_power.gif", "#36a9ff", 1.5, 8, 820, 0, PlayerStatusEffect.PUSHED, 0.72); }
        @Override public void applyImpact(Player owner, Player opponent) throws DopoSoccerException {
            super.applyImpact(owner, opponent);
            if (!opponent.stunned()) opponent.setVelocity(opponent.x() < Game.WIDTH / 2 ? -600 : 600, -180);
        }
    }
    private static final class ElectricBoltPower extends PowerShot {
        private ElectricBoltPower() { super("electric-bolt", "Cm_power.gif", "#ffe34a", 3.0, 12, 760, 0, PlayerStatusEffect.ELECTRIFIED, 0.72); }
    }
    private static final class TornadoPower extends PowerShot {
        private TornadoPower() { super("tornado", "Ng_power.gif", "#48d99a", 3.0, 8, 650, -120, PlayerStatusEffect.UNCONSCIOUS, 0.95); }
        @Override public void applyImpact(Player owner, Player opponent) throws DopoSoccerException {
            super.applyImpact(owner, opponent);
            if (!opponent.stunned()) opponent.setVelocity(opponent.x() < Game.WIDTH / 2 ? -120 : 120, -560);
        }
    }
    private static final class IllusionPower extends PowerShot {
        private IllusionPower() { super("illusion", "usa-illusion.gif", "#c5a0ff", 2.5, 6, 700, -50, PlayerStatusEffect.CONFUSED, 0.72); }
        @Override public boolean renderCopies() { return true; }
    }
}
