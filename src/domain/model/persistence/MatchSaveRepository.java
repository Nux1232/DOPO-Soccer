package domain.model.persistence;

import java.nio.file.Path;
import java.util.Optional;

/** Contrato de almacenamiento para instantáneas de partidos serializables. */
public interface MatchSaveRepository {
    /** Persiste una instantánea completa del partido. */
    void save(MatchSaveData data);

    /** Recupera y valida una instantánea previamente persistida. */
    MatchSaveData load();

    /** Indica si existe una partida guardada. */
    boolean exists();

    /**
     * Expone una ruta cuando el repositorio está basado en archivos.
     * Los repositorios que no usan archivos pueden conservar el valor predeterminado.
     */
    default Optional<Path> storagePath() {
        return Optional.empty();
    }
}