package domain.model.persistence;

import domain.model.exception.DopoSoccerException;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Reads and writes versioned serializable match snapshots. */
public final class MatchSaveStore {
    private final Path file;

    public MatchSaveStore() {
        this(Path.of(System.getProperty("user.home"), ".dopo-soccer", "saved-match.ser"));
    }

    public MatchSaveStore(Path file) {
        if (file == null) throw new DopoSoccerException("La ruta de guardado es obligatoria.");
        this.file = file.toAbsolutePath();
    }

    public void save(MatchSaveData data) {
        if (data == null) throw new DopoSoccerException("Los datos del partido son obligatorios.");
        Path parent = file.getParent();
        Path temporaryFile = null;
        try {
            if (parent != null) Files.createDirectories(parent);
            temporaryFile = Files.createTempFile(parent, "dopo-match-", ".tmp");
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporaryFile))) {
                output.writeObject(data);
            }
            try {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new DopoSoccerException(DopoSoccerException.PROFILE_SAVE_ERROR, exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException exception) {
                    throw new DopoSoccerException(DopoSoccerException.PROFILE_SAVE_ERROR, exception);
                }
            }
        }
    }

    public MatchSaveData load() {
        if (!Files.isRegularFile(file)) throw new DopoSoccerException("No hay una partida guardada.");
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            input.setObjectInputFilter(MatchSaveStore::filterSerializedClass);
            Object value = input.readObject();
            if (!(value instanceof MatchSaveData data)) {
                throw new InvalidClassException("El archivo no contiene un estado de partido válido.");
            }
            if (data.version() != MatchSaveData.CURRENT_VERSION) {
                throw new InvalidClassException("El archivo de partida tiene una versión incompatible.");
            }
            return data;
        } catch (IOException | ClassNotFoundException exception) {
            throw new DopoSoccerException(DopoSoccerException.PROFILE_READ_ERROR, exception);
        }
    }

    private static ObjectInputFilter.Status filterSerializedClass(ObjectInputFilter.FilterInfo info) {
        Class<?> type = info.serialClass();
        if (type == null) return ObjectInputFilter.Status.UNDECIDED;
        if (type == MatchSaveData.class
                || type == MatchSaveData.PlayerState.class
                || type == MatchSaveData.BallState.class
                || type == PlayerProfile.class
                || type == String.class
                || type == Enum.class
                || type.isEnum()) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    }

    public boolean exists() {
        return Files.isRegularFile(file);
    }

    public Path filePath() {
        return file;
    }
}
