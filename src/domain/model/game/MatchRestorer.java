package domain.model.game;

import domain.model.exception.DopoSoccerException;

/** Puerto para reconstruir el tipo concreto de partido a partir de su configuración guardada. */
@FunctionalInterface
public interface MatchRestorer {
    /** Crea el partido base correspondiente a los datos persistidos. */
    Game restore(MatchRestoreRequest request);

    /** Valida una configuración antes de persistirla; implementaciones pueden validar según su catálogo. */
    default void validateConfiguration(MatchConfiguration configuration) { }

    /** Adapta la configuración de valores de texto utilizada por el formato de guardado 5. */
    default MatchConfiguration migrateLegacyConfiguration(MatchConfiguration configuration) {
        if (configuration == null) {
            throw new DopoSoccerException("La configuración heredada es obligatoria.");
        }
        return configuration.migrateLegacyValues();
    }
}
