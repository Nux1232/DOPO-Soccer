package domain.model.entity;
import domain.model.game.Game;
import domain.model.exception.DopoSoccerException;
public final class Nigeria extends Country {
 public Nigeria() {
  super(
          "nigeria",
          "Nigeria",
          "#008751",
          "ng.png",
          3.0,
          8,
          880,
          -200,
          PlayerStatusEffect.UNCONSCIOUS,
          .99
  );
 } @Override protected String powerAsset(){return "Ng_power.gif";} @Override protected String powerColor(){return "#48d99a";}
 @Override public void applyImpact(Player owner,Player opponent)throws DopoSoccerException {
  super.applyImpact(owner,opponent);
  if(!opponent.stunned())opponent.applyKnockback(opponent.x()<Game.WIDTH/2?-420:420,-900,.75);
 }
}
