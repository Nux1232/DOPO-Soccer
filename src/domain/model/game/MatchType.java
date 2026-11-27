package domain.model.game;

/** Modalidad de victoria configurada para un partido. */
public enum MatchType {
    /** El partido finaliza al alcanzar un límite de goles. */
    GOALS,
    /** El partido finaliza al agotarse el tiempo o en gol de oro tras empate. */
    TIME
}
