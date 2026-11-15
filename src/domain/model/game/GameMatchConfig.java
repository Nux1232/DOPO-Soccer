package domain.model.game;

import domain.model.entity.Country;

/**
 * Configuración compartida para armar un partido antes de iniciarlo.
 */
public class GameMatchConfig {

    private PlayerMode gameMode;
    private Country player1Character = Country.values()[0];
    private Country player2Character = Country.values()[1];

    public PlayerMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(PlayerMode gameMode) {
        this.gameMode = gameMode;
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