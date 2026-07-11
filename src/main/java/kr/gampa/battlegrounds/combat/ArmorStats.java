package kr.gampa.battlegrounds.combat;
public record ArmorStats(String id, double reduction, double durability) {
  public ArmorStats {
    if (reduction < 0 || reduction > 1 || durability < 0) throw new IllegalArgumentException("invalid armor stats");
  }
  public boolean usable() { return durability > 0; }
}
