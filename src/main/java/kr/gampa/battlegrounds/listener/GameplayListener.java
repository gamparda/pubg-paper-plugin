package kr.gampa.battlegrounds.listener;

import kr.gampa.battlegrounds.BattlegroundsPlugin;
import kr.gampa.battlegrounds.combat.*;
import kr.gampa.battlegrounds.item.ItemRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

public final class GameplayListener implements Listener {
  private final BattlegroundsPlugin plugin; private final ItemRegistry items; private final DamageCalculator calculator=new DamageCalculator();
  public GameplayListener(BattlegroundsPlugin plugin,ItemRegistry items){this.plugin=plugin;this.items=items;}

  @EventHandler(priority=EventPriority.HIGH)
  public void onInteract(PlayerInteractEvent event){
    if(event.getHand()!=EquipmentSlot.HAND) return;
    ItemStack held=event.getItem(); String id=items.itemId(held); if(id==null)return;
    String category=items.category(id);
    if(event.getAction().isRightClick()){
      if("weapon".equals(category)){event.setCancelled(true);fire(event.getPlayer(),held,id);}
      else if("healing".equals(category)){event.setCancelled(true);heal(event.getPlayer(),held,id);}
    } else if(event.getAction().isLeftClick()&&"weapon".equals(category)){event.setCancelled(true);reload(event.getPlayer(),held,id);}
  }

  private void fire(Player shooter,ItemStack gun,String id){
    ConfigurationSection c=items.definition(id); ItemMeta meta=gun.getItemMeta(); int rounds=meta.getPersistentDataContainer().getOrDefault(plugin.roundsKey(),PersistentDataType.INTEGER,0);
    if(rounds<=0){shooter.playSound(shooter,Sound.BLOCK_LEVER_CLICK,0.8f,1.7f);shooter.sendActionBar(Component.text("재장전 필요 — 좌클릭"));return;}
    if(shooter.hasCooldown(gun.getType()))return;
    meta.getPersistentDataContainer().set(plugin.roundsKey(),PersistentDataType.INTEGER,rounds-1);gun.setItemMeta(meta);shooter.setCooldown(gun.getType(),c.getInt("cooldown-ticks",3));
    shooter.getWorld().playSound(shooter.getLocation(),Sound.ENTITY_FIREWORK_ROCKET_BLAST,2f,0.8f);
    double range=c.getDouble("max-range");
    RayTraceResult ray=shooter.getWorld().rayTrace(shooter.getEyeLocation(),shooter.getEyeLocation().getDirection(),range,FluidCollisionMode.NEVER,true,.25,e->e instanceof LivingEntity&&e!=shooter);
    if(ray==null||!(ray.getHitEntity() instanceof LivingEntity target)) {shooter.sendActionBar(Component.text("탄약 "+(rounds-1)+" / "+c.getInt("magazine")));return;}
    if(target instanceof Player victim&&!plugin.getConfig().getBoolean("combat.friendly-fire")&&sameTeam(shooter,victim))return;
    double distance=shooter.getEyeLocation().distance(ray.getHitPosition().toLocation(shooter.getWorld()));
    double ratio=(ray.getHitPosition().getY()-target.getBoundingBox().getMinY())/Math.max(.01,target.getBoundingBox().getHeight());
    HitLocation hit=ratio>=plugin.getConfig().getDouble("combat.head-threshold",.72)?HitLocation.HEAD:(ratio<.35?HitLocation.LIMB:HitLocation.BODY);
    WeaponStats weapon=new WeaponStats(id,c.getDouble("base-damage"),c.getDouble("falloff-start"),range,c.getDouble("minimum-damage-multiplier"),c.getDouble("head-multiplier"),c.getDouble("body-multiplier"),c.getDouble("limb-multiplier"));
    ItemStack armorItem=armorFor(target,hit); ArmorStats armor=armorStats(armorItem);
    DamageResult damage=calculator.calculate(weapon,hit,distance,armor); damageArmor(armorItem,damage.armorDamage()); target.setNoDamageTicks(0); target.damage(damage.healthDamage(),shooter);
    target.getWorld().spawnParticle(Particle.CRIT,ray.getHitPosition().getX(),ray.getHitPosition().getY(),ray.getHitPosition().getZ(),8,.1,.1,.1,.1);
    shooter.sendActionBar(Component.text((hit==HitLocation.HEAD?"헤드샷! ":"")+"피해 "+Math.round(damage.healthDamage())+" | 탄약 "+(rounds-1)));
  }

