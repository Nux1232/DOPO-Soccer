package domain;

import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.entity.Player;
import domain.model.entity.Ball;
import domain.model.entity.Power;
import domain.factory.MatchFactory;
import domain.model.ai.FootballAI;
import domain.model.ai.AIProfile;
import domain.model.persistence.ProfileStore;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.MatchSaveStore;
import domain.model.entity.PlayerStats;
import domain.model.entity.PowerState;
import domain.model.entity.PlayerStatusEffect;
import domain.model.game.GoalGame;
import domain.model.game.TimeGame;
import java.util.Properties;

/**
 * Fachada pública del dominio DOPO-SOCCER.
 * La presentación se comunica exclusivamente con esta clase. Valida solicitudes,
 * administra el ciclo de vida del partido activo y delega las reglas en el dominio.
 */
public final class DopoSoccer {
    private Game currentMatch;
    private final FootballAI ai = new FootballAI();
    private final ProfileStore profiles = new ProfileStore();
    private final MatchSaveStore matchSaves = new MatchSaveStore();
    private boolean rewardsApplied;
    private AIProfile aiProfile = AIProfile.EXPERT;


    public enum MatchKind { GOALS, TIME }
    public enum PlayerModeKind { PLAYER_VS_PLAYER, PLAYER_VS_MACHINE }
    public enum CountrySelection { SOUTH_KOREA, CAMEROON, NIGERIA, USA }
    public enum MatchStatus { NO_MATCH, ACTIVE, FINISHED }

    /** Contrato público para iniciar un partido, sin exponer entidades. */
    public record MatchRequest(PlayerModeKind playerMode, MatchKind matchType,
            String firstCountry, String secondCountry, int goalLimit, int durationSeconds, String aiProfile) {
        public MatchRequest(PlayerModeKind mode, MatchKind type, String first, String second,
                int goals, int seconds) {
            this(mode, type, first, second, goals, seconds, "EXPERT");
        }

        /** Compatibility constructor for the original built-in-country enum API. */
        public MatchRequest(PlayerModeKind mode, MatchKind type, CountrySelection first,
                CountrySelection second, int goals, int seconds) {
            this(mode,type,first==null?null:first.name(),second==null?null:second.name(),goals,seconds,"EXPERT");
        }
    }

    /** Acciones abstractas independientes del teclado o dispositivo. */
    public record PlayerActions(boolean left, boolean right, boolean jump, boolean kick, boolean power) { }

    /** Estado de jugador de solo lectura para construir una representación visual. */
    public record PlayerSnapshot(String countryName, String jerseyColor, double x, double y,
            int goals, double health, double maxHealth, double powerCharge, int points,
            int facingDirection, boolean kicking, boolean stunned, String power,
            String statusEffect) { }

    /** Estado de partido de solo lectura. No expone entidades modificables. */
    public record MatchSnapshot(double width, double height, double groundY,
            String modeDescription, boolean finished, int winner,
            PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
            double ballX, double ballY, double ballRadius,
            String activePowerShot, String powerShotAsset, String powerShotColor,
            double powerShotDuration, double powerShotElapsed) { }

    public void startMatch(MatchRequest request) throws DopoSoccerException {
        currentMatch = MatchFactory.create(request);
        rewardsApplied = false;
        aiProfile = request.aiProfile() == null ? AIProfile.EXPERT : AIProfile.valueOf(request.aiProfile());
        loadProfile("player1", currentMatch.firstPlayer());
        loadProfile("player2", currentMatch.secondPlayer());
    }

