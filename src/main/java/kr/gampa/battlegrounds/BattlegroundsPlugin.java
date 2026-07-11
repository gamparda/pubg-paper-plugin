package kr.gampa.battlegrounds;

import kr.gampa.battlegrounds.item.ItemRegistry;
import kr.gampa.battlegrounds.listener.GameplayListener;
import kr.gampa.battlegrounds.system.PlayerSystemManager;
import kr.gampa.battlegrounds.vehicle.VehicleManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class BattlegroundsPlugin extends JavaPlugin {
  private NamespacedKey itemIdKey,itemTypeKey,roundsKey,armorDurabilityKey,fireModeKey; private ItemRegistry items; private VehicleManager vehicles;
  @Override public void onEnable() {
    saveDefaultConfig(); itemIdKey=new NamespacedKey(this,"item_id"); itemTypeKey=new NamespacedKey(this,"item_type"); roundsKey=new NamespacedKey(this,"rounds"); armorDurabilityKey=new NamespacedKey(this,"armor_durability");fireModeKey=new NamespacedKey(this,"fire_mode");
    items=new ItemRegistry(this);vehicles=new VehicleManager(this,items); PlayerSystemManager systems=new PlayerSystemManager(this,items); getServer().getPluginManager().registerEvents(vehicles,this);getServer().getPluginManager().registerEvents(systems,this); getServer().getPluginManager().registerEvents(new GameplayListener(this,items,systems),this);
    BattlegroundsCommand command=new BattlegroundsCommand(this,items,vehicles); getCommand("battlegrounds").setExecutor(command); getCommand("battlegrounds").setTabCompleter(command);
    getLogger().info("Loaded "+items.ids().size()+" configurable Battlegrounds items.");
  }
  @Override public void onDisable(){if(vehicles!=null)vehicles.shutdown();}
  public void reloadSystems(){ reloadConfig(); items.reload();vehicles.reload(); }
  public NamespacedKey itemIdKey(){return itemIdKey;} public NamespacedKey itemTypeKey(){return itemTypeKey;} public NamespacedKey roundsKey(){return roundsKey;} public NamespacedKey armorDurabilityKey(){return armorDurabilityKey;} public NamespacedKey fireModeKey(){return fireModeKey;}
}
