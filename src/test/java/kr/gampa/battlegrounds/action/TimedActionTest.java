package kr.gampa.battlegrounds.action;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class TimedActionTest {
 @Test void completesOnlyAfterConfiguredTicks(){var a=new TimedAction(60,2);assertEquals(ActionStatus.RUNNING,a.tick(1));assertEquals(ActionStatus.COMPLETED,a.tick(59));}
 @Test void excessiveMovementDamageAndWeaponSwitchCancel(){assertEquals(ActionStatus.CANCELLED,new TimedAction(60,2).move(2.01));assertEquals(ActionStatus.CANCELLED,new TimedAction(60,2).damage());assertEquals(ActionStatus.CANCELLED,new TimedAction(60,2).weaponSwitch());}
}
