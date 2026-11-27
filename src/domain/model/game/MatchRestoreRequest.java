package domain.model.game;

import domain.model.ai.AIProfile;

/**
 * Datos estables necesarios para reconstruir una modalidad antes de restaurar su estado de juego.
 *
 * @param playerMode modo de participación de la partida
 * @param firstCountryId identificador del país del primer jugador
 * @param secondCountryId identificador del país del segundo jugador
 * @param aiProfile perfil de IA guardado para el partido
 * @param configuration configuración y estado serializable específicos de la modalidad
 */
public record MatchRestoreRequest(
        PlayerMode playerMode,
        String firstCountryId,
        String secondCountryId,
        AIProfile aiProfile,
        MatchConfiguration configuration) { }
