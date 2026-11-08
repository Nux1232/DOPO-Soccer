package domain.model.entity;

/**
 * Representa a un jugador durante un partido.
 *
 * Guarda su personaje (país), sus estadísticas, sus goles, su vida,
 * su barra de poder y el poder activo, además de su posición y su
 * movimiento en la cancha. La clase solo mantiene el estado y aplica
 * reglas simples (daño, carga del poder); el movimiento y las colisiones
 * los calcula otra clase del dominio.
 */
public final class Player {

    /** Radio del jugador, en las mismas unidades que usa la cancha. */
    public static final double RADIUS = 25;

    /** Valor máximo de la barra de poder. */
    public static final double MAX_POWER_CHARGE = 100;

    /** Personaje (país) que eligió el jugador. No cambia durante el partido. */
    private final Country country;

    /** Estadísticas del jugador (velocidad, patada, salto y supervivencia). */
    private final PlayerStats stats = new PlayerStats();

    /** Goles anotados en el partido actual. */
    private int goals;

    /** Vida actual del jugador. */
    private double health;

    /** Carga actual de la barra de poder, entre 0 y #MAX_POWER_CHARGE. */
    private double powerCharge;

    /** Poder activo en este momento, Power.NONE si no tiene ninguno. */
    private Power power = Power.NONE;

    /** Posición horizontal del jugador. */
    private double x;

    /** Posición vertical del jugador. */
    private double y;

    /** Velocidad horizontal actual. */
    private double velocityX;

    /** Velocidad vertical actual. */
    private double velocityY;

    /** Indica si el jugador está apoyado en el suelo. */
    private boolean grounded;

    /** Segundos que faltan para poder volver a patear. */
    private double kickCooldown;

    /** Segundos que le quedan al poder activo antes de apagarse. */
    private double powerDuration;

    /**
     * Crea un jugador con un personaje y una posición horizontal inicial.
     * Empieza con la vida al máximo, la barra de poder vacía y sin goles.
     *
     * @param country personaje (país) del jugador
     * @param x posición horizontal inicial
     */
    public Player(Country country, double x) {
        this.country = country;
        this.x = x;
        this.health = maxHealth();
    }

    /**
     * Devuelve el personaje del jugador.
     *
     * @return el país elegido
     */
    public Country country() {
        return country;
    }

    /**
     * Devuelve las estadísticas del jugador.
     *
     * @return las estadísticas actuales
     */
    public PlayerStats stats() {
        return stats;
    }

    /**
     * Devuelve los goles anotados en el partido.
     *
     * @return número de goles
     */
    public int goals() {
        return goals;
    }

    /**
     * Devuelve la vida actual del jugador.
     *
     * @return puntos de vida actuales
     */
    public double health() {
        return health;
    }

    /**
     * Calcula la vida máxima según la estadística de supervivencia,
     * con la fórmula 100 + (survival - 5) * 10.
     *
     * @return puntos de vida máximos
     */
    public double maxHealth() {
        return 100 + (stats.survival() - 5) * 10;
    }

    /**
     * Devuelve la carga actual de la barra de poder.
     *
     * @return valor entre 0 y MAX_POWER_CHARGE
     */
    public double powerCharge() {
        return powerCharge;
    }

    /**
     * Devuelve el poder activo del jugador.
     *
     * @return el poder activo, o Power.NONE si no tiene ninguno
     */
    public Power power() {
        return power;
    }

    /**
     * Devuelve la posición horizontal del jugador.
     *
     * @return la coordenada x actual
     */
    public double x() {
        return x;
    }

    /**
     * Devuelve la posición vertical del jugador.
     *
     * @return la coordenada y actual
     */
    public double y() {
        return y;
    }

    /**
     * Devuelve la velocidad horizontal del jugador.
     *
     * @return la velocidad en el eje x
     */
    public double velocityX() {
        return velocityX;
    }

