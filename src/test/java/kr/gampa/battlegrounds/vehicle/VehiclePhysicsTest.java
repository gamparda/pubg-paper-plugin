package kr.gampa.battlegrounds.vehicle;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class VehiclePhysicsTest {
  private final VehicleSpec car=new VehicleSpec("dacia",VehicleType.LAND,117,82,24,4,1800,100,0.06,0.018,false);
  @Test void throttleAcceleratesButNeverExceedsConfiguredTopSpeed(){
    VehicleState state=VehicleState.fresh(car);for(int i=0;i<400;i++)state=VehiclePhysics.step(car,state,new VehicleInput(1,0,false,false),.05);
    assertTrue(state.speedKmh()<=117.0001);assertTrue(state.speedKmh()>110);
  }
  @Test void boostRaisesLimitAndConsumesFuelFaster(){
    VehicleState normal=VehicleState.fresh(car),boosted=normal;
    for(int i=0;i<100;i++){normal=VehiclePhysics.step(car,normal,new VehicleInput(1,0,false,false),.05);boosted=VehiclePhysics.step(car,boosted,new VehicleInput(1,0,true,false),.05);}
    assertTrue(boosted.speedKmh()>normal.speedKmh());assertTrue(boosted.fuel()<normal.fuel());
  }
  @Test void bicycleDoesNotConsumeFuel(){
    VehicleSpec bike=new VehicleSpec("mountain-bike",VehicleType.LAND,62,35,18,1,500,0,0.08,0,false);VehicleState state=VehicleState.fresh(bike);
    for(int i=0;i<100;i++)state=VehiclePhysics.step(bike,state,new VehicleInput(1,0,true,false),.05);
    assertEquals(0,state.fuel());
  }
  @Test void damageDestroysVehicleAtZeroDurability(){VehicleState state=VehicleState.fresh(car).damage(1800);assertTrue(state.destroyed());}
  @Test void watercraftLosesStrongSpeedOnLand(){VehicleSpec boat=new VehicleSpec("boat",VehicleType.WATER,90,65,18,6,1500,100,.05,.02,false);VehicleState state=new VehicleState(80,100,1500,false);assertTrue(VehiclePhysics.terrainSpeed(boat,state,false)<25);}
  @Test void gliderOnlyClimbsAboveTakeoffSpeed(){assertFalse(VehiclePhysics.canClimb(VehicleType.AIR,45,60));assertTrue(VehiclePhysics.canClimb(VehicleType.AIR,70,60));}
  @Test void collisionDamageIgnoresParkingBumpsAndScalesWithImpact(){assertEquals(0,VehiclePhysics.collisionDamage(10,1));assertTrue(VehiclePhysics.collisionDamage(80,1)>VehiclePhysics.collisionDamage(40,1));}
}
