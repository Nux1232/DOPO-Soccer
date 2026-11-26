package domain.model.persistence;

import domain.factory.MatchFactory;
import domain.model.ai.AIProfile;
import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.Power;
import domain.model.entity.PowerState;
import domain.model.entity.PlayerStatusEffect;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchType;
import domain.model.game.TimeGame;

import java.nio.file.Path;

/** Coordinates typed match snapshots with the serializable file store. */
public final class MatchPersistenceService {
    private final MatchSaveStore store;

    public MatchPersistenceService() {
        this(new MatchSaveStore());
    }

    public MatchPersistenceService(Path file) {
        this(new MatchSaveStore(file));
    }

    public MatchPersistenceService(MatchSaveStore store) {
        if (store == null) throw new DopoSoccerException("El almacén de partidas es obligatorio.");
        this.store = store;
    }

    public void save(Game game, AIProfile aiProfile, boolean rewardsApplied) {
        if (game == null || aiProfile == null) {
            throw new DopoSoccerException("El partido y el perfil de IA son obligatorios.");
        }

        MatchType matchType = game instanceof TimeGame ? MatchType.TIME : MatchType.GOALS;
        int goalLimit = game instanceof GoalGame goalGame ? goalGame.goalLimit() : 10;
        int duration = game instanceof TimeGame timeGame ? timeGame.durationSeconds() : 240;
        double remaining = game instanceof TimeGame timeGame ? timeGame.remainingExactSeconds() : 0;
        boolean suddenDeath = game instanceof TimeGame timeGame && timeGame.suddenDeath();

        MatchSaveData data = new MatchSaveData(
                MatchSaveData.CURRENT_VERSION,
                game.playerMode(),
                matchType,
                game.firstPlayer().country().id(),
                game.secondPlayer().country().id(),
                aiProfile,
                goalLimit,
                duration,
                remaining,
                suddenDeath,
                game.kickoffDelaySeconds(),
                game.winner(),
                game.isFinished(),
                rewardsApplied,
                snapshot(game.firstPlayer()),
                snapshot(game.secondPlayer()),
                snapshot(game.ball(), game));
        store.save(data);
    }

    public LoadedMatch load() {
        MatchSaveData data = store.load();
        Game game = MatchFactory.create(
                data.playerMode(),
                data.matchType(),
                data.firstCountryId(),
                data.secondCountryId(),
                data.goalLimit(),
                data.durationSeconds(),
                data.aiProfile());

        restore(data.firstPlayer(), game.firstPlayer());
        restore(data.secondPlayer(), game.secondPlayer());
        game.restoreKickoffDelay(data.kickoffDelaySeconds());

        MatchSaveData.BallState ballState = data.ball();
        Ball ball = game.ball();
        ball.setPosition(ballState.x(), ballState.y());
        ball.setVelocity(ballState.velocityX(), ballState.velocityY());
        if (ballState.powerId() != null) {
            Player owner = ballState.powerOwner() == 1 ? game.firstPlayer() : game.secondPlayer();
            if (!owner.country().id().equals(ballState.powerId())) {
                throw new DopoSoccerException("El tiro guardado no coincide con su propietario.");
            }
            ball.restorePowerShot(owner.country(), owner, ballState.powerTime(),
                    ballState.powerEffectApplied());
        }
        if (game instanceof TimeGame timeGame) {
            timeGame.restoreClock(data.remainingSeconds(), data.suddenDeath());
        }
        if (data.finished()) {
            game.restoreLifecycle(data.winner());
        }
        return new LoadedMatch(game, data.aiProfile(), data.rewardsApplied());
    }

    public boolean exists() {
        return store.exists();
    }

    public Path filePath() {
        return store.filePath();
    }

    private MatchSaveData.PlayerState snapshot(Player player) {
        return new MatchSaveData.PlayerState(
                player.stats().profile(),
                player.goals(),
                player.health(),
                player.powerCharge(),
                player.power(),
                player.x(),
                player.y(),
                player.velocityX(),
                player.velocityY(),
                player.grounded(),
                player.kickCooldown(),
                player.kickAnimationTimer(),
                player.stunTimer(),
                player.facingDirection(),
                player.statusEffect(),
                player.statusEffectTimer(),
                player.impactLockTimer());
    }

    private MatchSaveData.BallState snapshot(Ball ball, Game game) {
        Power power = ball.activePowerShot();
        int owner = ball.powerShotOwner() == game.firstPlayer() ? 1
                : ball.powerShotOwner() == game.secondPlayer() ? 2 : 0;
        if ((power == null) != (owner == 0)) {
            throw new DopoSoccerException("El estado del tiro activo está inconsistente.");
        }
        return new MatchSaveData.BallState(
                ball.x(),
                ball.y(),
                ball.velocityX(),
                ball.velocityY(),
                power == null ? null : power.id(),
                owner,
                ball.powerShotTime(),
                ball.powerEffectApplied());
    }

    private void restore(MatchSaveData.PlayerState state, Player player) {
        player.restoreProfile(state.profile());
        player.restoreMatchState(
                state.goals(),
                state.health(),
                state.powerCharge(),
                state.power(),
                state.x(),
                state.y(),
                state.velocityX(),
                state.velocityY(),
                state.grounded(),
                state.kickCooldown(),
                state.kickAnimationTimer(),
                state.stunTimer(),
                state.facingDirection(),
                state.statusEffect(),
                state.statusEffectTimer(),
                state.impactLockTimer());
    }

    public record LoadedMatch(Game game, AIProfile aiProfile, boolean rewardsApplied) { }
}