    /**
     * Devuelve la velocidad vertical del jugador.
     *
     * @return la velocidad en el eje y
     */
    public double velocityY() {
        return velocityY;
    }

    /**
     * Indica si el jugador está apoyado en el suelo.
     *
     * @return {@code true} si está en el suelo
     */
    public boolean grounded() {
        return grounded;
    }

    /**
     * Devuelve el tiempo restante antes de poder patear de nuevo.
     *
     * @return segundos restantes; 0 si ya puede patear
     */
    public double kickCooldown() {
        return kickCooldown;
    }

    /** Suma un gol al marcador del jugador. */
    public void scoreGoal() {
        goals++;
    }

    /**
     * Coloca al jugador en una posición y lo deja quieto: pone su
     * velocidad en cero, lo marca como no apoyado en el suelo y
     * reinicia el tiempo de espera de la patada.
     *
     * @param x nueva posición horizontal
     * @param y nueva posición vertical
     */
    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
        velocityX = 0;
        velocityY = 0;
        grounded = false;
        kickCooldown = 0;
    }

    /**
     * Cambia solo la posición del jugador, sin tocar su velocidad
     * ni el resto de su estado.
     *
     * @param x nueva posición horizontal
     * @param y nueva posición vertical
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Cambia la velocidad del jugador.
     *
     * @param velocityX nueva velocidad horizontal
     * @param velocityY nueva velocidad vertical
     */
    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /**
     * Indica si el jugador está apoyado en el suelo.
     *
     * @param grounded true si está en el suelo
     */
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    /**
     * Inicia el tiempo de espera entre patadas.
     *
     * @param seconds segundos que deben pasar antes de la siguiente patada
     */
    public void startKickCooldown(double seconds) {
        kickCooldown = seconds;
    }

    /**
     * Actualiza el estado del jugador al pasar el tiempo.
     *
     * Reduce el tiempo de espera de la patada, recarga la barra de
     * poder (5 unidades por segundo, sin pasar del máximo) y descuenta
     * tiempo al poder activo; cuando este llega a cero, el poder se apaga.
     *
     * @param seconds segundos transcurridos desde la actualización anterior
     */
    public void tick(double seconds) {
        kickCooldown = Math.max(0, kickCooldown - seconds);
        powerCharge = Math.min(MAX_POWER_CHARGE, powerCharge + seconds * 5);
        if (powerDuration > 0) {
            powerDuration = Math.max(0, powerDuration - seconds);
            if (powerDuration == 0) {
                power = Power.NONE;
            }
        }
    }

    /**
     * Intenta activar el poder del jugador.
     *
     * Solo se activa si no tiene ya un poder activo y la barra está
     * llena. Al activarse, la barra se vacía y el poder queda activo
     * durante 5 segundos.
     *
     * @return true si el poder se activó, false si no
     *         se cumplían las condiciones
     */
    public boolean activatePower() {
        if (power != Power.NONE || powerCharge < MAX_POWER_CHARGE) {
            return false;
        }
        power = Power.AURA;
        powerCharge = 0;
        powerDuration = 5;
        return true;
    }

    /**
     * Resta vida al jugador. La vida nunca baja de cero.
     *
     * @param amount daño a aplicar; no puede ser negativo
     * @throws IllegalArgumentException si el daño es negativo
     */
    public void takeDamage(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo.");
        }
        health = Math.max(0, Math.min(maxHealth(), health) - amount);
    }

    /**
     * Reinicia al jugador para empezar un partido: pone los goles en cero,
     * la vida al máximo, la barra de poder vacía y sin poder activo, y
     * lo coloca en la posición indicada.
     *
     * @param x posición horizontal inicial
     * @param y posición vertical inicial
     */
    public void resetForMatch(double x, double y) {
        goals = 0;
        health = maxHealth();
        powerCharge = 0;
        power = Power.NONE;
        powerDuration = 0;
        moveTo(x, y);
    }
}