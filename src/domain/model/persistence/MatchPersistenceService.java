package domain.model.persistence;

import domain.model.ai.AIProfile;
import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.Power;
import domain.model.entity.PowerState;
import domain.model.entity.PlayerStatusEffect;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.MatchConfiguration;
import domain.model.game.MatchRestorer;
import domain.model.game.MatchRestoreRequest;

import java.nio.file.Path;

/** Coordina el guardado y restauración de instantáneas tipadas del partido con el almacén serializable. */
public final class MatchPersistenceService {
    private final MatchSaveRepository store;
    private final MatchRestorer matchRestorer;

    /** Crea el servicio usando el archivo de guardado por defecto. */
    public MatchPersistenceService() {
        this(new MatchSaveStore(), new domain.factory.MatchFactory());
    }

    /** Crea el servicio con el repositorio y restaurador predeterminados. */
    public MatchPersistenceService(MatchRestorer matchRestorer) {
        this(new MatchSaveStore(), matchRestorer);
    }

    /**
     * Crea el servicio apuntando a una ruta específica de archivo.
     *
     * @param file ruta del archivo de partida guardada
     */
    public MatchPersistenceService(Path file) {
        this(new MatchSaveStore(file), new domain.factory.MatchFactory());
    }

    /**
     * Crea el servicio para una ruta y una fábrica configurada de modalidades.
     *
     * @param file ruta del archivo de partida guardada
     * @param matchRestorer estrategia utilizada para reconstruir el tipo concreto de partido
     */
    public MatchPersistenceService(Path file, MatchRestorer matchRestorer) {
        this(new MatchSaveStore(file), matchRestorer);
    }

    /**
     * Crea el servicio con un repositorio serializable inyectado.
     *
     * @param store repositorio responsable de leer y escribir la partida
     */
    public MatchPersistenceService(MatchSaveRepository store) {
        this(store, new domain.factory.MatchFactory());
    }

    /**
     * Crea el servicio con repositorio y estrategia de restauración inyectados.
     *
     * @param store repositorio serializable responsable de leer y escribir
     * @param matchRestorer estrategia usada para reconstruir los partidos cargados
     */
    public MatchPersistenceService(MatchSaveRepository store, MatchRestorer matchRestorer) {
        if (store == null) throw new DopoSoccerException("El repositorio de partidas es obligatorio.");
        if (matchRestorer == null) throw new DopoSoccerException("El restaurador de partidos es obligatorio.");
        this.store = store;
        this.matchRestorer = matchRestorer;
    }

    /**
     * Serializa y guarda el estado completo de un partido en curso o terminado.
     *
     * @param game partido activo a persistir
     * @param aiProfile perfil de máquina asociado al partido
     * @param rewardsApplied indica si ya se otorgaron los puntos de recompensa
     */
    public void save(Game game, AIProfile aiProfile, boolean rewardsApplied) {
        if (game == null || aiProfile == null) {
            throw new DopoSoccerException("El partido y el perfil de IA son obligatorios.");
        }

        MatchConfiguration configuration = game.matchConfiguration();
        matchRestorer.validateConfiguration(configuration);

        MatchSaveData data = new MatchSaveData(
                MatchSaveData.CURRENT_VERSION,
                game.playerMode(),
                game.firstPlayer().country().id(),
                game.secondPlayer().country().id(),
                aiProfile,
                configuration,
                game.kickoffDelaySeconds(),
                game.winner(),
                game.isFinished(),
                rewardsApplied,
                snapshot(game.firstPlayer()),
                snapshot(game.secondPlayer()),
                snapshot(game.ball(), game));
        store.save(data);
    }

    /**
     * Carga una partida previamente guardada y reconstruye sus entidades y temporizadores.
     *
     * @return resultado con el partido reconstruido, perfil de IA y estado de recompensas
     */
    public LoadedMatch load() {
        MatchSaveData data = store.load();
        Game game = matchRestorer.restore(
                new MatchRestoreRequest(
                        data.playerMode(),
                        data.firstCountryId(),
                        data.secondCountryId(),
                        data.aiProfile(),
                        data.matchConfiguration()));
        if (game == null) {
            throw new DopoSoccerException("El restaurador devolvió un partido nulo.");
        }

        restore(data.firstPlayer(), game.firstPlayer());
        restore(data.secondPlayer(), game.secondPlayer());
        game.restoreKickoffDelay(data.kickoffDelaySeconds());

        MatchSaveData.BallState ballState = data.ball();
        Ball ball = game.ball();
        ball.setPosition(ballState.x(), ballState.y());
        ball.setVelocity(ballState.velocityX(), ballState.velocityY());
        if (ballState.powerId() != null) {
            Player owner = ballState.powerOwner() == 1 ? game.firstPlayer() : game.secondPlayer();
            Power power = owner.country().power();
            if (!power.restoreActiveShot(ball, owner, ballState.powerId(),
                    ballState.powerTime(), ballState.powerEffectApplied())) {
                throw new DopoSoccerException("El tiro guardado no coincide con su propietario.");
            }
        }
        game.restoreMatchConfiguration(data.matchConfiguration());
        if (data.finished()) {
            game.restoreLifecycle(data.winner());
        }
        return new LoadedMatch(game, data.aiProfile(), data.rewardsApplied());
    }

    /** @return {@code true} si existe una partida guardada en la ruta configurada */
    public boolean exists() {
        return store.exists();
    }

    /** @return ruta absoluta del archivo gestionado por este servicio */
    public Path filePath() {
        return store.storagePath().orElseThrow(
                () -> new IllegalStateException("El repositorio configurado no utiliza un archivo."));
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
                player.impactLockTimer(),
                player.country().power().saveState(player));
    }

    private MatchSaveData.BallState snapshot(Ball ball, Game game) {
        String powerId = ball.activePowerShotId();
        int owner = ball.powerShotOwner() == game.firstPlayer() ? 1
                : ball.powerShotOwner() == game.secondPlayer() ? 2 : 0;
        if ((powerId == null) != (owner == 0)) {
            throw new DopoSoccerException("El estado del tiro activo está inconsistente.");
        }
        return new MatchSaveData.BallState(
                ball.x(),
                ball.y(),
                ball.velocityX(),
                ball.velocityY(),
                powerId,
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
        player.country().power().restoreState(player, state.powerExtensionState());
    }

    /**
     * Resultado de cargar una partida guardada.
     *
     * @param game partido reconstruido
     * @param aiProfile perfil de IA configurado en esa partida
     * @param rewardsApplied indica si los puntos de recompensa ya habían sido entregados
     */
    public record LoadedMatch(Game game, AIProfile aiProfile, boolean rewardsApplied) { }
}
