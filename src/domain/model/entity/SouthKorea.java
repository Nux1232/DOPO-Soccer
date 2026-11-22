package domain.model.entity;
import domain.model.game.Game;
import domain.model.exception.DopoSoccerException;
public final class SouthKorea extends Country {
 public SouthKorea(){super("south-korea","Corea del Sur","#CD2E3A","kr.png",1.5,8,820,0,PlayerStatusEffect.PUSHED,.72);}
 @Override protected String powerAsset(){return "Ko_power.gif";} @Override protected String powerColor(){return "#36a9ff";}
 @Override public void applyImpact(Player owner,Player opponent)throws DopoSoccerException {super.applyImpact(owner,opponent);if(!opponent.stunned())opponent.setVelocity(opponent.x()<Game.WIDTH/2?-600:600,-180);}
}
