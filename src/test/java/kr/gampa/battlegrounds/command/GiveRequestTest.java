package kr.gampa.battlegrounds.command;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GiveRequestTest {
  @Test void parsesOptionalAmountAfterItemQuery(){
    assertEquals(new GiveRequest("akm",5),GiveRequest.parse(new String[]{"give","akm","5"}));
    assertEquals(new GiveRequest("m416",1),GiveRequest.parse(new String[]{"give","m416"}));
  }
  @Test void clampsAmountAndRejectsInvalidValues(){
    assertThrows(IllegalArgumentException.class,()->GiveRequest.parse(new String[]{"give","bandage","999"}));
    assertThrows(IllegalArgumentException.class,()->GiveRequest.parse(new String[]{"give","akm","zero"}));
    assertThrows(IllegalArgumentException.class,()->GiveRequest.parse(new String[]{"give","akm","0"}));
  }
}
