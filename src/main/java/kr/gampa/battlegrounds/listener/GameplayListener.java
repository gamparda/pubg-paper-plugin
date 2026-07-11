package kr.gampa.battlegrounds.listener;

import java.util.*;
import kr.gampa.battlegrounds.BattlegroundsPlugin;
import kr.gampa.battlegrounds.combat.*;
import kr.gampa.battlegrounds.item.ItemRegistry;
import kr.gampa.battlegrounds.system.PlayerSystemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.util.*;

public final class GameplayListener implements Listener {
  private final BattlegroundsPlugin plugin; private final ItemRegistry items; private final PlayerSystemManager systems; private final DamageCalculator calculator=new DamageCalculator(); private final Map<UUID,Map<String,Long>> cooldowns=new HashMap<>();
  public GameplayListener(BattlegroundsPlugin plugin,ItemRegistry items,PlayerSystemManager systems){this.plugin=plugin;this.items=items;this.systems=systems;}

  @EventHandler(priority=EventPriority.HIGH)
  public void onInteract(PlayerInteractEvent event){
    if(event.getHand()!=EquipmentSlot.HAND)return;ItemStack held=event.getItem();String id=items.itemId(held);if(id==null)return;String category=items.category(id);
    if(event.getAction().isRightClick()){
      if("weapon".equals(category)){event.setCancelled(true);trigger(event.getPlayer(),held,id);}
      else if(!"ammo".equals(category)&&!"armor".equals(category)){event.setCancelled(true);systems.use(event.getPlayer(),held,id);}
    }else if(event.getAction().isLeftClick()&&"weapon".equals(category)){event.setCancelled(true);if(event.getPlayer().isSneaking())cycleMode(event.getPlayer(),held,id);else reload(event.getPlayer(),held,id);}
  }

  private void trigger(Player shooter,ItemStack gun,String id){
    ConfigurationSection c=items.definition(id);long now=System.currentTimeMillis(),ready=cooldowns.computeIfAbsent(shooter.getUniqueId(),k->new HashMap<>()).getOrDefault(id,0L);if(now<ready)return;String mode=gun.getItemMeta().getPersistentDataContainer().getOrDefault(plugin.fireModeKey(),PersistentDataType.STRING,"single");int rounds=gun.getItemMeta().getPersistentDataContainer().getOrDefault(plugin.roundsKey(),PersistentDataType.INTEGER,0);int shots=WeaponMechanics.shotsForTrigger(mode,c.getInt("burst-size",3),rounds);if(shots<=0){shootOnce(shooter,gun,id);return;}int interval=c.getInt("burst-interval-ticks",2),slot=shooter.getInventory().getHeldItemSlot();cooldowns.get(shooter.getUniqueId()).put(id,now+Math.max(c.getInt("cooldown-ticks",3),shots*interval)*50L);
    for(int i=0;i<shots;i++){int delay=i*interval;Bukkit.getScheduler().runTaskLater(plugin,()->{if(shooter.getInventory().getHeldItemSlot()==slot&&id.equals(items.itemId(shooter.getInventory().getItemInMainHand())))shootOnce(shooter,shooter.getInventory().getItemInMainHand(),id);},delay);}
  }

  private void shootOnce(Player shooter,ItemStack gun,String id){
    if(!id.equals(items.itemId(gun))||!shooter.isOnline())return;ConfigurationSection c=items.definition(id);boolean melee=c.getBoolean("melee");ItemMeta meta=gun.getItemMeta();int rounds=meta.getPersistentDataContainer().getOrDefault(plugin.roundsKey(),PersistentDataType.INTEGER,0);
    String category=c.getString("category","");boolean stackConsumed=category.equals("throwable");if(!melee&&!stackConsumed&&rounds<=0){shooter.playSound(shooter,Sound.BLOCK_LEVER_CLICK,.8f,1.7f);shooter.sendActionBar(Component.text("재장전 필요 — 좌클릭"));return;}
    if(!melee&&!stackConsumed){meta.getPersistentDataContainer().set(plugin.roundsKey(),PersistentDataType.INTEGER,rounds-1);gun.setItemMeta(meta);}
    if(category.equals("throwable")||category.equals("launcher")||category.equals("flare")){launchProjectile(shooter,c,id);if(stackConsumed)gun.subtract();return;}
    shooter.getWorld().playSound(shooter.getLocation(),melee?Sound.ENTITY_PLAYER_ATTACK_SWEEP:Sound.ENTITY_FIREWORK_ROCKET_BLAST,melee?1f:2f,melee?1f:.8f);
    int pellets=c.getInt("pellets",1);List<ShotOffset> pattern=ShotPattern.create(pellets,c.getDouble("spread-degrees",0),System.nanoTime());WeaponStats weapon=weapon(id,c);
    for(ShotOffset offset:pattern){org.bukkit.util.Vector direction=shooter.getEyeLocation().getDirection().clone().rotateAroundY(Math.toRadians(offset.yawDegrees()));org.bukkit.util.Vector right=direction.clone().crossProduct(new org.bukkit.util.Vector(0,1,0)).normalize();direction.rotateAroundAxis(right,Math.toRadians(offset.pitchDegrees()));resolveHit(shooter,direction,c,weapon);}
    int left=melee?0:gun.getItemMeta().getPersistentDataContainer().getOrDefault(plugin.roundsKey(),PersistentDataType.INTEGER,0);shooter.sendActionBar(Component.text(melee?id:"탄약 "+left+" / "+c.getInt("magazine")));
  }

