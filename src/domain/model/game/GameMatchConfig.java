package domain.model.game;

import domain.model.entity.Country;

public class GameMatchConfig {
    private GameMode gameMode;
    private Country player1Character = Country.MEXICO;
    private Country player2Character = Country.ARGENTINA;

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Country getPlayer1Character() {
        return player1Character;
    }

    public void setPlayer1Character(Country character) {
        if (character == null) {
            throw new IllegalArgumentException("El personaje del jugador 1 es obligatorio.");
        }
        player1Character = character;
    }

    public Country getPlayer2Character() {
        return player2Character;
    }

    public void setPlayer2Character(Country character) {
        if (character == null) {
            throw new IllegalArgumentException("El personaje del jugador 2 es obligatorio.");
        }
        player2Character = character;
    }
}
