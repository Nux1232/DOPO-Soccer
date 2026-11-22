package domain.model.entity;
/** Jugador situado en el lado derecho del campo. */
public final class RPlayer extends Player {
 @Override public PlayerSide side(){return PlayerSide.RIGHT;}
 public RPlayer(Country country, double x){super(country,x);setFacingDirection(-1);}
}
