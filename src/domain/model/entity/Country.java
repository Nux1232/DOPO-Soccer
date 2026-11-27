package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Base abstracta para personajes: identidad y comportamiento de su poder. */
public abstract class Country implements Power {
    private static final double POWER_DAMAGE_MULTIPLIER = 5.0;
    private final String id, displayName, color, flagAsset;
    private final double duration, baseDamage, speedX, speedY, restitution;
    private final PlayerStatusEffect effect;

    protected Country(String id, String displayName, String color, String flagAsset,
                      double duration, double baseDamage, double speedX, double speedY,
                      PlayerStatusEffect effect, double restitution) {
        this.id=id; this.displayName=displayName; this.color=color; this.flagAsset=flagAsset;
        this.duration=duration; this.baseDamage=baseDamage; this.speedX=speedX; this.speedY=speedY;
        this.effect=effect; this.restitution=restitution;
    }
    public final String id(){return id;}
    public final String displayName(){return displayName;}
    public final String color(){return color;}
    public final String flagAsset(){return flagAsset;}
    @Override public final String visualAsset(){return powerAsset();}
    @Override public final String visualColor(){return powerColor();}
    protected abstract String powerAsset();
    protected abstract String powerColor();
    @Override public final double durationSeconds(){return duration;}
    @Override public final double damage(){return baseDamage*POWER_DAMAGE_MULTIPLIER;}
    @Override public final double horizontalSpeed(){return speedX;}
    @Override public final double verticalSpeed(){return speedY;}
    @Override public final PlayerStatusEffect hitEffect(){return effect;}
    @Override public final double restitution(){return restitution;}
    @Override public void applyImpact(Player owner, Player opponent) throws DopoSoccerException {
        opponent.takeDamage(damage());
        if (!opponent.stunned()) opponent.applyStatusEffect(hitEffect(), durationSeconds());
    }
    public String name(){return id.toUpperCase(java.util.Locale.ROOT).replace('-', '_');}
    @Override public String toString(){return displayName;}
}
