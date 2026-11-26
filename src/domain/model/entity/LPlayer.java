package domain.model.entity;
/** Jugador situado en el lado izquierdo del campo. */
public final class LPlayer extends Player {
 @Override public PlayerSide side(){return PlayerSide.LEFT;}
 public LPlayer(Country country, double x){super(country,x);setFacingDirection(1);}
}
