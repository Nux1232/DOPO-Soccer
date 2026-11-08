package domain.model.game;

import domain.model.entity.Country;

/**
 * Guarda las opciones que el usuario elige antes de empezar un partido:
 * el modo de juego y el personaje de cada jugador.
 *
 * <p>Esta clase solo almacena las elecciones; con ellas se crea después
 * el partido. Los personajes tienen valores por defecto
 * y nunca pueden ser null.
 */
public class GameMatchConfig {

    /** Modo de juego elegido; es null hasta que se elija uno. */
    private GameMode gameMode;

    /** Personaje del jugador 1. Por defecto, México. */
    private Country player1Character = Country.MEXICO;

    /** Personaje del jugador 2. Por defecto, Argentina. */
    private Country player2Character = Country.ARGENTINA;

    /**
     * Devuelve el modo de juego elegido.
     *
     * @return el modo de juego, o null si todavía no se ha elegido
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Establece el modo de juego.
     *
     * @param gameMode el modo de juego elegido
     */
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    /**
     * Devuelve el personaje elegido por el jugador 1.
     *
     * @return el personaje del jugador 1
     */
    public Country getPlayer1Character() {
        return player1Character;
    }

    /**
     * Establece el personaje del jugador 1.
     *
     * @param character el personaje elegido; no puede ser null.
     * @throws IllegalArgumentException si el personaje es null.
     */
    public void setPlayer1Character(Country character) {
        if (character == null) {
            throw new IllegalArgumentException("El personaje del jugador 1 es obligatorio.");
        }
        player1Character = character;
    }

    /**
     * Devuelve el personaje elegido por el jugador 2.
     *
     * @return el personaje del jugador 2
     */
    public Country getPlayer2Character() {
        return player2Character;
    }

    /**
     * Establece el personaje del jugador 2.
     *
     * @param character el personaje elegido; no puede ser null.
     * @throws IllegalArgumentException si el personaje es null.
     */
    public void setPlayer2Character(Country character) {
        if (character == null) {
            throw new IllegalArgumentException("El personaje del jugador 2 es obligatorio.");
        }
        player2Character = character;
    }
}