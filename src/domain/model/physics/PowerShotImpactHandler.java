package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.Power;
import domain.model.exception.DopoSoccerException;
import java.util.IdentityHashMap;
import java.util.Map;

/** Detecta el impacto y delega el efecto de cada poder en su comportamiento polimórfico. */
public final class PowerShotImpactHandler {
    @FunctionalInterface public interface ImpactMotion { void apply(Player opponent); }
    private final Map<Power, ImpactMotion> additionalMotions = new IdentityHashMap<>();

    public PowerShotImpactHandler() { }
    /** Permite agregar una reacción adicional sin reemplazar el comportamiento propio del poder. */
    public PowerShotImpactHandler(Map<Power, ImpactMotion> motions) {
        if (motions != null) additionalMotions.putAll(motions);
    }
    public void register(Power shot, ImpactMotion motion) {
        if (shot == null || motion == null) throw new DopoSoccerException("Poder y reacción requeridos");
        additionalMotions.put(shot, motion);
    }

    public void apply(Ball ball, Player first, Player second) throws DopoSoccerException {
        Power shot = ball.activePowerShot();
        Player owner = ball.powerShotOwner();
        if (shot == null || owner == null || ball.powerEffectApplied()) return;
        Player opponent = owner == first ? second : first;
        if (Math.hypot(ball.x() - opponent.x(), ball.y() - opponent.y()) > Ball.RADIUS + Player.RADIUS) return;
        ball.markPowerEffectApplied();
        shot.applyImpact(owner, opponent);
        ImpactMotion extra = additionalMotions.get(shot);
        if (extra != null && !opponent.stunned()) extra.apply(opponent);
    }
}
