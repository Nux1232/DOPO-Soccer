package domain.persistence;

import domain.model.game.Game;
import domain.model.game.GameMatchConfig;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Guarda y recupera el estado de un partido como un archivo serializado.
 */
public final class GamePersistence {

    private static final String SAVE_FILE_NAME = "saved-match.ser";

    private final Path savePath;

    public GamePersistence() {
        this(Path.of(System.getProperty("user.home"), ".dopo-soccer", SAVE_FILE_NAME));
    }

    public GamePersistence(Path savePath) {
        this.savePath = Objects.requireNonNull(savePath, "La ruta de guardado es obligatoria.")
                .toAbsolutePath();
    }

    public boolean hasSavedMatch() {
        return Files.isRegularFile(savePath);
    }

    public void save(Game game, GameMatchConfig matchConfig) throws IOException {
        Objects.requireNonNull(game, "El partido es obligatorio.");
        Objects.requireNonNull(matchConfig, "La configuración del partido es obligatoria.");

        Path parent = savePath.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "saved-match-", ".tmp");
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporaryFile))) {
                output.writeObject(new SavedMatch(game, matchConfig));
            }
            try {
                Files.move(temporaryFile, savePath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, savePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public SavedMatch load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(savePath))) {
            input.setObjectInputFilter(GamePersistence::filterSerializedClass);
            Object storedObject = input.readObject();
            if (storedObject instanceof SavedMatch savedMatch) {
                return savedMatch;
            }
            throw new InvalidObjectException("El archivo no contiene un partido guardado válido.");
        }
    }

    private static ObjectInputFilter.Status filterSerializedClass(ObjectInputFilter.FilterInfo info) {
        Class<?> type = info.serialClass();
        if (type == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        String className = type.getName();
        if (className.equals(SavedMatch.class.getName())
                || className.equals(String.class.getName())
                || className.equals(Enum.class.getName())
                || className.startsWith("domain.model.entity.")
                || className.startsWith("domain.model.game.")) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    }

    public static final class SavedMatch implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final Game game;
        private final GameMatchConfig matchConfig;

        private SavedMatch(Game game, GameMatchConfig matchConfig) {
            this.game = game;
            this.matchConfig = matchConfig;
        }

        public Game game() {
            return game;
        }

        public GameMatchConfig matchConfig() {
            return matchConfig;
        }
    }
}
