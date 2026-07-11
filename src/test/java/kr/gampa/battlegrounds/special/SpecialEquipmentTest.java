package kr.gampa.battlegrounds.special;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class SpecialEquipmentTest {
 @Test void jammerConsumesEnergyInsteadOfHealth(){var j=new JammerEnergy(100);assertEquals(0,j.blockBlueZoneDamage(30),.001);assertEquals(70,j.energy(),.001);assertEquals(20,j.blockBlueZoneDamage(90),.001);}
 @Test void foldingShieldBreaksAtAccumulatedDamage(){var s=new ShieldState(1800);assertFalse(s.damage(1799));assertTrue(s.damage(1));}
 @Test void selfReviveConsumesOnActivationEvenWhenCancelled(){var r=new SelfReviveState();assertTrue(r.activate());r.cancel();assertTrue(r.consumed());assertFalse(r.complete());}
}
