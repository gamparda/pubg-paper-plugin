package kr.gampa.battlegrounds.vehicle;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;import java.util.*;
import org.bukkit.Material;import org.bukkit.configuration.file.YamlConfiguration;import org.junit.jupiter.api.Test;

class VehicleCatalogContractTest {
  @Test void catalogContainsRequestedSixteenVehicles(){var c=YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));var vehicles=c.getConfigurationSection("vehicles");assertNotNull(vehicles);assertEquals(Set.of("motorcycle","scooter","tukshai","mountain-bike","snowmobile","dacia","mirado","pony-coupe","minibus","uaz","buggy","brdm","motor-glider","boat","aquarail","rubber-boat"),vehicles.getKeys(false));for(String id:vehicles.getKeys(false)){String p="vehicles."+id+".";assertNotNull(Material.matchMaterial(c.getString(p+"model-material")),id);assertNotNull(Material.matchMaterial(c.getString(p+"spawn-item")),id);assertTrue(c.getDouble(p+"top-speed-kmh")>0,id);assertTrue(c.getDouble(p+"durability")>0,id);assertTrue(c.getInt(p+"seats")>0,id);assertTrue(c.getString(p+"source-url","").startsWith("https://namu.wiki/w/"),id);}}
}
