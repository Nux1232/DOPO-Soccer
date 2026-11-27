package domain.model.game;

import domain.model.exception.DopoSoccerException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Identificador extensible de una modalidad de partido.
 * Las modalidades integradas se ofrecen como constantes; otras pueden registrarse
 * en {@code MatchFactory} sin modificar este tipo.
 */
public final class MatchType implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final MatchType GOALS = new MatchType("goals");
    public static final MatchType TIME = new MatchType("time");

    private final String id;

    private MatchType(String id) {
        this.id = id;
    }

    /**
     * Crea o recupera el identificador de una modalidad.
     *
     * @param id identificador estable, por ejemplo {@code "sudden-death"}
     * @return tipo normalizado en minúsculas
     * @throws DopoSoccerException si el identificador es nulo o está en blanco
     */
    public static MatchType of(String id) {
        if (id == null || id.isBlank()) {
            throw new DopoSoccerException("El identificador de modalidad es obligatorio.");
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "goals" -> GOALS;
            case "time" -> TIME;
            default -> new MatchType(normalized);
        };
    }

    /** @return identificador estable para solicitudes y persistencia */
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof MatchType type && id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
