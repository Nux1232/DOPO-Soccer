package presentation.model;

/**
 * Instantánea inmutable de un cuadro del partido perteneciente exclusivamente a la capa de presentación.
 *
 * @param width ancho lógico de la cancha
 * @param height alto lógico de la cancha
 * @param groundY coordenada vertical del suelo
 * @param goalTop coordenada superior de los arcos
 * @param goalDepth profundidad horizontal de los arcos
 * @param modeDescription descripción textual del estado del modo (goles o tiempo)
 * @param finished indica si el partido ya finalizó
 * @param winner {@code 0} empate, {@code 1} primer jugador o {@code 2} segundo jugador
 * @param firstPlayer estado visual del jugador izquierdo
 * @param secondPlayer estado visual del jugador derecho
 * @param machineOpponent indica si el segundo jugador es controlado por la máquina
 * @param ballX posición horizontal del balón
 * @param ballY posición vertical del balón
 * @param ballRadius radio del balón
 * @param activePowerShot identificador del tiro especial activo, o {@code null}
 * @param powerShotAsset recurso gráfico del tiro especial activo
 * @param powerShotColor color hexadecimal del tiro especial activo
 * @param powerShotDuration duración total del tiro especial activo
 * @param powerShotElapsed tiempo transcurrido del tiro especial activo
 * @param powerShotRenderCopies indica si el balón debe dibujarse con copias ilusorias
 */
public record GameSnapshot(
        double width, double height, double groundY, double goalTop, double goalDepth,
        String modeDescription, boolean finished, int winner,
        PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
        double ballX, double ballY, double ballRadius,
        String activePowerShot, String powerShotAsset, String powerShotColor,
        double powerShotDuration, double powerShotElapsed, boolean powerShotRenderCopies) {

    /**
     * Instantánea inmutable del estado visual y HUD de un jugador.
     *
     * @param countryName nombre visible del país
     * @param jerseyColor color hexadecimal de la camiseta
     * @param flagAsset nombre del archivo de bandera
     * @param x posición horizontal en cancha
     * @param y posición vertical en cancha
     * @param goals goles anotados
     * @param health salud actual
     * @param maxHealth salud máxima
     * @param powerCharge porcentaje de carga de poder
     * @param points puntos acumulados del jugador
     * @param facingDirection dirección a la que mira ({@code 1} derecha, {@code -1} izquierda)
     * @param kicking indica si está ejecutando una patada
     * @param stunned indica si está aturdido
     * @param auraActive indica si tiene el aura de poder cargada
     * @param statusEffect nombre del efecto de estado activo
     */
    public record PlayerSnapshot(String countryName, String jerseyColor, String flagAsset,
            double x, double y, int goals, double health, double maxHealth,
            double powerCharge, int points, int facingDirection, boolean kicking,
            boolean stunned, boolean auraActive, String statusEffect) { }
}

