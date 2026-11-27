package domain.model.game;

/**
 * Modalidades de juego disponibles según los participantes.
 */
public enum PlayerMode {
    /** Dos jugadores humanos en el mismo teclado. */
    PLAYER_VS_PLAYER,
    /** Un jugador humano enfrentando a la máquina. */
    PLAYER_VS_MACHINE
}