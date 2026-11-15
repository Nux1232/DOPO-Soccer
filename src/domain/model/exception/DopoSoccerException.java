package domain.model.exception;

/**
 * Excepción específica del dominio de DOPOSoccer para reportar errores
 * de configuración o reglas de negocio con mensajes claros.
 */
public class DopoSoccerException extends Exception {

    public static final String NULL_CONFIG = "La modalidad y los personajes son obligatorios.";
    public static final String NULL_PLAYER1 = "El personaje del jugador 1 es obligatorio.";
    public static final String NULL_PLAYER2 = "El personaje del jugador 2 es obligatorio.";
    public static final String NEGATIVE_TIME = "El tiempo no puede ser negativo.";
    public static final String NEGATIVE_DAMAGE = "El daño no puede ser negativo.";
    public static final String NEGATIVE_POINTS = "La cantidad de puntos no puede ser negativa.";
    public static final String INSUFFICIENT_POINTS = "No hay puntos suficientes para mejorar estadísticas.";
    public static final String STAT_AT_MAX = "La estadística ya está en su máximo (10).";
    public static final String MODE_UNDER_CONSTRUCTION = "La modalidad/Opcion está en construcción, estará disponible en una próxima versión de DOPO-SOCCER.";
    public DopoSoccerException(String message) {
        super(message);
        DopoSoccerLog.record(this);
    }
}