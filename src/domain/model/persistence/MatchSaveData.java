package domain.model.persistence;

import domain.model.ai.AIProfile;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.PowerState;
import domain.model.game.MatchType;
import domain.model.game.PlayerMode;

import java.io.Serial;
import java.io.Serializable;

/**
 * Instantánea serializable y versionada con todo el estado necesario para reanudar un partido.
 */
public record MatchSaveData(
        int version,
        PlayerMode playerMode,
        MatchType matchType,
        String firstCountryId,
        String secondCountryId,
        AIProfile aiProfile,
        int goalLimit,
        int durationSeconds,
        double remainingSeconds,
        boolean suddenDeath,
        double kickoffDelaySeconds,
        int winner,
        boolean finished,
        boolean rewardsApplied,
        PlayerState firstPlayer,
        PlayerState secondPlayer,
        BallState ball) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Versión actual del formato de serialización de partidas. */
    public static final int CURRENT_VERSION = 1;

    /** Valida la integridad mínima de los campos serializados del partido. */
    public MatchSaveData {
        if (version != CURRENT_VERSION
                || playerMode == null || matchType == null
                || firstCountryId == null || secondCountryId == null
                || aiProfile == null || firstPlayer == null || secondPlayer == null || ball == null) {
            throw new IllegalArgumentException("Estado de partido guardado inválido.");
        }
    }

    /** Estado serializable de un jugador dentro de una partida guardada. */
    public record PlayerState(
            PlayerProfile profile,
            int goals,
            double health,
            double powerCharge,
            PowerState power,
            double x,
            double y,
            double velocityX,
            double velocityY,
            boolean grounded,
            double kickCooldown,
            double kickAnimationTimer,
            double stunTimer,
            int facingDirection,
            PlayerStatusEffect statusEffect,
            double statusEffectTimer,
            double impactLockTimer) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** Valida los objetos obligatorios del estado guardado del jugador. */
        public PlayerState {
            if (profile == null || power == null || statusEffect == null) {
                throw new IllegalArgumentException("Estado de jugador guardado inválido.");
            }
        }
    }

    /** Estado serializable del balón y de su tiro especial activo si existe. */
    public record BallState(
            double x,
            double y,
            double velocityX,
            double velocityY,
            String powerId,
            int powerOwner,
            double powerTime,
            boolean powerEffectApplied) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** Valida la consistencia entre el propietario y el identificador del tiro especial. */
        public BallState {
            if (powerOwner < 0 || powerOwner > 2) {
                throw new IllegalArgumentException("Propietario del poder guardado inválido.");
            }
            if ((powerId == null) != (powerOwner == 0)) {
                throw new IllegalArgumentException("Estado del tiro guardado inconsistente.");
            }
        }
    }
}