  private void resolveHit(Player shooter,org.bukkit.util.Vector direction,ConfigurationSection c,WeaponStats weapon){
    double range=c.getDouble("max-range");RayTraceResult ray=shooter.getWorld().rayTrace(shooter.getEyeLocation(),direction,range,FluidCollisionMode.NEVER,true,.25,e->e instanceof LivingEntity&&e!=shooter);if(ray==null||!(ray.getHitEntity() instanceof LivingEntity target))return;
    if(target instanceof Player victim&&!plugin.getConfig().getBoolean("combat.friendly-fire")&&sameTeam(shooter,victim))return;double distance=shooter.getEyeLocation().distance(ray.getHitPosition().toLocation(shooter.getWorld()));double ratio=(ray.getHitPosition().getY()-target.getBoundingBox().getMinY())/Math.max(.01,target.getBoundingBox().getHeight());HitLocation hit=ratio>=plugin.getConfig().getDouble("combat.head-threshold",.72)?HitLocation.HEAD:(ratio<.35?HitLocation.LIMB:HitLocation.BODY);
    if(target instanceof Player victim&&ratio<.55&&hasItem(victim,"frying-pan")&&victim.getLocation().getDirection().setY(0).dot(shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).setY(0).normalize())<-.3){victim.getWorld().playSound(victim,Sound.BLOCK_ANVIL_LAND,1f,1.8f);return;}
    ItemStack armorItem=armorFor(target,hit);DamageResult damage=calculator.calculate(weapon,hit,distance,armorStats(armorItem));double before=target.getHealth();target.setNoDamageTicks(0);target.damage(damage.healthDamage()/plugin.getConfig().getDouble("combat.health-scale",5),shooter);if(target.getHealth()<before)damageArmor(armorItem,damage.armorDamage());target.getWorld().spawnParticle(Particle.CRIT,ray.getHitPosition().getX(),ray.getHitPosition().getY(),ray.getHitPosition().getZ(),5,.1,.1,.1,.1);if(hit==HitLocation.HEAD)shooter.sendActionBar(Component.text("헤드샷! 피해 "+Math.round(damage.healthDamage())));
  }

  private void launchProjectile(Player shooter,ConfigurationSection c,String id){
    Snowball projectile=shooter.launchProjectile(Snowball.class,shooter.getEyeLocation().getDirection().multiply(c.getDouble("projectile-speed",1.4)));projectile.getPersistentDataContainer().set(plugin.itemIdKey(),PersistentDataType.STRING,id);projectile.setItem(new ItemStack(c.getString("effect","").equals("smoke")?Material.GRAY_DYE:Material.FIRE_CHARGE));
  }

  @EventHandler public void projectileHit(ProjectileHitEvent event){
    if(!(event.getEntity() instanceof Snowball ball))return;String id=ball.getPersistentDataContainer().get(plugin.itemIdKey(),PersistentDataType.STRING);if(id==null)return;ConfigurationSection c=items.definition(id);if(c==null)return;Location at=ball.getLocation();String effect=c.getString("effect","");double radius=c.getDouble("explosive-radius",6),damage=c.getDouble("base-damage",0)/plugin.getConfig().getDouble("combat.health-scale",5);
    switch(effect){
      case "smoke" -> smoke(at,(int)radius);
      case "flash" -> {for(Entity e:at.getWorld().getNearbyEntities(at,radius,radius,radius))if(e instanceof Player p&&p.hasLineOfSight(ball)){p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,100,1));p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,80,0));}}
      case "fire" -> {at.getBlock().setType(Material.FIRE);areaDamage(at,radius,damage,ball.getShooter());}
      case "bluezone" -> new org.bukkit.scheduler.BukkitRunnable(){int runs=0;public void run(){if(runs++>=10){cancel();return;}areaDamage(at,radius,damage,ball.getShooter());at.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,at,80,radius/2,1,radius/2,.02);}}.runTaskTimer(plugin,0,20);
      case "flare" -> {Firework fw=at.getWorld().spawn(at,Firework.class);fw.detonate();}
      default -> {areaDamage(at,radius,damage,ball.getShooter());at.getWorld().spawnParticle(Particle.EXPLOSION,at,1);at.getWorld().playSound(at,Sound.ENTITY_GENERIC_EXPLODE,2f,.8f);}
    }
  }

  private void smoke(Location at,int radius){for(int i=0;i<15;i++)Bukkit.getScheduler().runTaskLater(plugin,()->at.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE,at.clone().add(0,1,0),180,radius/2d,2,radius/2d,.01),i*20L);}
  private void areaDamage(Location at,double radius,double damage,Object source){for(Entity e:at.getWorld().getNearbyEntities(at,radius,radius,radius))if(e instanceof LivingEntity living){if(source instanceof Player attacker&&living instanceof Player victim&&!plugin.getConfig().getBoolean("combat.friendly-fire")&&sameTeam(attacker,victim))continue;double scaled=damage*Math.max(0,1-e.getLocation().distance(at)/radius);if(scaled>0)living.damage(scaled,source instanceof Entity entity?entity:null);}}
  private WeaponStats weapon(String id,ConfigurationSection c){return new WeaponStats(id,c.getDouble("base-damage"),c.getDouble("falloff-start"),c.getDouble("max-range"),c.getDouble("minimum-damage-multiplier"),c.getDouble("head-multiplier"),c.getDouble("body-multiplier"),c.getDouble("limb-multiplier"));}
  private void cycleMode(Player p,ItemStack gun,String id){ConfigurationSection c=items.definition(id);List<String> modes=c.getStringList("fire-modes");if(modes.isEmpty())modes=List.of("single");ItemMeta meta=gun.getItemMeta();String current=meta.getPersistentDataContainer().getOrDefault(plugin.fireModeKey(),PersistentDataType.STRING,modes.getFirst());String next=new FireModeState(current,modes).cycle();meta.getPersistentDataContainer().set(plugin.fireModeKey(),PersistentDataType.STRING,next);gun.setItemMeta(meta);p.sendActionBar(Component.text("발사 모드: "+next));}
  private void reload(Player player,ItemStack gun,String id){ConfigurationSection c=items.definition(id);String ammoId=c.getString("ammo-id");if(ammoId==null||ammoId.equals("none")){player.sendActionBar(Component.text("재장전이 필요 없는 무기입니다."));return;}ItemMeta meta=gun.getItemMeta();int capacity=c.getInt("magazine"),current=Math.min(capacity,meta.getPersistentDataContainer().getOrDefault(plugin.roundsKey(),PersistentDataType.INTEGER,0));int needed=capacity-current,available=count(player,ammoId),used=Math.min(needed,available);if(used<=0){player.sendActionBar(Component.text(needed==0?"탄창이 가득 찼습니다.":"호환 탄약이 없습니다."));return;}remove(player,ammoId,used);meta.getPersistentDataContainer().set(plugin.roundsKey(),PersistentDataType.INTEGER,current+used);gun.setItemMeta(meta);player.playSound(player,Sound.ITEM_ARMOR_EQUIP_IRON,1f,1.2f);player.sendActionBar(Component.text("재장전: "+(current+used)+" / "+capacity));}
  private ItemStack armorFor(LivingEntity target,HitLocation hit){if(target.getEquipment()==null)return null;return hit==HitLocation.HEAD?target.getEquipment().getHelmet():hit==HitLocation.BODY?target.getEquipment().getChestplate():null;}
  private ArmorStats armorStats(ItemStack item){String id=items.itemId(item);if(id==null||!"armor".equals(items.category(id)))return null;ConfigurationSection c=items.definition(id);double durability=item.getItemMeta().getPersistentDataContainer().getOrDefault(plugin.armorDurabilityKey(),PersistentDataType.DOUBLE,0d);if(durability<=0&&id.startsWith("vest-"))return new ArmorStats(id,c.getDouble("broken-reduction",.20),Double.MAX_VALUE);return new ArmorStats(id,c.getDouble("reduction"),durability);}
  private void damageArmor(ItemStack item,double damage){if(item==null||damage<=0)return;String id=items.itemId(item);ItemMeta meta=item.getItemMeta();double left=Math.max(0,meta.getPersistentDataContainer().getOrDefault(plugin.armorDurabilityKey(),PersistentDataType.DOUBLE,0d)-damage);if(left<=0&&id!=null&&id.startsWith("helmet-")){item.setAmount(0);return;}meta.getPersistentDataContainer().set(plugin.armorDurabilityKey(),PersistentDataType.DOUBLE,left);item.setItemMeta(meta);}
  private boolean sameTeam(Player a,Player b){var ta=a.getScoreboard().getEntryTeam(a.getName());return ta!=null&&ta.equals(b.getScoreboard().getEntryTeam(b.getName()));}
  private boolean hasItem(Player p,String id){for(ItemStack i:p.getInventory().getContents())if(id.equals(items.itemId(i)))return true;return false;}
  private int count(Player p,String id){int n=0;for(ItemStack i:p.getInventory().getContents())if(id.equals(items.itemId(i)))n+=i.getAmount();return n;}
  private void remove(Player p,String id,int amount){for(ItemStack i:p.getInventory().getContents()){if(!id.equals(items.itemId(i)))continue;int take=Math.min(amount,i.getAmount());i.subtract(take);amount-=take;if(amount==0)return;}}
}
