package kr.gampa.battlegrounds.item;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class CarryCapacityTest {
 @Test void combinesBaseVestAndBackpackCapacity(){var c=new CarryCapacity(50,50,200);assertEquals(300,c.maximum());assertTrue(c.canCarry(296,4));assertFalse(c.canCarry(296,5));}
}
