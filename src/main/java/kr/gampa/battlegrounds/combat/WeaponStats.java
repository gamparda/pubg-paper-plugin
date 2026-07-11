package kr.gampa.battlegrounds.combat;
public record WeaponStats(String id, double baseDamage, double falloffStart, double maxRange,
                          double minimumDamageMultiplier, double headMultiplier,
                          double bodyMultiplier, double limbMultiplier) {
  public WeaponStats {
    if (id == null || id.isBlank()) throw new IllegalArgumentException("weapon id is required");
    if (baseDamage < 0 || falloffStart < 0 || maxRange < falloffStart) throw new IllegalArgumentException("invalid weapon range/damage");
    if (minimumDamageMultiplier < 0 || minimumDamageMultiplier > 1) throw new IllegalArgumentException("invalid minimum multiplier");
  }
  public double hitMultiplier(HitLocation location) {
    return switch (location) { case HEAD -> headMultiplier; case BODY -> bodyMultiplier; case LIMB -> limbMultiplier; };
  }
}
