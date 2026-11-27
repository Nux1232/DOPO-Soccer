package domain.model.persistence;

import domain.model.ai.AIProfile;
import domain.model.entity.PlayerStatusEffect;
import domain.model.entity.PowerState;
import domain.model.game.MatchConfiguration;
import domain.model.game.PlayerMode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Instantánea serializable y versionada con todo el estado necesario para reanudar un partido.
 */
public record MatchSaveData(
        int version,
        PlayerMode playerMode,
        String firstCountryId,
        String secondCountryId,
        AIProfile aiProfile,
        MatchConfiguration matchConfiguration,
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
    public static final int CURRENT_VERSION = 6;
    /** Última versión anterior que todavía puede migrarse automáticamente. */
    public static final int LEGACY_VERSION = 5;

    /** Valida la integridad mínima de los campos serializados del partido. */
    public MatchSaveData {
        if ((version != CURRENT_VERSION && version != LEGACY_VERSION)
                || playerMode == null || matchConfiguration == null
                || firstCountryId == null || secondCountryId == null
                || aiProfile == null || firstPlayer == null || secondPlayer == null || ball == null) {
            throw new IllegalArgumentException("Estado de partido guardado inválido.");
        }
    }

    /** Copia esta instantánea en el formato actual y con la configuración migrada. */
    public MatchSaveData migratedToCurrentVersion(MatchConfiguration migratedConfiguration) {
        return new MatchSaveData(
                CURRENT_VERSION, playerMode, firstCountryId, secondCountryId, aiProfile,
                migratedConfiguration, kickoffDelaySeconds, winner, finished, rewardsApplied,
                firstPlayer, secondPlayer, ball);
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
            double impactLockTimer,
            Map<String, String> powerExtensionState) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** Valida los objetos obligatorios del estado guardado del jugador. */
        public PlayerState {
            if (profile == null || power == null || statusEffect == null) {
                throw new IllegalArgumentException("Estado de jugador guardado inválido.");
            }
            powerExtensionState = powerExtensionState == null
                    ? Map.of() : new TreeMap<>(powerExtensionState);
            if (powerExtensionState.entrySet().stream()
                    .anyMatch(entry -> entry.getKey().isBlank() || entry.getValue() == null)) {
                throw new IllegalArgumentException("Estado adicional del poder guardado inválido.");
            }
        }

        @Override
        public Map<String, String> powerExtensionState() {
            return Map.copyOf(powerExtensionState);
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
