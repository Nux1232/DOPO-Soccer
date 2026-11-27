package domain.model.game;

import domain.model.ai.AIProfile;

/** Datos estables necesarios para reconstruir una modalidad antes de restaurar su estado de juego. */
public record MatchRestoreRequest(
        PlayerMode playerMode,
        String firstCountryId,
        String secondCountryId,
        AIProfile aiProfile,
        MatchConfiguration configuration) { }
