package kr.gampa.battlegrounds.item;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class ArmorConditionTest {
 @Test void intactArmorKeepsFullReductionUntilLastDurability(){var a=new ArmorCondition(ArmorKind.HELMET,.55,1);var r=a.absorb(50);assertEquals(22.5,r.healthDamage(),.001);assertTrue(r.destroyed());}
 @Test void brokenVestRemainsAndReducesTwentyPercent(){var a=new ArmorCondition(ArmorKind.VEST,.55,1);a.absorb(50);var r=a.absorb(50);assertFalse(r.destroyed());assertEquals(40,r.healthDamage(),.001);assertEquals(.20,a.activeReduction(),.001);}
}
