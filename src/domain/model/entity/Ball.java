package domain.model.entity;

/**
 * Representa el balón del partido.
 *
 * Esta clase solo guarda el estado del balón (posición y velocidad).
 * No contiene reglas ni física: quien calcula cómo se mueve y rebota
 * es otra clase del dominio.
 */
public final class Ball {

    /** Radio del balón, en las mismas unidades que usa la cancha. */
    public static final double RADIUS = 15.0;

    /** Posición horizontal del balón. */
    private double x;

    /** Posición vertical del balón. */
    private double y;

    /** Velocidad horizontal actual del balón. */
    private double velocityX;

    /** Velocidad vertical actual del balón. */
    private double velocityY;
    private ProjectilePower activePowerShot;
    private Player powerShotOwner;
    private double powerShotTime;
    private boolean powerEffectApplied;

    /**
     * Crea un balón en una posición inicial. Su velocidad inicial es cero.
     *
     * @param x posición horizontal inicial
     * @param y posición vertical inicial
     */
    public Ball(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Devuelve la posición horizontal del balón.
     *
     * @return la coordenada x actual
     */
    public double x() {
        return this.x;
    }

    /**
     * Devuelve la posición vertical del balón.
     *
     * @return la coordenada y actual
     */
    public double y() {
        return this.y;
    }

    /**
     * Devuelve la velocidad horizontal del balón.
     *
     * @return la velocidad en el eje x
     */
    public double velocityX() {
        return this.velocityX;
    }

    /**
     * Devuelve la velocidad vertical del balón.
     *
     * @return la velocidad en el eje y
     */
    public double velocityY() {
        return this.velocityY;
    }

    /** @return tiro de poder activo en el balón, o {@code null} si es un tiro normal */
    public ProjectilePower activePowerShot() {
        return activePowerShot;
    }

    /** @return jugador que ejecutó el tiro de poder activo */
    public Player powerShotOwner() {
        return powerShotOwner;
    }

    /** @return tiempo restante en segundos del tiro de poder activo */
    public double powerShotTime() {
        return powerShotTime;
    }

    /** @return {@code true} si el efecto de impacto ya fue aplicado al rival */
    public boolean powerEffectApplied() {
        return powerEffectApplied;
    }

    /**
     * Inicia un tiro de poder sobre el balón.
     *
     * @param shot poder especial ejecutado
     * @param owner jugador propietario del disparo
     */
    public void startPowerShot(ProjectilePower shot, Player owner) {
        activePowerShot = shot;
        powerShotOwner = owner;
        powerShotTime = shot.durationSeconds();
        powerEffectApplied = false;
    }

    /** Marca que el impacto del tiro de poder ya se aplicó al oponente. */
    public void markPowerEffectApplied() {
        powerEffectApplied = true;
    }

    /**
     * Descuenta el tiempo restante del tiro de poder y lo limpia al expirar.
     *
     * @param seconds segundos transcurridos
     */
    public void tickPowerShot(double seconds) {
        if (activePowerShot == null) {
            return;
        }
        powerShotTime = Math.max(0.0, powerShotTime - seconds);
        if (powerShotTime == 0.0) {
            clearPowerShot();
        }
    }

    /**
     * Restaura un tiro de poder activo desde una partida guardada.
     *
     * @param shot poder restaurado
     * @param owner jugador propietario del tiro
     * @param remaining tiempo restante en segundos
     * @param effectApplied {@code true} si el impacto ya fue aplicado
     */
    public void restorePowerShot(ProjectilePower shot, Player owner, double remaining, boolean effectApplied) {
        if (shot == null || owner == null) {
            clearPowerShot();
            return;
        }
        activePowerShot = shot;
        powerShotOwner = owner;
        powerShotTime = Math.max(0, remaining);
        powerEffectApplied = effectApplied;
    }

    /** Limpia cualquier tiro de poder activo sobre el balón. */
    public void clearPowerShot() {
        activePowerShot = null;
        powerShotOwner = null;
        powerShotTime = 0.0;
        powerEffectApplied = false;
    }

    /**
     * Cambia la posición del balón.
     *
     * @param x nueva posición horizontal
     * @param y nueva posición vertical
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Cambia la velocidad del balón.
     *
     * @param velocityX nueva velocidad horizontal
     * @param velocityY nueva velocidad vertical
     */
    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
}