    public void updateMatch(PlayerActions first, PlayerActions second, double deltaSeconds)
            throws DopoSoccerException {
        requireMatch();
        if (deltaSeconds < 0) throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME);
        boolean wasFinished=currentMatch.isFinished();
        PlayerActions effectiveSecond = currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE
                ? toActions(ai.decide(aiProfile, currentMatch.secondPlayer(), currentMatch.firstPlayer(), currentMatch.ball(), currentMatch.secondPlayer().goals(), currentMatch.firstPlayer().goals()))
                : second;
        currentMatch.update(toInput(first), toInput(effectiveSecond), deltaSeconds);
        if (!wasFinished && currentMatch.isFinished()) { awardMatchPoints(); saveProfiles(); }
    }

    private PlayerActions toActions(domain.model.game.PlayerInput input) {
        return new PlayerActions(input.left(),input.right(),input.jump(),input.kick(),input.power());
    }

    private void loadProfile(String slot, Player player) {
        PlayerProfile profile=profiles.load(slot);
        if(profile!=null) player.stats().restore(profile);
    }

    /** Persists both player progression profiles for the next session. */
    public void saveProfiles() {
        requireMatch();
        profiles.save("player1",currentMatch.firstPlayer().stats().profile());
        profiles.save("player2",currentMatch.secondPlayer().stats().profile());
    }

    /** Explicit profile load/save operations are available to menu/profile UI. */
    public PlayerProfile loadSavedProfile(String slot) {
        PlayerProfile profile = profiles.load(slot);
        if (profile == null) return new PlayerProfile(1, 1, 1, 1, 0, 500);
        return profile;
    }


    /** Compra una mejora en el perfil persistente, antes de iniciar un partido. */
    public PlayerProfile improveSavedProfile(String slot, String stat) {
        PlayerStats stats = new PlayerStats();
        stats.restore(loadSavedProfile(slot));
        if ("speed".equalsIgnoreCase(stat)) stats.improveSpeed();
        else if ("kick".equalsIgnoreCase(stat)) stats.improveKick();
        else if ("jump".equalsIgnoreCase(stat)) stats.improveJump();
        else if ("survival".equalsIgnoreCase(stat)) stats.improveSurvival();
        else throw new DopoSoccerException(DopoSoccerException.INVALID_STAT + " " + stat);
        PlayerProfile improved = stats.profile();
        profiles.save(slot, improved);
        return improved;
    }


    private domain.model.game.PlayerInput toInput(PlayerActions actions) {
        if (actions == null) throw new DopoSoccerException("Las acciones del jugador son obligatorias.");
        return new domain.model.game.PlayerInput(actions.left(), actions.right(), actions.jump(), actions.kick(), actions.power());
    }

    private void awardMatchPoints() {
        if (rewardsApplied) return;
        int winner = currentMatch.winner();
        if (winner == 1) {
            currentMatch.firstPlayer().stats().addPoints(100);
            currentMatch.secondPlayer().stats().addPoints(25);
        } else if (winner == 2) {
            currentMatch.secondPlayer().stats().addPoints(100);
            currentMatch.firstPlayer().stats().addPoints(25);
        } else {
            currentMatch.firstPlayer().stats().addPoints(25);
            currentMatch.secondPlayer().stats().addPoints(25);
        }
        rewardsApplied = true;
    }

    /** Guarda el estado completo del partido activo. */
    public void saveMatch() {
        saveMatch(matchSaves.filePath());
    }

    /** Guarda el partido en la ruta elegida por el usuario. */
    public void saveMatch(java.nio.file.Path file) {
        requireMatch();
        Properties data = new Properties();
        data.setProperty("playerMode", currentMatch.playerMode().name());
        data.setProperty("country1", currentMatch.firstPlayer().country().id());
        data.setProperty("country2", currentMatch.secondPlayer().country().id());
        data.setProperty("aiProfile", aiProfile.name());
        if (currentMatch instanceof GoalGame) {
            data.setProperty("matchType", "GOALS");
            data.setProperty("goalLimit", Integer.toString(((GoalGame) currentMatch).goalLimit()));
        } else if (currentMatch instanceof TimeGame) {
            TimeGame timeGame = (TimeGame) currentMatch;
            data.setProperty("matchType", "TIME");
            data.setProperty("duration", Integer.toString(timeGame.durationSeconds()));
            data.setProperty("remaining", Double.toString(timeGame.remainingExactSeconds()));
            data.setProperty("suddenDeath", Boolean.toString(timeGame.suddenDeath()));
        }
        data.setProperty("kickoffDelay", Double.toString(currentMatch.kickoffDelaySeconds()));
        data.setProperty("winner", Integer.toString(currentMatch.winner()));
        data.setProperty("finished", Boolean.toString(currentMatch.isFinished()));
        savePlayerState(data, "p1", currentMatch.firstPlayer());
        savePlayerState(data, "p2", currentMatch.secondPlayer());
        Ball ball = currentMatch.ball();
        data.setProperty("ball.x", Double.toString(ball.x()));
        data.setProperty("ball.y", Double.toString(ball.y()));
        data.setProperty("ball.vx", Double.toString(ball.velocityX()));
        data.setProperty("ball.vy", Double.toString(ball.velocityY()));
        if (ball.activePowerShot() != null) {
            data.setProperty("ball.powerOwner", ball.powerShotOwner() == currentMatch.firstPlayer() ? "1" : "2");
            data.setProperty("ball.powerTime", Double.toString(ball.powerShotTime()));
            data.setProperty("ball.powerApplied", Boolean.toString(ball.powerEffectApplied()));
        }
        new MatchSaveStore(file).save(data);
        saveProfiles();
    }

    private void savePlayerState(Properties data, String prefix, Player player) {
        PlayerProfile profile = player.stats().profile();
        data.setProperty(prefix + ".speed", Integer.toString(profile.speed()));
        data.setProperty(prefix + ".kick", Integer.toString(profile.kick()));
        data.setProperty(prefix + ".jump", Integer.toString(profile.jump()));
        data.setProperty(prefix + ".survival", Integer.toString(profile.survival()));
        data.setProperty(prefix + ".points", Integer.toString(profile.points()));
        data.setProperty(prefix + ".cost", Integer.toString(profile.nextUpgradeCost()));
        data.setProperty(prefix + ".goals", Integer.toString(player.goals()));
        data.setProperty(prefix + ".health", Double.toString(player.health()));
        data.setProperty(prefix + ".charge", Double.toString(player.powerCharge()));
        data.setProperty(prefix + ".power", player.power().name());
        data.setProperty(prefix + ".x", Double.toString(player.x()));
        data.setProperty(prefix + ".y", Double.toString(player.y()));
        data.setProperty(prefix + ".vx", Double.toString(player.velocityX()));
        data.setProperty(prefix + ".vy", Double.toString(player.velocityY()));
        data.setProperty(prefix + ".grounded", Boolean.toString(player.grounded()));
        data.setProperty(prefix + ".kickCooldown", Double.toString(player.kickCooldown()));
        data.setProperty(prefix + ".kicking", Double.toString(player.kickAnimationTimer()));
        data.setProperty(prefix + ".stun", Double.toString(player.stunTimer()));
        data.setProperty(prefix + ".facing", Integer.toString(player.facingDirection()));
        data.setProperty(prefix + ".effect", player.statusEffect().name());
        data.setProperty(prefix + ".effectTime", Double.toString(player.statusEffectTimer()));
    }

    /** Abre la partida guardada y reconstruye sus entidades en el dominio. */
    public void loadMatch() {
        loadMatch(matchSaves.filePath());
    }

    /** Carga un partido desde un archivo seleccionado. */
    public void loadMatch(java.nio.file.Path file) {
        Properties data = new MatchSaveStore(file).load();
        MatchKind type = "TIME".equals(data.getProperty("matchType")) ? MatchKind.TIME : MatchKind.GOALS;
        int goals = integer(data, "goalLimit", 10);
        int duration = integer(data, "duration", 240);
        PlayerModeKind mode = PlayerModeKind.valueOf(data.getProperty("playerMode", "PLAYER_VS_PLAYER"));
        String profile = data.getProperty("aiProfile", "EXPERT");
        startMatch(new MatchRequest(mode, type, data.getProperty("country1"), data.getProperty("country2"),
                goals, duration, profile));
        restorePlayerState(data, "p1", currentMatch.firstPlayer());
        restorePlayerState(data, "p2", currentMatch.secondPlayer());
        currentMatch.restoreKickoffDelay(number(data, "kickoffDelay", 0));
        Ball ball = currentMatch.ball();
        ball.setPosition(number(data, "ball.x", ball.x()), number(data, "ball.y", ball.y()));
        ball.setVelocity(number(data, "ball.vx", 0), number(data, "ball.vy", 0));
        if (data.containsKey("ball.powerOwner")) {
            Player owner = "1".equals(data.getProperty("ball.powerOwner"))
                    ? currentMatch.firstPlayer() : currentMatch.secondPlayer();
            ball.restorePowerShot(owner.country(), owner, number(data, "ball.powerTime", 0),
                    Boolean.parseBoolean(data.getProperty("ball.powerApplied", "false")));
        }
        if (currentMatch instanceof TimeGame) {
            ((TimeGame) currentMatch).restoreClock(number(data, "remaining", duration),
                    Boolean.parseBoolean(data.getProperty("suddenDeath", "false")));
        }
        if (Boolean.parseBoolean(data.getProperty("finished", "false"))) {
            currentMatch.restoreLifecycle(integer(data, "winner", 0));
        }
        rewardsApplied = Boolean.parseBoolean(data.getProperty("finished", "false"));
        saveProfiles();
    }

    private void restorePlayerState(Properties data, String prefix, Player player) {
        PlayerProfile profile = new PlayerProfile(integer(data, prefix + ".speed", 1),
                integer(data, prefix + ".kick", 1), integer(data, prefix + ".jump", 1),
                integer(data, prefix + ".survival", 1), integer(data, prefix + ".points", 0),
                integer(data, prefix + ".cost", 500));
        player.restoreProfile(profile);
        player.restoreMatchState(integer(data, prefix + ".goals", 0),
                number(data, prefix + ".health", player.maxHealth()),
                number(data, prefix + ".charge", 0),
                PowerState.valueOf(data.getProperty(prefix + ".power", "NONE")),
                number(data, prefix + ".x", player.x()), number(data, prefix + ".y", player.y()),
                number(data, prefix + ".vx", 0), number(data, prefix + ".vy", 0),
                Boolean.parseBoolean(data.getProperty(prefix + ".grounded", "false")),
                number(data, prefix + ".kickCooldown", 0),
                number(data, prefix + ".kicking", 0),
                number(data, prefix + ".stun", 0), integer(data, prefix + ".facing", 1),
                PlayerStatusEffect.valueOf(data.getProperty(prefix + ".effect", "NONE")),
                number(data, prefix + ".effectTime", 0));
    }

    private int integer(Properties data, String key, int fallback) {
        try { return Integer.parseInt(data.getProperty(key, Integer.toString(fallback))); }
        catch (NumberFormatException exception) { return fallback; }
    }

    private double number(Properties data, String key, double fallback) {
        try { return Double.parseDouble(data.getProperty(key, Double.toString(fallback))); }
        catch (NumberFormatException exception) { return fallback; }
    }


    public MatchSnapshot snapshot() {
        requireMatch();
        Player first = currentMatch.firstPlayer();
        Player second = currentMatch.secondPlayer();
        Ball ball = currentMatch.ball();
        Power shot = ball.activePowerShot();
        return new MatchSnapshot(Game.WIDTH, Game.HEIGHT, Game.GROUND_Y,
                currentMatch.modeDescription(), currentMatch.isFinished(), currentMatch.winner(),
                playerSnapshot(first), playerSnapshot(second),
                currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE,
                ball.x(), ball.y(), Ball.RADIUS,
                shot == null ? null : shot.id(), shot == null ? null : shot.visualAsset(),
                shot == null ? null : shot.visualColor(), shot == null ? 0 : shot.durationSeconds(),
                shot == null ? 0 : shot.durationSeconds() - ball.powerShotTime());
    }

    private PlayerSnapshot playerSnapshot(Player player) {
        return new PlayerSnapshot(player.country().displayName(), player.country().color(),
                player.x(), player.y(), player.goals(), player.health(), player.maxHealth(),
                player.powerCharge(), player.stats().points(), player.facingDirection(), player.kicking(), player.stunned(),
                player.power().name(), player.statusEffect().name());
    }

    public MatchStatus matchStatus() {
        if (currentMatch == null) return MatchStatus.NO_MATCH;
        return currentMatch.isFinished() ? MatchStatus.FINISHED : MatchStatus.ACTIVE;
    }


    public int winner() {
        requireMatch();
        return currentMatch.winner();
    }

    /** Finaliza la sesión en la fachada y libera la referencia al partido. */
    public void reset() { currentMatch = null; }

    private void requireMatch() {
        if (currentMatch == null) throw new DopoSoccerException("No hay un partido activo.");
    }
}
