package kr.gampa.battlegrounds.item;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*; import java.util.*;
import org.bukkit.Material; import org.bukkit.configuration.file.YamlConfiguration; import org.junit.jupiter.api.Test;

class WeaponCatalogContractTest {
  private YamlConfiguration config(){return YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));}
  @Test void catalogContainsAllAttachedWeaponsWithValidMaterialsAndSources(){
    var c=config();var weapons=c.getConfigurationSection("weapons");assertNotNull(weapons);assertEquals(92,weapons.getKeys(false).size());
    for(String id:weapons.getKeys(false)){String path="weapons."+id+".";assertNotNull(Material.matchMaterial(c.getString(path+"material")),id);assertTrue(c.getString(path+"source-url","").startsWith("https://namu.wiki/w/"),id);assertTrue(c.getDouble(path+"base-damage")>=0,id);assertTrue(c.getInt(path+"cooldown-ticks")>0,id);}
  }
  @Test void everyWeaponAmmoReferenceExists(){var c=config();var weapons=c.getConfigurationSection("weapons");for(String id:weapons.getKeys(false)){String ammo=c.getString("weapons."+id+".ammo-id");assertTrue(ammo.equals("none")||c.contains("ammo."+ammo),id+" -> "+ammo);}}
}
