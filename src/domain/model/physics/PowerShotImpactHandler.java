package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.PowerShot;
import domain.model.exception.DopoSoccerException;

/** Detecta impactos; cada PowerShot ejecuta su propia regla al impactar. */
public final class PowerShotImpactHandler {
    public void apply(Ball ball, Player first, Player second) throws DopoSoccerException {
        PowerShot shot = ball.activePowerShot();
        Player owner = ball.powerShotOwner();
        if (shot == null || owner == null || ball.powerEffectApplied()) return;
        Player opponent = owner == first ? second : first;
        if (Math.hypot(ball.x() - opponent.x(), ball.y() - opponent.y()) > Ball.RADIUS + Player.RADIUS) return;
        ball.markPowerEffectApplied();
        shot.applyImpact(owner, opponent);
    }
}
