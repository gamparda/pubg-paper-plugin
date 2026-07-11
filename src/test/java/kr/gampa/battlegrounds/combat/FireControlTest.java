package kr.gampa.battlegrounds.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FireControlTest {
  @Test void holdingSingleModeProducesOnlyOneShotUntilInputGap(){
    FireControl control=new FireControl(6);
    assertTrue(control.signal("single",0,2,600));
    assertFalse(control.signal("single",4,2,600));
    assertFalse(control.signal("single",8,2,600));
    assertFalse(control.signal("single",12,2,600));
    assertTrue(control.signal("single",19,2,600));
  }
  @Test void automaticModeUsesConfiguredRpmInsteadOfInputEventFrequency(){
    FireControl control=new FireControl(6);
    assertTrue(control.signal("auto",0,2,800));
    int shots=1;
    for(int tick=1;tick<=20;tick++){
      if(tick%4==0)control.signal("auto",tick,2,800);
      if(control.tickAutomatic(tick,800))shots++;
    }
    assertTrue(shots>=13&&shots<=14,"800 RPM should produce about 13.3 shots/second, got "+shots);
  }
  @Test void automaticFireStopsWhenRightClickHeartbeatEnds(){
    FireControl control=new FireControl(6);control.signal("auto",0,2,600);
    int late=0;for(int tick=1;tick<=20;tick++)if(control.tickAutomatic(tick,600)&&tick>6)late++;
    assertEquals(0,late);
  }
}
