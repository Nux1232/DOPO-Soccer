package domain.model.entity;
import domain.model.game.Game;
import domain.model.exception.DopoSoccerException;
public final class SouthKorea extends Country {
 public SouthKorea(){super("south-korea","Corea del Sur","#CD2E3A","kr.png",2.0,10,1020,-160,PlayerStatusEffect.PUSHED,.88);}
 @Override protected String powerAsset(){return "Ko_power.gif";} @Override protected String powerColor(){return "#36a9ff";}
 @Override public void applyImpact(Player owner,Player opponent)throws DopoSoccerException {
  super.applyImpact(owner,opponent);
  if(!opponent.stunned())opponent.applyKnockback(opponent.x()<Game.WIDTH/2?-820:820,-440,.55);
 }
}
