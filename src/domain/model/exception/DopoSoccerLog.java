package domain.model.exception;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Registro de errores (log) para los programadores de DOPOSoccer.
 */
public final class DopoSoccerLog {

    private static final String LOG_NAME = "DopoSoccer";
    private static final Logger LOGGER = Logger.getLogger(LOG_NAME);
    private static boolean initialized = false;

    private DopoSoccerLog() {
    }

    private static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            LOGGER.setUseParentHandlers(false);
            FileHandler fileHandler = new FileHandler("DopoSoccer.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            initialized = true;
        } catch (Exception ignored) {
            // Si el sistema de archivos bloquea el log en pruebas, no interrumpe la ejecución
        }
    }

    /**
     * Registra una excepción en el archivo DopoSoccer.log.
     *
     * @param exception excepción ocurrida
     */
    public static void record(Exception exception) {
        init();
        LOGGER.log(Level.WARNING, exception.getMessage(), exception);
    }
}