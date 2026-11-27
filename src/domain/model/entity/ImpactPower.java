package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/**
 * Tiro de poder configurable basado en proyectil con daño escalado por la estadística
 * {@code kick}, estado temporal y retroceso físico.
 */
public final class ImpactPower implements ProjectilePower {
    private final String id;
    private final String visualAsset;
    private final String visualColor;
    private final double durationSeconds;
    private final double damage;
    private final double horizontalSpeed;
    private final double verticalSpeed;
    private final PlayerStatusEffect hitEffect;
    private final double restitution;
    private final double knockbackX;
    private final double knockbackY;
    private final double knockbackSeconds;
    private final boolean renderCopies;

    /**
     * Construye un poder de impacto con sus parámetros físicos, visuales y de combate.
     *
     * @param id identificador del poder
     * @param visualAsset nombre del recurso gráfico del proyectil
     * @param visualColor color hexadecimal de respaldo para el renderizado
     * @param durationSeconds duración del tiro y del efecto en segundos
     * @param damage daño base del tiro ({@code DañoTiro × 5})
     * @param horizontalSpeed velocidad horizontal inicial en píxeles por segundo
     * @param verticalSpeed velocidad vertical inicial en píxeles por segundo
     * @param hitEffect efecto de estado aplicado al rival al impactar
     * @param restitution coeficiente de rebote del balón mientras dura el tiro
     * @param knockbackX magnitud horizontal del retroceso al impactar
     * @param knockbackY velocidad vertical del retroceso al impactar
     * @param knockbackSeconds duración del bloqueo físico de entrada tras el impacto
     * @param renderCopies {@code true} si el balón debe renderizarse multiplicado en ilusiones
     * @throws DopoSoccerException si algún parámetro es nulo o inválido
     */
    public ImpactPower(String id, String visualAsset, String visualColor,
            double durationSeconds, double damage, double horizontalSpeed, double verticalSpeed,
            PlayerStatusEffect hitEffect, double restitution,
            double knockbackX, double knockbackY, double knockbackSeconds, boolean renderCopies) {
        if (id == null || visualAsset == null || visualColor == null || hitEffect == null
                || durationSeconds <= 0 || damage < 0 || restitution < 0 || knockbackSeconds < 0) {
            throw new DopoSoccerException("La configuración del poder es inválida.");
        }
        this.id = id;
        this.visualAsset = visualAsset;
        this.visualColor = visualColor;
        this.durationSeconds = durationSeconds;
        this.damage = damage;
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
        this.hitEffect = hitEffect;
        this.restitution = restitution;
        this.knockbackX = knockbackX;
        this.knockbackY = knockbackY;
        this.knockbackSeconds = knockbackSeconds;
        this.renderCopies = renderCopies;
    }

    @Override public String id() { return id; }
    @Override public String visualAsset() { return visualAsset; }
    @Override public String visualColor() { return visualColor; }
    @Override public double durationSeconds() { return durationSeconds; }
    @Override public double damage() { return damage; }
    @Override public double horizontalSpeed() { return horizontalSpeed; }
    @Override public double verticalSpeed() { return verticalSpeed; }
    @Override public PlayerStatusEffect hitEffect() { return hitEffect; }
    @Override public double restitution() { return restitution; }
    @Override public boolean renderCopies() { return renderCopies; }

    /**
     * Aplica el daño escalado ({@code dañoBase * multiplicadorKick}), el efecto temporal
     * y el retroceso hacia el arco propio del oponente.
     *
     * @param owner jugador que ejecutó el tiro de poder
     * @param opponent jugador que recibe el impacto
     * @throws DopoSoccerException si el daño calculado resulta negativo o hay datos inválidos
     */
    @Override
    public void applyImpact(Player owner, Player opponent) throws DopoSoccerException {
        if (owner == null || opponent == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        double scaledDamage = damage * owner.kickMultiplier();
        opponent.takeDamage(scaledDamage);
        if (!opponent.stunned()) {
            opponent.applyStatusEffect(hitEffect, durationSeconds);
            if (knockbackSeconds > 0) {
                double direction = opponent.side() == PlayerSide.LEFT ? -1.0 : 1.0;
                opponent.applyKnockback(direction * knockbackX, knockbackY, knockbackSeconds);
            }
        }
    }
}
