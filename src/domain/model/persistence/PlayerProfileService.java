package domain.model.persistence;

import domain.model.entity.Player;
import domain.model.entity.PlayerStats;
import domain.model.exception.DopoSoccerException;

/** Coordinates profile reads, upgrades, and persistence independently of matches. */
public final class PlayerProfileService {
    private final ProfileStore store;

    public PlayerProfileService() {
        this(new ProfileStore());
    }

    public PlayerProfileService(ProfileStore store) {
        if (store == null) throw new DopoSoccerException("El almacén de perfiles es obligatorio.");
        this.store = store;
    }

    public PlayerProfile load(String slot) {
        PlayerProfile profile = store.load(slot);
        return profile == null ? defaultProfile() : profile;
    }

    public void loadInto(String slot, Player player) {
        player.stats().restore(load(slot));
    }

    public void saveMatchProfiles(Player first, Player second) {
        store.save("player1", first.stats().profile());
        store.save("player2", second.stats().profile());
    }

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
