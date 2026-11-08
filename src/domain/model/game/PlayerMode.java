package domain.model.game;

/**
 * Modalidad del partido según quién controla a cada jugador.
 *
 * Indica si el rival es otra persona o es la máquina. No define el
 * tipo de partido (por tiempo o por goles) ni el perfil de la máquina.
 **/
public enum PlayerMode {

    /** Jugador contra jugador: dos personas en el mismo teclado. */
    PLAYER_VS_PLAYER,

    /** Jugador contra máquina: el segundo jugador lo controla la computadora. */
    PLAYER_VS_MACHINE
}