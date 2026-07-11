package kr.gampa.battlegrounds.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BulletTrajectoryTest {
  @Test void gravityMakesProjectileDropEveryTick(){var b=new BulletTrajectory(new BallisticVector(0,0,0),new BallisticVector(100,0,0),9.81,0);var next=b.step(.05);assertTrue(next.position().y()<0);assertTrue(next.velocity().y()<0);}
  @Test void zeroGravityKeepsTrajectoryStraight(){var b=new BulletTrajectory(new BallisticVector(0,2,0),new BallisticVector(100,0,0),0,0);for(int i=0;i<20;i++)b=b.step(.05);assertEquals(2,b.position().y(),1e-9);assertEquals(100,b.position().x(),1e-6);}
  @Test void dragReducesVelocityWithoutReversingIt(){var b=new BulletTrajectory(new BallisticVector(0,0,0),new BallisticVector(200,0,0),9.81,.1);var next=b.step(1);assertTrue(next.velocity().x()<200);assertTrue(next.velocity().x()>0);}
  @Test void fasterBulletDropsLessAtSameHorizontalDistance(){double slow=BulletTrajectory.dropAtDistance(400,400,9.81,0);double fast=BulletTrajectory.dropAtDistance(400,800,9.81,0);assertTrue(slow>fast*3.9);}
  @Test void trailSamplesRespectConfiguredSpacing(){var points=BulletTrajectory.sampleSegment(new BallisticVector(0,0,0),new BallisticVector(10,0,0),2);assertEquals(5,points.size());assertEquals(2,points.getFirst().x(),1e-9);assertEquals(10,points.getLast().x(),1e-9);}
  @Test void finalSegmentIsClippedAtRemainingRange(){var end=BulletTrajectory.clipSegment(new BallisticVector(0,0,0),new BallisticVector(40,0,0),5);assertEquals(5,end.x(),1e-9);}
}
