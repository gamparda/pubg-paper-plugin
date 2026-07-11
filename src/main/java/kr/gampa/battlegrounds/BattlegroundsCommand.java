package kr.gampa.battlegrounds;
import java.util.*; import kr.gampa.battlegrounds.item.ItemRegistry; import org.bukkit.command.*; import org.bukkit.entity.Player; import org.bukkit.inventory.ItemStack;
public final class BattlegroundsCommand implements CommandExecutor, TabCompleter {
  private final BattlegroundsPlugin plugin; private final ItemRegistry items;
  BattlegroundsCommand(BattlegroundsPlugin plugin,ItemRegistry items){this.plugin=plugin;this.items=items;}
  public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
    if(!sender.hasPermission("battlegrounds.admin")){sender.sendMessage("권한이 없습니다.");return true;}
    if(args.length==0||args[0].equalsIgnoreCase("status")){sender.sendMessage("Battlegrounds: 아이템 "+items.ids().size()+"개 로드됨");return true;}
    if(args[0].equalsIgnoreCase("reload")){plugin.reloadSystems();sender.sendMessage("Battlegrounds 설정을 다시 불러왔습니다.");return true;}
    if(args[0].equalsIgnoreCase("give")&&sender instanceof Player p&&args.length>=2){ItemStack item=items.create(args[1]);if(item==null){sender.sendMessage("없는 아이템: "+args[1]);return true;} p.getInventory().addItem(item);sender.sendMessage("지급: "+args[1]);return true;}
    sender.sendMessage("/bg give <item> | /bg reload | /bg status");return true;
  }
  public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){if(args.length==1)return List.of("give","reload","status").stream().filter(x->x.startsWith(args[0].toLowerCase())).toList();if(args.length==2&&args[0].equalsIgnoreCase("give"))return items.ids().stream().filter(x->x.startsWith(args[1].toLowerCase())).toList();return List.of();}
}
