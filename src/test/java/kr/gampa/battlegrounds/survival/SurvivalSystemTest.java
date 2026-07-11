package kr.gampa.battlegrounds.survival;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SurvivalSystemTest {
  @Test void firstLethalTeamDamageKnocksPlayerInsteadOfKilling() {
    SurvivalState state = SurvivalState.alive(100);
    DamageOutcome outcome = state.applyDamage(120, true, false);
    assertEquals(DamageOutcome.KNOCKED, outcome);
    assertTrue(state.downed()); assertEquals(1, state.health());
  }
  @Test void soloOrRepeatedLethalDamageKills() {
    SurvivalState solo = SurvivalState.alive(100);
    assertEquals(DamageOutcome.KILLED, solo.applyDamage(120, false, false));
    SurvivalState team = SurvivalState.alive(100);
    team.applyDamage(120, true, false);
    assertEquals(DamageOutcome.KILLED, team.applyDamage(10, true, false));
  }
  @Test void healingRespectsCapAndReviveRestoresConfiguredHealth() {
    SurvivalState state = SurvivalState.alive(40);
    assertEquals(75, state.heal(80, 75));
    state.applyDamage(100, true, false);
    assertTrue(state.revive(30)); assertEquals(30, state.health());
  }
}
