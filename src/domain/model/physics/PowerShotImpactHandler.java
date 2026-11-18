package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.PowerShot;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import java.util.EnumMap;
import java.util.Map;

/** Aplica impactos de poderes; las reacciones específicas se pueden registrar. */
public final class PowerShotImpactHandler {
    @FunctionalInterface public interface ImpactMotion { void apply(Player opponent); }
    private final Map<PowerShot, ImpactMotion> motions;

    public PowerShotImpactHandler() { this(defaultMotions()); }
    public PowerShotImpactHandler(Map<PowerShot, ImpactMotion> motions) {
        this.motions = new EnumMap<>(PowerShot.class); this.motions.putAll(motions);
    }
    public void register(PowerShot shot, ImpactMotion motion) { motions.put(shot, motion); }

    public void apply(Ball ball, Player first, Player second) throws DopoSoccerException {
        PowerShot shot = ball.activePowerShot();
        Player owner = ball.powerShotOwner();
        if (shot == null || owner == null || ball.powerEffectApplied()) return;
        Player opponent = owner == first ? second : first;
        if (Math.hypot(ball.x() - opponent.x(), ball.y() - opponent.y()) > Ball.RADIUS + Player.RADIUS) return;
        ball.markPowerEffectApplied();
        opponent.takeDamage(shot.damage());
        if (opponent.stunned()) return;
        opponent.applyStatusEffect(shot.hitEffect(), shot.durationSeconds());
        ImpactMotion motion = motions.get(shot);
        if (motion != null) motion.apply(opponent);
    }

    private static Map<PowerShot, ImpactMotion> defaultMotions() {
        Map<PowerShot, ImpactMotion> defaults = new EnumMap<>(PowerShot.class);
        defaults.put(PowerShot.BLUE_AURA, opponent -> {
            double direction = opponent.x() < Game.WIDTH / 2 ? -1.0 : 1.0;
            opponent.setVelocity(direction * 600.0, -180.0);
        });
        defaults.put(PowerShot.TORNADO, opponent -> {
            double direction = opponent.x() < Game.WIDTH / 2 ? -1.0 : 1.0;
            opponent.setVelocity(direction * 120.0, -560.0);
        });
        return defaults;
    }
}
