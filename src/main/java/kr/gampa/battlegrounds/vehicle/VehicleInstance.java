package kr.gampa.battlegrounds.vehicle;

import java.util.*;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.*;

final class VehicleInstance {
  final UUID id; final String specId; final ArmorStand root; final List<Entity> visuals=new ArrayList<>(); final List<ArmorStand> seats=new ArrayList<>();
  VehicleState state; VehicleInput input=new VehicleInput(0,0,false,false); float heading; boolean exploding;Location lastLocation;double lastCommandedKmh;long lastCollisionTick;
  VehicleInstance(String specId,ArmorStand root,VehicleState state){this.id=root.getUniqueId();this.specId=specId;this.root=root;this.state=state;this.heading=root.getLocation().getYaw();this.lastLocation=root.getLocation();}
  Player driver(){return passenger(seats.isEmpty()?null:seats.getFirst());}
  int playerCount(){return (int)seats.stream().filter(s->passenger(s)!=null).count();}
  ArmorStand freeSeat(){return seats.stream().filter(s->passenger(s)==null).findFirst().orElse(null);}
  boolean isOccupant(Entity entity){return seats.stream().anyMatch(s->s.getPassengers().contains(entity));}
  private Player passenger(ArmorStand seat){return seat==null?null:seat.getPassengers().stream().filter(Player.class::isInstance).map(Player.class::cast).findFirst().orElse(null);}
  void updateInput(Input in){double throttle=(in.isForward()?1:0)-(in.isBackward()?1:0),steer=(in.isRight()?1:0)-(in.isLeft()?1:0);input=new VehicleInput(throttle,steer,in.isSprint(),in.isSneak());}
}
