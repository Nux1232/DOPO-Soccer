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