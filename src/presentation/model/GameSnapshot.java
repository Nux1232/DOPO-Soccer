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
 * @param ball estado del balón y su tiro especial activo
 */
public record GameSnapshot(
        double width, double height, double groundY, double goalTop, double goalDepth,
        String modeDescription, boolean finished, int winner,
        PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
        BallSnapshot ball) {

    public GameSnapshot(double width, double height, double groundY, double goalTop, double goalDepth,
            String modeDescription, boolean finished, int winner,
            PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
            double ballX, double ballY, double ballRadius,
            String activePowerShot, String powerShotAsset, String powerShotColor,
            double powerShotDuration, double powerShotElapsed, boolean powerShotRenderCopies) {
        this(width, height, groundY, goalTop, goalDepth, modeDescription, finished, winner,
                firstPlayer, secondPlayer, machineOpponent,
                new BallSnapshot(ballX, ballY, ballRadius, activePowerShot == null ? null
                        : new PowerShotSnapshot(activePowerShot, powerShotAsset, powerShotColor,
                                powerShotDuration, powerShotElapsed, powerShotRenderCopies)));
    }

    public double ballX() { return ball.x(); }
    public double ballY() { return ball.y(); }
    public double ballRadius() { return ball.radius(); }
    public String activePowerShot() { return ball.powerShot() == null ? null : ball.powerShot().id(); }
    public String powerShotAsset() { return ball.powerShot() == null ? null : ball.powerShot().asset(); }
    public String powerShotColor() { return ball.powerShot() == null ? null : ball.powerShot().color(); }
    public double powerShotDuration() {
        return ball.powerShot() == null ? 0 : ball.powerShot().duration();
    }
    public double powerShotElapsed() {
        return ball.powerShot() == null ? 0 : ball.powerShot().elapsed();
    }
    public boolean powerShotRenderCopies() {
        return ball.powerShot() != null && ball.powerShot().renderCopies();
    }

    /** Estado del balón y los metadatos visuales de su tiro activo. */
    public record BallSnapshot(double x, double y, double radius, PowerShotSnapshot powerShot) { }

    /** Metadatos de presentación de un tiro especial. */
    public record PowerShotSnapshot(
            String id, String asset, String color, double duration, double elapsed, boolean renderCopies) { }

    /**
     * Instantánea inmutable del estado visual, estadísticas y HUD de un jugador.
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
     * @param points puntos acumulados disponibles para mejoras
     * @param speed nivel de velocidad (1 a 10)
     * @param kick nivel de patada (1 a 10)
     * @param jump nivel de salto (1 a 10)
     * @param survival nivel de supervivencia (1 a 10)
     * @param facingDirection dirección a la que mira ({@code 1} derecha, {@code -1} izquierda)
     * @param kicking indica si está ejecutando una patada
     * @param stunned indica si está aturdido
     * @param auraActive indica si tiene el aura de poder cargada
     * @param statusEffect nombre del efecto de estado activo
     */
    public record PlayerSnapshot(String countryName, String jerseyColor, String flagAsset,
            double x, double y, int goals, double health, double maxHealth,
            double powerCharge, int points, int speed, int kick, int jump, int survival,
            int facingDirection, boolean kicking,
            boolean stunned, boolean auraActive, String statusEffect) { }
}
