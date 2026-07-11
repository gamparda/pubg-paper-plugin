package kr.gampa.battlegrounds.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class WeaponMechanicsTest {
  @Test void fireModeCyclesOnlyThroughSupportedModes(){
    FireModeState state=new FireModeState("single",java.util.List.of("single","burst","auto"));
    assertEquals("burst",state.cycle()); assertEquals("auto",state.cycle()); assertEquals("single",state.cycle());
  }
  @Test void shotgunPatternCreatesConfiguredPelletsWithinSpread(){
    var vectors=ShotPattern.create(9,8,1234L);
    assertEquals(9,vectors.size());
    assertTrue(vectors.stream().allMatch(v->Math.abs(v.yawDegrees())<=8&&Math.abs(v.pitchDegrees())<=8));
  }
  @Test void burstConsumesOnlyRemainingRounds(){
    assertEquals(2,WeaponMechanics.shotsForTrigger("burst",3,2));
    assertEquals(1,WeaponMechanics.shotsForTrigger("single",30,30));
    assertEquals(1,WeaponMechanics.shotsForTrigger("auto",30,30));
  }
}
