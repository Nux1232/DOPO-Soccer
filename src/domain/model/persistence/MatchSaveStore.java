package domain.model.persistence;

import domain.model.exception.DopoSoccerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Guarda y abre el estado de una partida en un archivo sencillo. */
public final class MatchSaveStore {
    private final Path file;

    public MatchSaveStore() {
        this(Path.of(System.getProperty("user.home"), ".dopo-soccer", "saved-match.properties"));
    }

    public MatchSaveStore(Path file) {
        if (file == null) throw new DopoSoccerException("La ruta de guardado es obligatoria.");
        this.file = file;
    }

    public void save(Properties data) {
        data.setProperty("saveVersion", "1");
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                data.store(output, "DOPO Soccer saved match");
            }
        } catch (IOException exception) {
            throw new DopoSoccerException(DopoSoccerException.PROFILE_SAVE_ERROR, exception);
        }
    }

    public Properties load() {
        if (!Files.exists(file)) throw new DopoSoccerException("No hay una partida guardada.");
        Properties data = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            data.load(input);
        } catch (IOException exception) {
            throw new DopoSoccerException(DopoSoccerException.PROFILE_READ_ERROR, exception);
        }
        if (!"1".equals(data.getProperty("saveVersion"))) {
            throw new DopoSoccerException("El archivo de partida tiene una versión incompatible.");
        }
        return data;
    }

    public boolean exists() {
        return Files.exists(file);
    }

    public Path filePath() {
        return file;
    }
}
