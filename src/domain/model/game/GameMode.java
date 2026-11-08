package domain.model.game;

/**
 * Modalidad del partido según quién controla a cada jugador.
 *
 * Solo indica si el rival es humano o es la máquina; no define el
 * tipo de partido (por tiempo o por goles) ni el perfil de la máquina.
 */
public enum GameMode {

    /** Jugador contra jugador: dos personas en el mismo teclado. */
    PvP,

    /** Jugador contra máquina: el segundo jugador lo controla la computadora. */
    PvIA
}