package kr.gampa.battlegrounds.system;

import java.util.*;
import kr.gampa.battlegrounds.BattlegroundsPlugin;
import kr.gampa.battlegrounds.action.*;
import kr.gampa.battlegrounds.item.ItemRegistry;
import kr.gampa.battlegrounds.survival.BoostGauge;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

public final class PlayerSystemManager implements Listener {
  private record UseSession(TimedAction action,String itemId,Location origin,int slot,UUID reviveTarget,boolean consumedAtStart) {}
  private final BattlegroundsPlugin plugin; private final ItemRegistry items;
  private final Map<UUID,UseSession> uses=new HashMap<>(); private final Map<UUID,BoostGauge> boosts=new HashMap<>();
  private final Map<UUID,String> backpacks=new HashMap<>(); private final Map<UUID,Double> jammer=new HashMap<>(); private final Set<UUID> downed=new HashSet<>();
  private final Map<UUID,Integer> downedGeneration=new HashMap<>();
  public PlayerSystemManager(BattlegroundsPlugin plugin,ItemRegistry items){this.plugin=plugin;this.items=items;plugin.getServer().getScheduler().runTaskTimer(plugin,this::tick,1,1);}

  public boolean isDowned(Player player){return downed.contains(player.getUniqueId());}
  public double boost(Player player){return boosts.getOrDefault(player.getUniqueId(),new BoostGauge()).percent();}

