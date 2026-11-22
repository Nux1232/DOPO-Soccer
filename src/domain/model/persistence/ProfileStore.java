package domain.model.persistence;

import domain.model.exception.DopoSoccerException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Guarda perfiles sencillos en un archivo local del usuario. */
public final class ProfileStore {
    private final Path file;

    public ProfileStore() {
        this(Path.of(System.getProperty("user.home"), ".dopo-soccer", "profiles.properties"));
    }

    public ProfileStore(Path file) {
        if (file == null) {
            throw new DopoSoccerException("La ruta del archivo de perfiles es obligatoria.");
        }
        this.file = file;
    }

    public synchronized PlayerProfile load(String slot) {
        Properties properties = read();
        String prefix = validateSlot(slot) + ".";
        if (!properties.containsKey(prefix + "speed")) {
            return null;
        }
        return new PlayerProfile(
                readInt(properties, prefix + "speed", 1),
                readInt(properties, prefix + "kick", 1),
                readInt(properties, prefix + "jump", 1),
                readInt(properties, prefix + "survival", 1),
                readInt(properties, prefix + "points", 0),
                readInt(properties, prefix + "cost", 500));
    }

    public synchronized void save(String slot, PlayerProfile profile) {
        if (profile == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_PROFILE);
        }
        Properties properties = read();
        String prefix = validateSlot(slot) + ".";
        properties.setProperty(prefix + "speed", Integer.toString(profile.speed()));
        properties.setProperty(prefix + "kick", Integer.toString(profile.kick()));
        properties.setProperty(prefix + "jump", Integer.toString(profile.jump()));
        properties.setProperty(prefix + "survival", Integer.toString(profile.survival()));
        properties.setProperty(prefix + "points", Integer.toString(profile.points()));
        properties.setProperty(prefix + "cost", Integer.toString(profile.nextUpgradeCost()));

        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (OutputStream output = Files.newOutputStream(file)) {
                properties.store(output, "DOPO Soccer player profiles");
            }
        } catch (IOException exception) {
            throw new DopoSoccerException(DopoSoccerException.PROFILE_SAVE_ERROR, exception);
        }
    }

    private Properties read() {
        Properties properties = new Properties();
        if (!Files.exists(file)) {
            return properties;
        }
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        } catch (IOException exception) {
            throw new DopoSoccerException(DopoSoccerException.PROFILE_READ_ERROR, exception);
        }
        return properties;
    }

    private int readInt(Properties properties, String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String validateSlot(String slot) {
        if (slot == null || !slot.matches("[A-Za-z0-9_-]+")) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_PROFILE_SLOT);
        }
        return slot;
    }
}