  private void reload(Player player,ItemStack gun,String id){
    ConfigurationSection c=items.definition(id); String ammoId=c.getString("ammo-id"); if(ammoId==null)return;
    ItemMeta meta=gun.getItemMeta();int capacity=c.getInt("magazine"),current=Math.min(capacity,meta.getPersistentDataContainer().getOrDefault(plugin.roundsKey(),PersistentDataType.INTEGER,0));int needed=capacity-current,available=count(player,ammoId),used=Math.min(needed,available);
    if(used<=0){player.sendActionBar(Component.text(needed==0?"탄창이 가득 찼습니다.":"호환 탄약이 없습니다."));return;}
    remove(player,ammoId,used);meta.getPersistentDataContainer().set(plugin.roundsKey(),PersistentDataType.INTEGER,current+used);gun.setItemMeta(meta);player.playSound(player,Sound.ITEM_ARMOR_EQUIP_IRON,1f,1.2f);player.sendActionBar(Component.text("재장전: "+(current+used)+" / "+capacity));
  }

  private void heal(Player p,ItemStack item,String id){ConfigurationSection c=items.definition(id);double cap=c.getDouble("cap"),before=p.getHealth();if(before>=Math.min(cap,p.getMaxHealth())){p.sendActionBar(Component.text("지금은 사용할 수 없습니다."));return;}p.setHealth(Math.min(Math.min(cap,p.getMaxHealth()),before+c.getDouble("heal")));item.subtract();p.playSound(p,Sound.ITEM_HONEY_BOTTLE_DRINK,1f,1f);p.sendActionBar(Component.text("체력 회복: "+Math.round(before)+" → "+Math.round(p.getHealth())));}
  private ItemStack armorFor(LivingEntity target,HitLocation hit){if(!(target.getEquipment()!=null))return null;return hit==HitLocation.HEAD?target.getEquipment().getHelmet():target.getEquipment().getChestplate();}
  private ArmorStats armorStats(ItemStack item){String id=items.itemId(item);if(id==null||!"armor".equals(items.category(id)))return null;ConfigurationSection c=items.definition(id);double durability=item.getItemMeta().getPersistentDataContainer().getOrDefault(plugin.armorDurabilityKey(),PersistentDataType.DOUBLE,0d);return new ArmorStats(id,c.getDouble("reduction"),durability);}
  private void damageArmor(ItemStack item,double damage){if(item==null||damage<=0)return;ItemMeta meta=item.getItemMeta();double left=Math.max(0,meta.getPersistentDataContainer().getOrDefault(plugin.armorDurabilityKey(),PersistentDataType.DOUBLE,0d)-damage);if(left<=0){item.setAmount(0);return;}meta.getPersistentDataContainer().set(plugin.armorDurabilityKey(),PersistentDataType.DOUBLE,left);item.setItemMeta(meta);}
  private boolean sameTeam(Player a,Player b){var ta=a.getScoreboard().getEntryTeam(a.getName());return ta!=null&&ta.equals(b.getScoreboard().getEntryTeam(b.getName()));}
  private int count(Player p,String id){int n=0;for(ItemStack i:p.getInventory().getContents())if(id.equals(items.itemId(i)))n+=i.getAmount();return n;}
  private void remove(Player p,String id,int amount){for(ItemStack i:p.getInventory().getContents()){if(!id.equals(items.itemId(i)))continue;int take=Math.min(amount,i.getAmount());i.subtract(take);amount-=take;if(amount==0)return;}}
}
