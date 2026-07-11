package kr.gampa.battlegrounds.item;
public final class ArmorCondition {private final ArmorKind kind;private final double reduction;private double durability;private boolean broken;
 public ArmorCondition(ArmorKind kind,double reduction,double durability){if(kind==null||reduction<0||reduction>1||durability<0)throw new IllegalArgumentException();this.kind=kind;this.reduction=reduction;this.durability=durability;this.broken=durability==0;}
 public ArmorHitResult absorb(double raw){if(raw<0)throw new IllegalArgumentException();double active=activeReduction(),health=raw*(1-active),lost=Math.min(durability,raw*active);durability-=lost;if(durability<=0)broken=true;return new ArmorHitResult(health,lost,kind==ArmorKind.HELMET&&broken);}
 public double activeReduction(){return broken?(kind==ArmorKind.VEST?.20:0):reduction;} public double durability(){return durability;} public boolean broken(){return broken;}}
