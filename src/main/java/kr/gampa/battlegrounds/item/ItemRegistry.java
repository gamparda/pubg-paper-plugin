package kr.gampa.battlegrounds.item;

import java.util.*;
import kr.gampa.battlegrounds.BattlegroundsPlugin;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ItemRegistry {
  private final BattlegroundsPlugin plugin;
  private final Map<String, ConfigurationSection> definitions = new LinkedHashMap<>();
  public ItemRegistry(BattlegroundsPlugin plugin) { this.plugin = plugin; reload(); }
  public void reload() {
    definitions.clear();
    for (String category : List.of("weapons", "ammo", "healing", "boosters", "armor", "backpacks", "special")) {
      ConfigurationSection root = plugin.getConfig().getConfigurationSection(category);
      if (root != null) for (String id : root.getKeys(false)) definitions.put(id, root.getConfigurationSection(id));
    }
  }
  public Set<String> ids() { return Collections.unmodifiableSet(definitions.keySet()); }
  public ConfigurationSection definition(String id) { return definitions.get(id); }
  public String category(String id) {
    if (plugin.getConfig().contains("weapons."+id)) return "weapon";
    if (plugin.getConfig().contains("ammo."+id)) return "ammo";
    if (plugin.getConfig().contains("healing."+id)) return "healing";
    if (plugin.getConfig().contains("boosters."+id)) return "booster";
    if (plugin.getConfig().contains("armor."+id)) return "armor";
    if (plugin.getConfig().contains("backpacks."+id)) return "backpack";
    if (plugin.getConfig().contains("special."+id)) return "special";
    return null;
  }
  public ItemStack create(String id) {
    ConfigurationSection c = definitions.get(id); if (c == null) return null;
    Material material = Material.matchMaterial(c.getString("material", "STONE"));
    if (material == null) throw new IllegalArgumentException("Unknown material for "+id);
    ItemStack item = new ItemStack(material); ItemMeta meta = item.getItemMeta();
    meta.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(c.getString("name", id)));
    meta.getPersistentDataContainer().set(plugin.itemIdKey(), PersistentDataType.STRING, id);
    String category=category(id); meta.getPersistentDataContainer().set(plugin.itemTypeKey(), PersistentDataType.STRING, category);
    if (category.equals("weapon")) meta.getPersistentDataContainer().set(plugin.roundsKey(), PersistentDataType.INTEGER, c.getInt("magazine"));
    if (category.equals("weapon")) meta.getPersistentDataContainer().set(plugin.fireModeKey(), PersistentDataType.STRING, c.getStringList("fire-modes").isEmpty()?"single":c.getStringList("fire-modes").getFirst());
    if (category.equals("armor")) meta.getPersistentDataContainer().set(plugin.armorDurabilityKey(), PersistentDataType.DOUBLE, c.getDouble("durability"));
    if (category.equals("special") && c.contains("durability")) meta.getPersistentDataContainer().set(plugin.armorDurabilityKey(), PersistentDataType.DOUBLE, c.getDouble("durability"));
    if (category.equals("armor")) {
      meta.removeAttributeModifier(org.bukkit.attribute.Attribute.ARMOR);
      meta.removeAttributeModifier(org.bukkit.attribute.Attribute.ARMOR_TOUGHNESS);
    }
    item.setItemMeta(meta); return item;
  }
  public String itemId(ItemStack item) { if (item == null || !item.hasItemMeta()) return null; return item.getItemMeta().getPersistentDataContainer().get(plugin.itemIdKey(), PersistentDataType.STRING); }
  public List<String> search(String query) {String q=query.toLowerCase(Locale.ROOT);if(definitions.containsKey(q))return List.of(q);List<String> exact=new ArrayList<>(),prefix=new ArrayList<>(),contains=new ArrayList<>();for(var e:definitions.entrySet()){String id=e.getKey(),name=org.bukkit.ChatColor.stripColor(org.bukkit.ChatColor.translateAlternateColorCodes('&',e.getValue().getString("name",id))).toLowerCase(Locale.ROOT);List<String> aliases=e.getValue().getStringList("aliases");boolean aliasExact=aliases.stream().anyMatch(a->a.equalsIgnoreCase(q));if(id.equalsIgnoreCase(q)||name.equals(q)||aliasExact)exact.add(id);else if(id.startsWith(q)||name.startsWith(q)||aliases.stream().anyMatch(a->a.toLowerCase(Locale.ROOT).startsWith(q)))prefix.add(id);else if(id.contains(q)||name.contains(q)||aliases.stream().anyMatch(a->a.toLowerCase(Locale.ROOT).contains(q)))contains.add(id);}return !exact.isEmpty()?exact:!prefix.isEmpty()?prefix:contains;}
}
