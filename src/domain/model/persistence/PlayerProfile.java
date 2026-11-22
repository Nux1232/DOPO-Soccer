package domain.model.persistence;

import domain.model.exception.DopoSoccerException;

/** Datos permanentes del jugador. No guarda estados temporales del partido. */
public record PlayerProfile(int speed, int kick, int jump, int survival, int points, int nextUpgradeCost) {
    public PlayerProfile {
        if (speed < 1 || speed > 10 || kick < 1 || kick > 10
                || jump < 1 || jump > 10 || survival < 1 || survival > 10
                || points < 0 || nextUpgradeCost < 1) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_PROFILE);
        }
    }
}
