package domain.model.exception;

/** Excepción común para errores de reglas y configuración del dominio. */
public class DopoSoccerException extends RuntimeException {
    public static final String NULL_CONFIG = "La modalidad y los personajes son obligatorios.";
    public static final String NEGATIVE_TIME = "El tiempo no puede ser negativo.";
    public static final String NEGATIVE_DAMAGE = "El daño no puede ser negativo.";
    public static final String NEGATIVE_POINTS = "La cantidad de puntos no puede ser negativa.";
    public static final String INSUFFICIENT_POINTS = "No hay puntos suficientes para mejorar estadísticas.";
    public static final String STAT_AT_MAX = "La estadística ya está en su máximo (10).";
    public static final String INVALID_GOAL_LIMIT = "El límite de goles debe ser mayor que cero.";
    public static final String INVALID_MATCH_DURATION = "La duración del partido debe ser mayor que cero.";
    public static final String INVALID_PROFILE = "El perfil de jugador no es válido.";
    public static final String INVALID_PLAYER = "El jugador debe ser 1 o 2.";
    public static final String INVALID_STAT = "La estadística solicitada no existe.";
    public static final String NO_ACTIVE_MATCH = "No hay un partido activo.";
    public static final String INVALID_COUNTRY = "El país solicitado no está registrado.";
    public static final String INVALID_COUNTRY_PROVIDER = "El proveedor de país no es válido.";
    public static final String INVALID_PROFILE_SLOT = "El nombre del perfil no es válido.";
    public static final String PROFILE_READ_ERROR = "No se pudo leer el perfil guardado.";
    public static final String PROFILE_SAVE_ERROR = "No se pudo guardar el perfil.";
    public static final String INVALID_PHYSICS_CONFIG = "La configuración física no es válida.";
    public static final String INVALID_DEPENDENCIES = "Las dependencias del motor son obligatorias.";

    public DopoSoccerException(String message) {
        super(message);
        DopoSoccerLog.record(this);
    }

    public DopoSoccerException(String message, Throwable cause) {
        super(message, cause);
        DopoSoccerLog.record(this);
    }
}
