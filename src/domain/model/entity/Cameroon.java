package domain.model.entity;
public final class Cameroon extends Country {
 public Cameroon(){super("cameroon","Camerún","#F2B705","cm.png",3.0,12,760,0,PlayerStatusEffect.ELECTRIFIED,.72);}
 @Override protected String powerAsset(){return "Cm_power.gif";} @Override protected String powerColor(){return "#ffe34a";}
}