  public void use(Player player,ItemStack item,String id){
    String category=items.category(id); ConfigurationSection c=items.definition(id); if(c==null)return;
    if("backpack".equals(category)){equipBackpack(player,item,id,c);return;}
    if("special".equals(category)&&id.equals("parachute")){if(player.getFallDistance()>=c.getDouble("minimum-fall-blocks",6)){player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,c.getInt("slow-falling-seconds",12)*20,0,false,false));player.sendActionBar(net.kyori.adventure.text.Component.text("낙하산 전개"));}return;}
    if(id.startsWith("ghillie-")){player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,20*30,0,false,false));consume(item);return;}
    if(id.equals("self-aed")&&!isDowned(player)){player.sendActionBar(net.kyori.adventure.text.Component.text("기절 상태에서만 사용할 수 있습니다."));return;}
    double seconds=c.getDouble("use-seconds",c.getDouble("revive-seconds",c.getDouble("deploy-seconds",0)));if(("healing".equals(category)||"booster".equals(category))&&has(player,"emt-gear"))seconds*=items.definition("emt-gear").getDouble("healing-speed-multiplier",.75); if(seconds<=0){player.sendActionBar(net.kyori.adventure.text.Component.text("이 장비는 다른 시스템과 연동해 사용합니다."));return;}
    boolean consumeNow=c.getBoolean("consume-on-start",false); if(consumeNow)consume(item);
    start(player,id,(long)Math.ceil(seconds*20),null,consumeNow);
  }

  public void startRevive(Player helper,Player target,ItemStack held){
    if(!isDowned(target)||uses.containsKey(helper.getUniqueId()))return;
    String id=items.itemId(held); boolean fast="hemostatic-device".equals(id); double seconds=fast?items.definition(id).getDouble("revive-seconds",1):plugin.getConfig().getDouble("downed.revive-seconds",10);
    if(fast)consume(held); start(helper,fast?id:"revive",(long)Math.ceil(seconds*20),target.getUniqueId(),fast);
  }

  private void start(Player p,String id,long ticks,UUID target,boolean consumed){cancel(p,false);uses.put(p.getUniqueId(),new UseSession(new TimedAction(ticks,plugin.getConfig().getDouble("consumables.movement-radius",2)),id,p.getLocation().clone(),p.getInventory().getHeldItemSlot(),target,consumed));p.sendActionBar(net.kyori.adventure.text.Component.text("사용 시작: "+id));}
  public void cancel(Player p,boolean notify){UseSession old=uses.remove(p.getUniqueId());if(old!=null&&notify)p.sendActionBar(net.kyori.adventure.text.Component.text("사용이 취소되었습니다."));}

  private void tick(){
    for(Player p:Bukkit.getOnlinePlayers()){
      BoostGauge gauge=boosts.get(p.getUniqueId()); if(gauge!=null){if(Bukkit.getCurrentTick()%60==0)gauge.elapseSeconds(3);if(Bukkit.getCurrentTick()%160==0&&p.getHealth()<p.getMaxHealth())p.setHealth(Math.min(p.getMaxHealth(),p.getHealth()+gauge.healPerEightSeconds()/scale()));p.setWalkSpeed((float)Math.min(1,.2*(1+gauge.speedBonus())));}
    }
    for(var entry:new ArrayList<>(uses.entrySet())){Player p=Bukkit.getPlayer(entry.getKey());if(p==null){uses.remove(entry.getKey());continue;}UseSession s=entry.getValue();if(s.action().tick(1)==ActionStatus.COMPLETED){uses.remove(entry.getKey());complete(p,s);}else p.sendActionBar(net.kyori.adventure.text.Component.text("사용 중... "+Math.round(s.action().progress()*100)+"%"));}
  }

  private void complete(Player p,UseSession s){
    if(s.reviveTarget()!=null){Player target=Bukkit.getPlayer(s.reviveTarget());if(target!=null&&target.getWorld().equals(p.getWorld())&&target.getLocation().distance(p.getLocation())<=3&&downed.remove(target.getUniqueId())){target.removePotionEffect(PotionEffectType.SLOWNESS);target.setHealth(Math.min(target.getMaxHealth(),plugin.getConfig().getDouble("downed.revive-health",10)/scale()));target.sendMessage("소생되었습니다.");}else p.sendMessage("대상이 너무 멀거나 소생할 수 없습니다.");return;}
    ConfigurationSection c=items.definition(s.itemId());if(c==null)return;ItemStack held=p.getInventory().getItem(s.slot());if(!s.consumedAtStart()&&!s.itemId().equals(items.itemId(held))){p.sendActionBar(net.kyori.adventure.text.Component.text("아이템이 없어 사용이 취소되었습니다."));return;}
    if(items.category(s.itemId()).equals("healing")||s.itemId().equals("combat-ready-kit")){double to=c.contains("heal-to")?c.getDouble("heal-to"):Math.min(c.getDouble("cap",75),p.getHealth()*scale()+c.getDouble("heal"));p.setHealth(Math.min(p.getMaxHealth(),to/scale()));}
    if(items.category(s.itemId()).equals("booster")||c.contains("boost"))boosts.computeIfAbsent(p.getUniqueId(),x->new BoostGauge()).add(c.getDouble("boost"));
    if(s.itemId().equals("self-aed")&&downed.remove(p.getUniqueId())){p.removePotionEffect(PotionEffectType.SLOWNESS);p.setHealth(Math.min(p.getMaxHealth(),c.getDouble("revive-health",10)/scale()));}
    if(s.itemId().equals("folding-shield"))deployShield(p,c);
    if(s.itemId().equals("all-in-one-repair-kit"))repairArmor(p);
    if(!s.consumedAtStart())consume(held);p.playSound(p,Sound.ENTITY_PLAYER_LEVELUP,.7f,1.5f);p.sendActionBar(net.kyori.adventure.text.Component.text("사용 완료: "+s.itemId()));
  }

  private void equipBackpack(Player p,ItemStack item,String id,ConfigurationSection c){String previous=backpacks.put(p.getUniqueId(),id);if(previous!=null)giveOrDrop(p,items.create(previous));consume(item);jammer.remove(p.getUniqueId());if(id.equals("jammer-backpack"))jammer.put(p.getUniqueId(),c.getDouble("energy",100));p.sendActionBar(net.kyori.adventure.text.Component.text("배낭 장착: "+id+" (용량 +"+c.getInt("capacity")+")"));}
  public double applyBlueZoneDamage(Player p,double damage){Double energy=jammer.get(p.getUniqueId());if(energy==null||energy<=0)return damage;double blocked=Math.min(energy,damage);jammer.put(p.getUniqueId(),energy-blocked);return damage-blocked;}
  public double capacity(Player p){double result=plugin.getConfig().getDouble("capacity.base",50);ItemStack chest=p.getInventory().getChestplate();String vest=items.itemId(chest);if(vest!=null&&items.definition(vest)!=null)result+=items.definition(vest).getDouble("capacity",0);String bag=backpacks.get(p.getUniqueId());if(bag!=null&&items.definition(bag)!=null)result+=items.definition(bag).getDouble("capacity",0);return result;}
  public double carriedWeight(Player p){double total=0;for(ItemStack stack:p.getInventory().getStorageContents()){String id=items.itemId(stack);if(id!=null&&items.definition(id)!=null)total+=items.definition(id).getDouble("weight",1)*stack.getAmount();}return total;}
  private void deployShield(Player p,ConfigurationSection c){Location at=p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(2)).getBlock().getLocation();if(!at.getBlock().isEmpty()||!at.clone().add(0,1,0).getBlock().isEmpty()){giveOrDrop(p,items.create("folding-shield"));p.sendMessage("평평하고 빈 공간에만 설치할 수 있습니다.");return;}at.getBlock().setType(Material.IRON_BARS);at.clone().add(0,1,0).getBlock().setType(Material.IRON_BARS);}
  private void repairArmor(Player p){for(ItemStack armor:new ItemStack[]{p.getInventory().getHelmet(),p.getInventory().getChestplate()}){String id=items.itemId(armor);if(id==null||items.definition(id)==null)continue;var meta=armor.getItemMeta();meta.getPersistentDataContainer().set(plugin.armorDurabilityKey(),org.bukkit.persistence.PersistentDataType.DOUBLE,items.definition(id).getDouble("durability"));armor.setItemMeta(meta);}}
  private void consume(ItemStack item){if(item!=null)item.subtract();}
  private double scale(){return plugin.getConfig().getDouble("combat.health-scale",5);}
  private void giveOrDrop(Player p,ItemStack item){var left=p.getInventory().addItem(item);for(ItemStack stack:left.values())p.getWorld().dropItemNaturally(p.getLocation(),stack);}
  private boolean has(Player p,String id){for(ItemStack i:p.getInventory().getContents())if(id.equals(items.itemId(i)))return true;return false;}

  @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void lethal(EntityDamageEvent e){if(!(e.getEntity() instanceof Player p)||isDowned(p))return;if(e.getFinalDamage()<p.getHealth())return;boolean teamAlive=p.getScoreboard().getEntryTeam(p.getName())!=null&&p.getScoreboard().getEntryTeam(p.getName()).getEntries().stream().anyMatch(n->{Player q=Bukkit.getPlayerExact(n);return q!=null&&q!=p&&!q.isDead();});if(!teamAlive&&!has(p,"self-aed"))return;e.setCancelled(true);p.setHealth(Math.min(p.getMaxHealth(),.2));downed.add(p.getUniqueId());int generation=downedGeneration.merge(p.getUniqueId(),1,Integer::sum);p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,plugin.getConfig().getInt("downed.bleedout-seconds",60)*20,6,false,false));p.sendMessage("기절했습니다. 팀원의 소생 또는 자가제세동기가 필요합니다.");Bukkit.getScheduler().runTaskLater(plugin,()->{if(downedGeneration.getOrDefault(p.getUniqueId(),0)==generation&&downed.remove(p.getUniqueId())&&p.isOnline())p.setHealth(0);},plugin.getConfig().getLong("downed.bleedout-seconds",60)*20);}
  @EventHandler(ignoreCancelled=true) public void damaged(EntityDamageEvent e){if(e.getEntity() instanceof Player p&&uses.containsKey(p.getUniqueId())&&plugin.getConfig().getBoolean("consumables.cancel-on-damage",true))cancel(p,true);}
  @EventHandler public void move(PlayerMoveEvent e){UseSession s=uses.get(e.getPlayer().getUniqueId());if(s!=null&&s.action().move(s.origin().distance(e.getTo()))==ActionStatus.CANCELLED)cancel(e.getPlayer(),true);}
  @EventHandler public void held(PlayerItemHeldEvent e){if(plugin.getConfig().getBoolean("consumables.cancel-on-item-switch",true))cancel(e.getPlayer(),true);}
  @EventHandler public void interact(PlayerInteractEntityEvent e){if(e.getRightClicked() instanceof Player target&&isDowned(target))startRevive(e.getPlayer(),target,e.getPlayer().getInventory().getItemInMainHand());}
  @EventHandler public void quit(PlayerQuitEvent e){UUID id=e.getPlayer().getUniqueId();uses.remove(id);boosts.remove(id);backpacks.remove(id);jammer.remove(id);downed.remove(id);downedGeneration.remove(id);e.getPlayer().setWalkSpeed(.2f);}
  @EventHandler public void death(PlayerDeathEvent e){downed.remove(e.getPlayer().getUniqueId());ItemStack chip=items.create("bluechip");if(chip!=null)e.getDrops().add(chip);}
  @EventHandler(ignoreCancelled=true) public void pickup(EntityPickupItemEvent e){if(!(e.getEntity() instanceof Player p)||!plugin.getConfig().getBoolean("capacity.enforce-on-pickup",true))return;ItemStack stack=e.getItem().getItemStack();String id=items.itemId(stack);if(id==null||items.definition(id)==null)return;double added=items.definition(id).getDouble("weight",1)*stack.getAmount();if(carriedWeight(p)+added>capacity(p)){e.setCancelled(true);p.sendActionBar(net.kyori.adventure.text.Component.text("소지 용량 부족: "+Math.round(carriedWeight(p))+" / "+Math.round(capacity(p))));}}
}
