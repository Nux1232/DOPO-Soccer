package domain.model.entity;

public final class Player {
    public static final double RADIUS = 25;
    public static final double MAX_POWER_CHARGE = 100;

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
    private double powerDuration;

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

    public double maxHealth() {
        return 100 + (stats.survival() - 5) * 10;
    }

    public double powerCharge() {
        return powerCharge;
    }

    public Power power() {
        return power;
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

    public void scoreGoal() {
        goals++;
    }

    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
        velocityX = 0;
        velocityY = 0;
        grounded = false;
        kickCooldown = 0;
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

    public boolean activatePower() {
        if (power != Power.NONE || powerCharge < MAX_POWER_CHARGE) {
            return false;
        }
        power = Power.AURA;
        powerCharge = 0;
        powerDuration = 5;
        return true;
    }

    public void takeDamage(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo.");
        }
        health = Math.max(0, Math.min(maxHealth(), health) - amount);
    }

    public void resetForMatch(double x, double y) {
        goals = 0;
        health = maxHealth();
        powerCharge = 0;
        power = Power.NONE;
        powerDuration = 0;
        moveTo(x, y);
    }
}
