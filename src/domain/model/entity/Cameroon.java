package domain.model.entity;
import domain.model.game.Game;
import domain.model.exception.DopoSoccerException;

public final class Cameroon extends Country {
 public Cameroon() {
  super(
          "cameroon",
          "Camerún",
          "#F2B705",
          "cm.png",
          3.0,
          12,
          980,
          -100,
          PlayerStatusEffect.ELECTRIFIED,
          .84
  );
 } @Override protected String powerAsset(){return "Cm_power.gif";} @Override protected String powerColor(){return "#ffe34a";}
 @Override public void applyImpact(Player owner,Player opponent)throws DopoSoccerException {
  super.applyImpact(owner,opponent);
  if(!opponent.stunned())opponent.applyKnockback(opponent.x()<Game.WIDTH/2?-640:640,-300,.4);
 }
}
