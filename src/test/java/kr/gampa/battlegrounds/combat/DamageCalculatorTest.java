package kr.gampa.battlegrounds.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DamageCalculatorTest {
  private final DamageCalculator calculator = new DamageCalculator();

  @Test void appliesHitLocationDistanceAndArmorInOrder() {
    WeaponStats weapon = new WeaponStats("akm", 50, 100, 200, 0.5, 2.0, 1.0, 0.75);
    ArmorStats armor = new ArmorStats("helmet-2", 0.40, 100);
    DamageResult result = calculator.calculate(weapon, HitLocation.HEAD, 150, armor);
    assertEquals(45.0, result.healthDamage(), 0.001);
    assertEquals(30.0, result.armorDamage(), 0.001);
  }

  @Test void brokenArmorDoesNotReduceDamage() {
    WeaponStats weapon = new WeaponStats("akm", 50, 100, 200, 0.5, 2.0, 1.0, 0.75);
    DamageResult result = calculator.calculate(weapon, HitLocation.BODY, 0, new ArmorStats("vest", .5, 0));
    assertEquals(50, result.healthDamage(), .001);
    assertEquals(0, result.armorDamage(), .001);
  }

  @Test void rejectsDistancesOutsideWeaponRange() {
    WeaponStats weapon = new WeaponStats("pistol", 30, 20, 60, .5, 2, 1, .7);
    assertEquals(0, calculator.calculate(weapon, HitLocation.BODY, 61, null).healthDamage());
  }
}
