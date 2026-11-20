package domain.model.exception;

/**
 * Excepción específica del dominio de DOPOSoccer para reportar errores
 * de configuración o reglas de negocio con mensajes claros.
 */
public class DopoSoccerException extends Exception {

    public static final String NULL_CONFIG = "La modalidad y los personajes son obligatorios.";
    public static final String NEGATIVE_TIME = "El tiempo no puede ser negativo.";
    public static final String NEGATIVE_DAMAGE = "El daño no puede ser negativo.";
    public static final String NEGATIVE_POINTS = "La cantidad de puntos no puede ser negativa.";
    public static final String INSUFFICIENT_POINTS = "No hay puntos suficientes para mejorar estadísticas.";
    public static final String STAT_AT_MAX = "La estadística ya está en su máximo (10).";
    public static final String MODE_UNDER_CONSTRUCTION = "La modalidad/Opcion está en construcción, estará disponible en una próxima versión de DOPO-SOCCER.";
    public static final String INVALID_GOAL_LIMIT = "El límite de goles debe ser mayor que cero.";
    public static final String INVALID_MATCH_DURATION = "La duración del partido debe ser mayor que cero.";
    public static final String EMPTY_MATCH_VALUE = "Debes ingresar un valor para configurar el partido.";
    public static final String INVALID_MATCH_VALUE = "El valor ingresado para el partido no es válido.";
    public DopoSoccerException(String message) {
        super(message);
        DopoSoccerLog.record(this);
    }
}