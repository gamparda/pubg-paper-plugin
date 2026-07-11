package kr.gampa.battlegrounds;

import java.util.*;
import kr.gampa.battlegrounds.command.GiveRequest;
import kr.gampa.battlegrounds.item.ItemRegistry;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BattlegroundsCommand implements CommandExecutor, TabCompleter {
  private final BattlegroundsPlugin plugin; private final ItemRegistry items;
  BattlegroundsCommand(BattlegroundsPlugin plugin,ItemRegistry items){this.plugin=plugin;this.items=items;}
  public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
    if(!sender.hasPermission("battlegrounds.admin")){sender.sendMessage("권한이 없습니다.");return true;}
    if(args.length==0||args[0].equalsIgnoreCase("status")){sender.sendMessage("Battlegrounds: 아이템 "+items.ids().size()+"개 로드됨");return true;}
    if(args[0].equalsIgnoreCase("reload")){plugin.reloadSystems();sender.sendMessage("Battlegrounds 설정을 다시 불러왔습니다.");return true;}
    if(args[0].equalsIgnoreCase("find")&&args.length>=2){var found=items.search(args[1]);sender.sendMessage(found.isEmpty()?"검색 결과 없음":"검색 결과: "+String.join(", ",found.stream().limit(20).toList()));return true;}
    if(args[0].equalsIgnoreCase("give")&&sender instanceof Player p){
      final GiveRequest request;try{request=GiveRequest.parse(args);}catch(IllegalArgumentException ex){sender.sendMessage("/bg give <ID/이름/별칭> [1~64]");return true;}
      var matches=items.search(request.query());if(matches.isEmpty()){sender.sendMessage("없는 아이템입니다. /bg find "+request.query());return true;}if(matches.size()>1){sender.sendMessage("여러 항목이 검색됨: "+String.join(", ",matches.stream().limit(15).toList()));return true;}
      String id=matches.getFirst();int remaining=request.amount();int delivered=0;
      while(remaining>0){ItemStack item=items.create(id);int stack="weapon".equals(items.category(id))?1:Math.min(remaining,item.getMaxStackSize());item.setAmount(stack);var leftovers=p.getInventory().addItem(item);int rejected=leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();delivered+=stack-rejected;remaining-=stack;if(rejected>0)break;}
      sender.sendMessage("지급: "+id+" × "+delivered+(delivered<request.amount()?" (인벤토리 공간 부족)":""));return true;
    }
    sender.sendMessage("/bg give <item> [수량] | /bg find <검색어> | /bg reload | /bg status");return true;
  }
  public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){
    if(args.length==1)return List.of("give","find","reload","status").stream().filter(x->x.startsWith(args[0].toLowerCase())).toList();
    if(args.length==2&&(args[0].equalsIgnoreCase("give")||args[0].equalsIgnoreCase("find"))){String q=args[1].toLowerCase();return items.ids().stream().filter(x->x.startsWith(q)||x.contains(q)).limit(50).toList();}
    if(args.length==3&&args[0].equalsIgnoreCase("give"))return List.of("1","5","10","30","64").stream().filter(x->x.startsWith(args[2])).toList();return List.of();
  }
}
