
package domain.model.game;
import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
public class GameMatchConfig {

    private PlayerMode gameMode;
    private MatchType matchType = MatchType.GOALS;

    private int goalLimit = 10;
    private int durationSeconds = 240;

    private Country player1Character = Country.values()[0];
    private Country player2Character = Country.values()[1];

    public PlayerMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(PlayerMode gameMode) {
        this.gameMode = gameMode;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        if (matchType == null) {
            throw new IllegalArgumentException("El modo de partido es obligatorio.");
        }
        this.matchType = matchType;
    }

    public int getGoalLimit() {
        return goalLimit;
    }

    public void setGoalLimit(int goalLimit) throws DopoSoccerException {
        if (goalLimit <= 0) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_GOAL_LIMIT);
        }
        this.goalLimit = goalLimit;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) throws DopoSoccerException {
        if (durationSeconds <= 0) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_MATCH_DURATION);
        }
        this.durationSeconds = durationSeconds;
    }

    public Country getPlayer1Character() {
        return player1Character;
    }

    public void setPlayer1Character(Country player1Character) {
        if (player1Character == null) {
            throw new IllegalArgumentException("El personaje del jugador 1 es obligatorio.");
        }
        this.player1Character = player1Character;
    }

    public Country getPlayer2Character() {
        return player2Character;
    }

    public void setPlayer2Character(Country player2Character) {
        if (player2Character == null) {
            throw new IllegalArgumentException("El personaje del jugador 2 es obligatorio.");
        }
        this.player2Character = player2Character;
    }
}
