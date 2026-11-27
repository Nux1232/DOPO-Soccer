package domain.model.game;

import domain.model.exception.DopoSoccerException;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.PlayerProfileService;

/**
 * Coordina la entrega idempotente de recompensas y su persistencia al finalizar un partido.
 */
public final class MatchCompletionService {
    private final MatchRewardService rewardService;
    private final PlayerProfileService profileService;
    private boolean rewardsApplied;

    /**
     * Crea el coordinador de finalización con sus colaboradores.
     *
     * @param rewardService reglas de asignación de puntos
     * @param profileService repositorio lógico de perfiles
     */
    public MatchCompletionService(
            MatchRewardService rewardService, PlayerProfileService profileService) {
        if (rewardService == null || profileService == null) {
            throw new DopoSoccerException("Los servicios de finalización son obligatorios.");
        }
        this.rewardService = rewardService;
        this.profileService = profileService;
    }

    /** Reinicia la marca de recompensas al comenzar un partido nuevo. */
    public void reset() {
        rewardsApplied = false;
    }

    /**
     * Entrega y guarda recompensas una sola vez cuando el partido termina.
     *
     * @param game partido ya finalizado
     */
    public void complete(Game game) {
        if (game == null || !game.isFinished()) {
            throw new DopoSoccerException("El partido debe estar finalizado para completar sus recompensas.");
        }
        if (rewardsApplied) return;
        PlayerProfile first = withReward(game.firstPlayer().stats().profile(), rewardService.rewardFor(game, 1));
        PlayerProfile second = withReward(game.secondPlayer().stats().profile(), rewardService.rewardFor(game, 2));
        if (game.playerMode() == PlayerMode.PLAYER_VS_MACHINE) {
            profileService.saveProfile("player1", first);
        } else {
            profileService.saveMatchProfiles(first, second);
        }
        rewardService.award(game);
        rewardsApplied = true;
    }

    private PlayerProfile withReward(PlayerProfile profile, int reward) {
        return new PlayerProfile(profile.speed(), profile.kick(), profile.jump(), profile.survival(),
                Math.addExact(profile.points(), reward), profile.nextUpgradeCost());
    }

    /** @return {@code true} si las recompensas ya se entregaron en esta sesión */
    public boolean rewardsApplied() {
        return rewardsApplied;
    }

    /** Restaura la marca al cargar un snapshot de partido. */
    public void restoreRewardsApplied(boolean applied) {
        rewardsApplied = applied;
    }
}
