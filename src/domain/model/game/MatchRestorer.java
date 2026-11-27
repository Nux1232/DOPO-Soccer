package domain.model.game;

/** Puerto para reconstruir el tipo concreto de partido a partir de su configuración guardada. */
@FunctionalInterface
public interface MatchRestorer {
    /** Crea el partido base correspondiente a los datos persistidos. */
    Game restore(MatchRestoreRequest request);

    /** Valida una configuración antes de persistirla; implementaciones pueden validar según su catálogo. */
    default void validateConfiguration(MatchConfiguration configuration) { }
}
