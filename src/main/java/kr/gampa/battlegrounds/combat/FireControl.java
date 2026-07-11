package kr.gampa.battlegrounds.combat;

/** Converts noisy right-click heartbeats into semi-auto edges or RPM-paced automatic shots. */
public final class FireControl {
  private final int releaseGraceTicks;
  private long lastSignalTick=Long.MIN_VALUE/2;
  private long lastManualShotTick=Long.MIN_VALUE/2;
  private boolean latched;
  private double automaticAccumulator;

  public FireControl(int releaseGraceTicks){if(releaseGraceTicks<1)throw new IllegalArgumentException();this.releaseGraceTicks=releaseGraceTicks;}

  public boolean signal(String mode,long tick,int minimumIntervalTicks,double rpm){
    boolean released=tick-lastSignalTick>releaseGraceTicks;
    if(released){latched=false;automaticAccumulator=0;}
    lastSignalTick=tick;
    if("auto".equals(mode)){
      if(!latched){latched=true;return true;}
      return false;
    }
    if(!latched&&tick-lastManualShotTick>=Math.max(1,minimumIntervalTicks)){
      latched=true;lastManualShotTick=tick;return true;
    }
    return false;
  }

  public boolean tickAutomatic(long tick,double rpm){
    if(!latched||tick-lastSignalTick>releaseGraceTicks)return false;
    automaticAccumulator+=Math.max(0,rpm)/1200.0;
    if(automaticAccumulator>=1){automaticAccumulator-=1;return true;}
    return false;
  }

  public boolean isHeld(long tick){return latched&&tick-lastSignalTick<=releaseGraceTicks;}
}
