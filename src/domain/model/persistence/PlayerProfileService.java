package domain.model.persistence;

import domain.model.entity.Player;
import domain.model.entity.PlayerStats;
import domain.model.exception.DopoSoccerException;

/** Coordina la lectura, mejora y persistencia de perfiles independientemente de los partidos. */
public final class PlayerProfileService {
    private final PlayerProfileRepository store;

    /** Crea el servicio utilizando el almacén de perfiles por defecto. */
    public PlayerProfileService() {
        this(new ProfileStore());
    }

    /**
     * Crea el servicio con un almacén específico.
     *
     * @param store almacén de perfiles
     * @throws DopoSoccerException si el almacén es nulo
     */
    public PlayerProfileService(PlayerProfileRepository store) {
        if (store == null) throw new DopoSoccerException("El almacén de perfiles es obligatorio.");
        this.store = store;
    }

    /**
     * Carga el perfil guardado de un slot o devuelve los valores iniciales si no existe.
     *
     * @param slot identificador del perfil (por ejemplo {@code "player1"})
     * @return perfil cargado o por defecto
     */
    public PlayerProfile load(String slot) {
        PlayerProfile profile = store.load(slot);
        return profile == null ? defaultProfile() : profile;
    }

    /**
     * Carga el perfil guardado de un slot directamente sobre un jugador.
     *
     * @param slot identificador del perfil
     * @param player jugador a inicializar
     */
    public void loadInto(String slot, Player player) {
        player.restoreProfile(load(slot));
    }

    /**
     * Guarda el perfil de un jugador específico en su slot correspondiente.
     *
     * @param slot identificador del perfil
     * @param player jugador cuyo progreso se desea persistir
     */
    public void saveProfile(String slot, Player player) {
        store.save(slot, player.stats().profile());
    }

    /**
     * Guarda los perfiles de ambos jugadores humanos tras un partido.
     *
     * @param first primer jugador
     * @param second segundo jugador
     */
    public void saveMatchProfiles(Player first, Player second) {
        store.save("player1", first.stats().profile());
        store.save("player2", second.stats().profile());
    }

    /**
     * Aplica una mejora estadística en el perfil indicado y la guarda en disco.
     *
     * @param slot identificador del perfil
     * @param stat nombre de la estadística ({@code speed}, {@code kick}, {@code jump}, {@code survival})
     * @return perfil actualizado tras la compra
     * @throws DopoSoccerException si la estadística no existe, ya está al máximo o faltan puntos
     */
    public PlayerProfile improve(String slot, String stat) {
        PlayerStats stats = new PlayerStats();
        stats.restore(load(slot));
        if ("speed".equalsIgnoreCase(stat)) stats.improveSpeed();
        else if ("kick".equalsIgnoreCase(stat)) stats.improveKick();
        else if ("jump".equalsIgnoreCase(stat)) stats.improveJump();
        else if ("survival".equalsIgnoreCase(stat)) stats.improveSurvival();
        else throw new DopoSoccerException(DopoSoccerException.INVALID_STAT + " " + stat);
        PlayerProfile improved = stats.profile();
        store.save(slot, improved);
        return improved;
    }

    private PlayerProfile defaultProfile() {
        return new PlayerProfile(1, 1, 1, 1, 0, 500);
    }
}
