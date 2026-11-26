package domain.model.entity;
public final class UnitedStates extends Country {
 public UnitedStates(){super("usa","Estados Unidos","#3C3B6E","us.png",2.5,6,700,-50,PlayerStatusEffect.CONFUSED,.72);}
 @Override protected String powerAsset(){return "usa-illusion.gif";} @Override protected String powerColor(){return "#c5a0ff";}
 @Override public boolean renderCopies(){return true;}
}
