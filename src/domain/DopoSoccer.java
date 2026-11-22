package domain;

import domain.factory.CountryFactory;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.entity.Player;
import domain.model.entity.Ball;
import domain.model.entity.Power;
import domain.factory.MatchFactory;
import domain.model.ai.FootballAI;
import domain.model.persistence.ProfileStore;
import domain.model.persistence.PlayerProfile;

import java.util.List;

/**
 * Fachada pública del dominio DOPO-SOCCER.
 * La presentación se comunica exclusivamente con esta clase. Valida solicitudes,
 * administra el ciclo de vida del partido activo y delega las reglas en el dominio.
 */
public final class DopoSoccer {
    private Game currentMatch;
    private final FootballAI ai = new FootballAI();
    private final ProfileStore profiles = new ProfileStore();


    public enum MatchKind { GOALS, TIME }
    public enum PlayerModeKind { PLAYER_VS_PLAYER, PLAYER_VS_MACHINE }
    public enum CountrySelection { SOUTH_KOREA, CAMEROON, NIGERIA, USA }
    public enum MatchStatus { NO_MATCH, ACTIVE, FINISHED }

    /** Contrato público para iniciar un partido, sin exponer entidades. */
    public record MatchRequest(PlayerModeKind playerMode, MatchKind matchType,
            String firstCountry, String secondCountry, int goalLimit, int durationSeconds) {
        /** Compatibility constructor for the original built-in-country enum API. */
        public MatchRequest(PlayerModeKind mode, MatchKind type, CountrySelection first,
                CountrySelection second, int goals, int seconds) {
            this(mode,type,first==null?null:first.name(),second==null?null:second.name(),goals,seconds);
        }
    }

    /** Acciones abstractas independientes del teclado o dispositivo. */
    public record PlayerActions(boolean left, boolean right, boolean jump, boolean kick, boolean power) { }

    /** Estado de jugador de solo lectura para construir una representación visual. */
    public record PlayerSnapshot(String countryName, String jerseyColor, double x, double y,
            int goals, double health, double maxHealth, double powerCharge,
            int facingDirection, boolean kicking, boolean stunned, String power,
            String statusEffect) { }

    /** Estado de partido de solo lectura. No expone entidades modificables. */
    public record MatchSnapshot(double width, double height, double groundY,
            String modeDescription, boolean finished, int winner,
            PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer,
            double ballX, double ballY, double ballRadius,
            String activePowerShot, String powerShotAsset, String powerShotColor,
            double powerShotDuration, double powerShotElapsed) { }

    public void startMatch(MatchRequest request) throws DopoSoccerException {
        currentMatch = MatchFactory.create(request);
        loadProfile("player1", currentMatch.firstPlayer());
        loadProfile("player2", currentMatch.secondPlayer());
    }

    public void updateMatch(PlayerActions first, PlayerActions second, double deltaSeconds)
            throws DopoSoccerException {
        requireMatch();
        if (deltaSeconds < 0) throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME);
        boolean wasFinished=currentMatch.isFinished();
        PlayerActions effectiveSecond = currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE
                ? toActions(ai.decide(currentMatch.secondPlayer(), currentMatch.firstPlayer(), currentMatch.ball()))
                : second;
        currentMatch.update(toInput(first), toInput(effectiveSecond), deltaSeconds);
        if (!wasFinished && currentMatch.isFinished()) saveProfiles();
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
    public PlayerProfile loadSavedProfile(String slot) { return profiles.load(slot); }
    public void saveProfile(String slot, PlayerProfile profile) { profiles.save(slot,profile); }

    public void improvePlayerStat(int playerNumber, String stat) {
        requireMatch();
        Player player=playerNumber==1?currentMatch.firstPlayer():playerNumber==2?currentMatch.secondPlayer():null;
        if(player==null)throw new DopoSoccerException("Jugador debe ser 1 o 2");
        String normalizedStat = stat.toLowerCase(java.util.Locale.ROOT);
        if (normalizedStat.equals("speed")) {
            player.stats().improveSpeed();
        } else if (normalizedStat.equals("kick")) {
            player.stats().improveKick();
        } else if (normalizedStat.equals("jump")) {
            player.stats().improveJump();
        } else if (normalizedStat.equals("survival")) {
            player.stats().improveSurvival();
        } else {
            throw new DopoSoccerException(DopoSoccerException.INVALID_STAT + " " + stat);
        }
        saveProfiles();
    }

    private domain.model.game.PlayerInput toInput(PlayerActions actions) {
        if (actions == null) throw new DopoSoccerException("Las acciones del jugador son obligatorias.");
        return new domain.model.game.PlayerInput(actions.left(), actions.right(), actions.jump(), actions.kick(), actions.power());
    }

    public MatchSnapshot snapshot() {
        requireMatch();
        Player first = currentMatch.firstPlayer();
        Player second = currentMatch.secondPlayer();
        Ball ball = currentMatch.ball();
        Power shot = ball.activePowerShot();
        return new MatchSnapshot(Game.WIDTH, Game.HEIGHT, Game.GROUND_Y,
                currentMatch.modeDescription(), currentMatch.isFinished(), currentMatch.winner(),
                playerSnapshot(first), playerSnapshot(second), ball.x(), ball.y(), Ball.RADIUS,
                shot == null ? null : shot.id(), shot == null ? null : shot.visualAsset(),
                shot == null ? null : shot.visualColor(), shot == null ? 0 : shot.durationSeconds(),
                shot == null ? 0 : shot.durationSeconds() - ball.powerShotTime());
    }

    private PlayerSnapshot playerSnapshot(Player player) {
        return new PlayerSnapshot(player.country().displayName(), player.country().color(),
                player.x(), player.y(), player.goals(), player.health(), player.maxHealth(),
                player.powerCharge(), player.facingDirection(), player.kicking(), player.stunned(),
                player.power().name(), player.statusEffect().name());
    }

    public java.util.List<String> availableCountries() { return List.of(CountryFactory.availableCountryIds()); }

    public MatchStatus matchStatus() {
        if (currentMatch == null) return MatchStatus.NO_MATCH;
        return currentMatch.isFinished() ? MatchStatus.FINISHED : MatchStatus.ACTIVE;
    }

    public boolean isMatchFinished() { return matchStatus() == MatchStatus.FINISHED; }
    public boolean hasActiveMatch() { return matchStatus() != MatchStatus.NO_MATCH; }

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
