package kr.gampa.battlegrounds.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MagazineTest {
  @Test void firingConsumesRoundsAndEmptyMagazineCannotFire() {
    Magazine magazine = new Magazine(2);
    assertTrue(magazine.fire()); assertTrue(magazine.fire()); assertFalse(magazine.fire());
    assertEquals(0, magazine.rounds());
  }
  @Test void reloadUsesOnlyAvailableAmmo() {
    Magazine magazine = new Magazine(5, 1);
    assertEquals(3, magazine.reload(3));
    assertEquals(4, magazine.rounds());
  }
}
