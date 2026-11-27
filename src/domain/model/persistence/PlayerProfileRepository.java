package domain.model.persistence;

/** Contrato de almacenamiento para perfiles permanentes de jugadores. */
public interface PlayerProfileRepository {
    /** Carga un perfil por su identificador, o retorna {@code null} si no existe. */
    PlayerProfile load(String slot);

    /** Guarda el perfil asociado al identificador indicado. */
    void save(String slot, PlayerProfile profile);
}