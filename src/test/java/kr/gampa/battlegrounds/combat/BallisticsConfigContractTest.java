package kr.gampa.battlegrounds.combat;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class BallisticsConfigContractTest {
  @Test void firearmsHaveVelocitySpreadAndDropConfiguration(){var c=YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));assertTrue(c.getBoolean("ballistics.enabled"));assertTrue(c.getDouble("ballistics.gravity-mps2")>0);assertTrue(c.getDouble("ballistics.trail-spacing-blocks")>0);assertTrue(c.getInt("ballistics.max-active-bullets")>=100);var weapons=c.getConfigurationSection("weapons");for(String id:weapons.getKeys(false)){String p="weapons."+id+".",category=c.getString(p+"category","");if(category.equals("melee")||category.equals("throwable")||category.equals("launcher")||category.equals("flare"))continue;assertTrue(c.getDouble(p+"bullet-speed-mps")>0,id);assertTrue(c.getDouble(p+"gravity-scale")>0,id);assertTrue(c.getDouble(p+"spread-degrees")>=0,id);}}
}
