package domain.model.physics;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.ProjectilePower;
import domain.model.exception.DopoSoccerException;
import java.util.IdentityHashMap;
import java.util.Map;

/** Detecta el impacto del balón especial y delega el efecto de cada poder en su comportamiento polimórfico. */
public final class PowerShotImpactHandler {
    /** Reacción física adicional opcional al impactar al jugador contrario. */
    @FunctionalInterface public interface ImpactMotion {
        /**
         * Aplica un efecto complementario sobre el oponente impactado.
         *
         * @param opponent jugador que recibió el tiro especial
         */
        void apply(Player opponent);
    }

    private final Map<ProjectilePower, ImpactMotion> additionalMotions = new IdentityHashMap<>();

    /** Crea un manejador sin reacciones físicas adicionales registradas. */
    public PowerShotImpactHandler() { }

    /**
     * Permite agregar reacciones adicionales sin reemplazar el comportamiento propio de cada poder.
     *
     * @param motions mapa de poderes y sus reacciones complementarias
     */
    public PowerShotImpactHandler(Map<ProjectilePower, ImpactMotion> motions) {
        if (motions != null) additionalMotions.putAll(motions);
    }

    /**
     * Registra una reacción física adicional asociada a un poder específico.
     *
     * @param shot poder al que se asocia la reacción
     * @param motion reacción complementaria a ejecutar tras el impacto
     * @throws DopoSoccerException si el poder o la reacción son {@code null}
     */
    public void register(ProjectilePower shot, ImpactMotion motion) {
        if (shot == null || motion == null) throw new DopoSoccerException("Poder y reacción requeridos");
        additionalMotions.put(shot, motion);
    }

    /**
     * Evalúa si el balón con tiro especial activo impactó al oponente y aplica sus efectos una sola vez.
     *
     * @param ball balón en juego
     * @param first primer jugador del partido
     * @param second segundo jugador del partido
     * @throws DopoSoccerException si ocurre un error de dominio al aplicar el daño o impacto
     */
    public void apply(Ball ball, Player first, Player second) throws DopoSoccerException {
        ProjectilePower shot = ball.activePowerShot();
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
