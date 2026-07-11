package kr.gampa.battlegrounds.survival;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class BoostGaugeTest {
 @Test void boostStacksToOneHundredAndDrainsTenPerThirtySeconds(){var g=new BoostGauge();g.add(40);g.add(60);g.elapseSeconds(30);assertEquals(90,g.percent(),.001);}
 @Test void tierDeterminesHealingAndSpeed(){var g=new BoostGauge();g.add(65);assertEquals(3,g.healPerEightSeconds(),.001);assertEquals(.025,g.speedBonus(),.001);g.add(35);assertEquals(4,g.healPerEightSeconds(),.001);assertEquals(.0625,g.speedBonus(),.001);}
}
