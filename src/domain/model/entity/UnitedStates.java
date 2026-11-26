package domain.model.entity;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
public final class UnitedStates extends Country {
 public UnitedStates(){super("usa","Estados Unidos","#3C3B6E","us.png",3.5,8,940,-120,PlayerStatusEffect.CONFUSED,.84);}
 @Override protected String powerAsset(){return "usa-illusion.gif";} @Override protected String powerColor(){return "#c5a0ff";}
 @Override public void applyImpact(Player owner,Player opponent)throws DopoSoccerException {
  super.applyImpact(owner,opponent);
  if(!opponent.stunned())opponent.applyKnockback(opponent.x()<Game.WIDTH/2?-560:560,-500,.45);
 }
 @Override public boolean renderCopies(){return true;}
}
