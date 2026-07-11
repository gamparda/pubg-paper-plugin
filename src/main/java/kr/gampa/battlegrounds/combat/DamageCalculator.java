package kr.gampa.battlegrounds.combat;
public final class DamageCalculator {
  public DamageResult calculate(WeaponStats weapon, HitLocation hit, double distance, ArmorStats armor) {
    if (distance < 0) throw new IllegalArgumentException("distance must be positive");
    if (distance > weapon.maxRange()) return new DamageResult(0, 0);
    double distanceMultiplier = 1;
    if (distance > weapon.falloffStart() && weapon.maxRange() > weapon.falloffStart()) {
      double progress = (distance - weapon.falloffStart()) / (weapon.maxRange() - weapon.falloffStart());
      distanceMultiplier = 1 - progress * (1 - weapon.minimumDamageMultiplier());
    }
    double raw = weapon.baseDamage() * weapon.hitMultiplier(hit) * distanceMultiplier;
    if (armor == null || !armor.usable()) return new DamageResult(raw, 0);
    double absorbed = Math.min(raw * armor.reduction(), armor.durability());
    return new DamageResult(raw - absorbed, absorbed);
  }
}
