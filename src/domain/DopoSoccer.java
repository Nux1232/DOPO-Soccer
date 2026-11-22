package domain;

import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.entity.Player;
import domain.model.entity.Ball;
import domain.model.entity.PowerShot;
import domain.factory.MatchFactory;

/**
 * Fachada pública del dominio DOPO-SOCCER.
 * La presentación se comunica exclusivamente con esta clase. Valida solicitudes,
 * administra el ciclo de vida del partido activo y delega las reglas en el dominio.
 */
public final class DopoSoccer {
    private Game currentMatch;


    public enum MatchKind { GOALS, TIME }
    public enum PlayerModeKind { PLAYER_VS_PLAYER, PLAYER_VS_MACHINE }
    public enum CountrySelection { SOUTH_KOREA, CAMEROON, NIGERIA, USA }
    public enum MatchStatus { NO_MATCH, ACTIVE, FINISHED }

    /** Contrato público para iniciar un partido, sin exponer entidades. */
    public record MatchRequest(PlayerModeKind playerMode, MatchKind matchType,
            CountrySelection firstCountry, CountrySelection secondCountry,
            int goalLimit, int durationSeconds) { }

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
    }

    public void updateMatch(PlayerActions first, PlayerActions second, double deltaSeconds)
            throws DopoSoccerException {
        requireMatch();
        if (deltaSeconds < 0) throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME);
        currentMatch.update(toInput(first), toInput(second), deltaSeconds);
    }

    private domain.model.game.PlayerInput toInput(PlayerActions actions) {
        if (actions == null) throw new IllegalArgumentException("Las acciones del jugador son obligatorias.");
        return new domain.model.game.PlayerInput(actions.left(), actions.right(), actions.jump(), actions.kick(), actions.power());
    }

    public MatchSnapshot snapshot() {
        requireMatch();
        Player first = currentMatch.firstPlayer();
        Player second = currentMatch.secondPlayer();
        Ball ball = currentMatch.ball();
        PowerShot shot = ball.activePowerShot();
        return new MatchSnapshot(Game.WIDTH, Game.HEIGHT, Game.GROUND_Y,
                currentMatch.modeDescription(), currentMatch.isFinished(), currentMatch.winner(),
                playerSnapshot(first), playerSnapshot(second), ball.x(), ball.y(), Ball.RADIUS,
                shot == null ? null : shot.name(), shot == null ? null : shot.visualAsset(),
                shot == null ? null : shot.visualColor(), shot == null ? 0 : shot.durationSeconds(),
                shot == null ? 0 : shot.durationSeconds() - ball.powerShotTime());
    }

    private PlayerSnapshot playerSnapshot(Player player) {
        return new PlayerSnapshot(player.country().displayName(), player.country().color(),
                player.x(), player.y(), player.goals(), player.health(), player.maxHealth(),
                player.powerCharge(), player.facingDirection(), player.kicking(), player.stunned(),
                player.power().name(), player.statusEffect().name());
    }

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
        if (currentMatch == null) throw new IllegalStateException("No hay un partido activo.");
    }
}
