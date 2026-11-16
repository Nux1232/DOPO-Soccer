package domain.model.entity;
import domain.model.exception.DopoSoccerException;

/**
 * Representa a un jugador durante un partido de DOPOSoccer.
 */
public final class Player {

    /** Radio del jugador en píxeles. */
    public static final double RADIUS = 25.0;

    /** Valor máximo de la barra de poder. */
    public static final double MAX_POWER_CHARGE = 100.0;

    /** Segundos que tarda la barra de poder en llenarse por completo . */
    public static final double POWER_CHARGE_SECONDS = 10.0;

    /** Segundos que dura el aturdimiento cuando la vida llega a cero. */
    public static final double STUN_DURATION_SECONDS = 3.0;

    /** Daño base de una patada normal . */
    public static final double BASE_KICK_DAMAGE = 10.0;

    private final Country country;
    private final PlayerStats stats = new PlayerStats();
    private int goals;
    private double health;
    private double powerCharge;
    private Power power = Power.NONE;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private boolean grounded;
    private double kickCooldown;
    private double stunTimer;
    private PlayerStatusEffect statusEffect = PlayerStatusEffect.NONE;
    private double statusEffectTimer;

    public Player(Country country, double x) {
        this.country = country;
        this.x = x;
        this.health = maxHealth();
    }

    public Country country() {
        return country;
    }

    public PlayerStats stats() {
        return stats;
    }

    public int goals() {
        return goals;
    }

    public double health() {
        return health;
    }

    /**
     * Calcula la vida máxima según la estadística Survival (Anexo 2, Tabla 2):
     * vidaMax = 100 * (1 + 0.10 * (Survival - 1)).
     *
     * @return puntos de vida máximos (entre 100 y 190)
     */
    public double maxHealth() {
        return Math.round(100.0 * (1.0 + 0.10 * (stats.survival() - 1)));
    }

    /**
     * Calcula el multiplicador de daño según la estadística Kick (Anexo 2, Tabla 3):
     * multiplicadorKick = 1 + 0.10 * (Kick - 1).
     *
     * @return multiplicador entre 1.0 y 1.9
     */
    public double kickMultiplier() {
        return 1.0 + 0.10 * (stats.kick() - 1);
    }

    /**
     * Calcula el daño que hace una patada normal de este jugador:
     * 10 * multiplicadorKick.
     *
     * @return daño de patada (entre 10 y 19)
     */
    public double kickDamage() {
        return BASE_KICK_DAMAGE * kickMultiplier();
    }

    public double powerCharge() {
        return powerCharge;
    }

    public Power power() {
        return power;
    }

    /**
     * Indica si el jugador está aturdido (vida llegó a 0).
     *
     * @return true si está aturdido y no puede moverse, saltar ni patear
     */
    public boolean stunned() {
        return stunTimer > 0;
    }

    public double stunTimer() {
        return stunTimer;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double velocityX() {
        return velocityX;
    }

    public double velocityY() {
        return velocityY;
    }

    public boolean grounded() {
        return grounded;
    }

    public double kickCooldown() {
        return kickCooldown;
    }

    public PlayerStatusEffect statusEffect() {
        return statusEffect;
    }

    public double statusEffectTimer() {
        return statusEffectTimer;
    }

    public boolean canJump() {
        return !stunned() && statusEffect.canJump();
    }

    public boolean canKick() {
        return !stunned() && statusEffect.canKick();
    }

    public void scoreGoal() {
        goals++;
    }

    /**
     * Regla 1 de poderes: cuando el jugador recibe un gol, su barra de poder
     * aumenta de inmediato un cuarto (1/4) de su capacidad máxima.
     */
    public void onGoalConceded() {
        powerCharge = Math.min(MAX_POWER_CHARGE, powerCharge + MAX_POWER_CHARGE * 0.25);
    }

    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
        velocityX = 0;
        velocityY = 0;
        grounded = false;
        kickCooldown = 0;
        clearStatusEffect();
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void startKickCooldown(double seconds) {
        kickCooldown = seconds;
    }

    /**
     * Avanza los temporizadores del jugador:
     * - Reduce la espera de patada.
     * - Recarga la barra de poder en 10 segundos (incluso si ya tiene el aura activa).
     * - Si está aturdido, descuenta los 3 segundos y al terminar restaura la vida al máximo.
     *
     * @param seconds segundos transcurridos
     */
    public void tick(double seconds) {
        kickCooldown = Math.max(0.0, kickCooldown - seconds);

        double chargePerSecond = MAX_POWER_CHARGE / POWER_CHARGE_SECONDS;
        powerCharge = Math.min(MAX_POWER_CHARGE, powerCharge + seconds * chargePerSecond);

        if (stunTimer > 0) {
            stunTimer = Math.max(0.0, stunTimer - seconds);
            if (stunTimer == 0.0) {
                health = maxHealth();
            }
        }

        if (statusEffectTimer > 0) {
            statusEffectTimer = Math.max(0.0, statusEffectTimer - seconds);
            if (statusEffectTimer == 0.0) {
                clearStatusEffect();
            }
        }
    }

    /**
     * Reglas 2, 3 y 7 de poderes:
     * Con la barra llena y sin poder activo, activa el aura (sin límite de tiempo)
     * y vacía la barra para que empiece a recargarse de inmediato.
     *
     * @return true si se activó el aura
     */
    public boolean activatePower() {
        if (power != Power.NONE || powerCharge < MAX_POWER_CHARGE || stunned()) {
            return false;
        }
        power = Power.AURA;
        powerCharge = 0.0;
        return true;
    }

    /**
     * Regla 4 de poderes: cuando el personaje con poder toca el balón,
     * ejecuta su tiro de poder y el aura desaparece.
     *
     * @return true si tenía el aura activa y fue consumida
     */
    public boolean consumePower() {
        if (power == Power.NONE) {
            return false;
        }
        power = Power.NONE;
        return true;
    }

    public void applyStatusEffect(PlayerStatusEffect effect, double seconds) {
        if (effect == null || effect == PlayerStatusEffect.NONE || seconds <= 0 || stunned()) {
            return;
        }
        statusEffect = effect;
        statusEffectTimer = seconds;
    }

    public void clearStatusEffect() {
        statusEffect = PlayerStatusEffect.NONE;
        statusEffectTimer = 0.0;
    }

    /**
     * Resta vida al jugador. Si la vida llega a cero, queda aturdido durante 3 segundos.
     *
     * @param amount daño recibido (no negativo)
     */
    public void takeDamage(double amount) throws DopoSoccerException{
        if (amount < 0) {
            throw new DopoSoccerException(DopoSoccerException.NEGATIVE_DAMAGE);
        }
        if (stunned()) {
            return;
        }
        health = Math.max(0.0, Math.min(maxHealth(), health) - amount);
        if (health == 0.0) {
            stunTimer = STUN_DURATION_SECONDS;
            velocityX = 0.0;
        }
    }

    public void resetForMatch(double x, double y) {
        goals = 0;
        health = maxHealth();
        powerCharge = 0;
        power = Power.NONE;
        stunTimer = 0;
        clearStatusEffect();
        moveTo(x, y);
    }
}