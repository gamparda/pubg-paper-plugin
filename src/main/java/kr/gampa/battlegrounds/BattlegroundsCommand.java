package kr.gampa.battlegrounds;

import java.util.*;
import kr.gampa.battlegrounds.command.GiveRequest;
import kr.gampa.battlegrounds.item.ItemRegistry;
import kr.gampa.battlegrounds.vehicle.VehicleManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BattlegroundsCommand implements CommandExecutor, TabCompleter {
  private final BattlegroundsPlugin plugin; private final ItemRegistry items;private final VehicleManager vehicles;
  BattlegroundsCommand(BattlegroundsPlugin plugin,ItemRegistry items,VehicleManager vehicles){this.plugin=plugin;this.items=items;this.vehicles=vehicles;}
  public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
    if(!sender.hasPermission("battlegrounds.admin")){sender.sendMessage("권한이 없습니다.");return true;}
    if(args.length==0||args[0].equalsIgnoreCase("status")){sender.sendMessage("Battlegrounds: 아이템 "+items.ids().size()+"개, 이동수단 "+vehicles.ids().size()+"개 로드됨");return true;}
    if(args[0].equalsIgnoreCase("reload")){plugin.reloadSystems();sender.sendMessage("Battlegrounds 설정을 다시 불러왔습니다.");return true;}
    if(args[0].equalsIgnoreCase("vehicle"))return vehicleCommand(sender,args);
    if(args[0].equalsIgnoreCase("find")&&args.length>=2){var found=items.search(args[1]);sender.sendMessage(found.isEmpty()?"검색 결과 없음":"검색 결과: "+String.join(", ",found.stream().limit(20).toList()));return true;}
    if(args[0].equalsIgnoreCase("give")&&sender instanceof Player p){
      final GiveRequest request;try{request=GiveRequest.parse(args);}catch(IllegalArgumentException ex){sender.sendMessage("/bg give <ID/이름/별칭> [1~64]");return true;}
      var matches=items.search(request.query());if(matches.isEmpty()){sender.sendMessage("없는 아이템입니다. /bg find "+request.query());return true;}if(matches.size()>1){sender.sendMessage("여러 항목이 검색됨: "+String.join(", ",matches.stream().limit(15).toList()));return true;}
      String id=matches.getFirst();int remaining=request.amount(),delivered=0;while(remaining>0){ItemStack item=items.create(id);int stack="weapon".equals(items.category(id))?1:Math.min(remaining,item.getMaxStackSize());item.setAmount(stack);var leftovers=p.getInventory().addItem(item);int rejected=leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();delivered+=stack-rejected;remaining-=stack;if(rejected>0)break;}sender.sendMessage("지급: "+id+" × "+delivered+(delivered<request.amount()?" (인벤토리 공간 부족)":""));return true;
    }
    sender.sendMessage("/bg give|find|vehicle|reload|status");return true;
  }
  private boolean vehicleCommand(CommandSender sender,String[] args){
    if(args.length<2){sender.sendMessage("/bg vehicle list|give|spawn");return true;}if(args[1].equalsIgnoreCase("list")){sender.sendMessage("이동수단: "+String.join(", ",vehicles.ids()));return true;}if(!(sender instanceof Player p)){sender.sendMessage("플레이어만 사용할 수 있습니다.");return true;}if(args.length<3||!vehicles.ids().contains(args[2].toLowerCase())){sender.sendMessage("이동수단 ID를 확인하세요: /bg vehicle list");return true;}String id=args[2].toLowerCase();
    if(args[1].equalsIgnoreCase("spawn")){vehicles.spawn(id,p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(3)));sender.sendMessage("소환: "+id);return true;}
    if(args[1].equalsIgnoreCase("give")){int amount=1;if(args.length>=4)try{amount=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.sendMessage("수량은 숫자여야 합니다.");return true;}if(amount<1||amount>64){sender.sendMessage("수량은 1~64입니다.");return true;}int given=0;for(int i=0;i<amount;i++){var left=p.getInventory().addItem(vehicles.spawnItem(id));if(!left.isEmpty())break;given++;}sender.sendMessage("차량 소환권 지급: "+id+" × "+given);return true;}
    sender.sendMessage("/bg vehicle list|give|spawn");return true;
  }
  public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){
    if(args.length==1)return List.of("give","find","vehicle","reload","status").stream().filter(x->x.startsWith(args[0].toLowerCase())).toList();
    if(args.length==2&&args[0].equalsIgnoreCase("vehicle"))return List.of("list","give","spawn").stream().filter(x->x.startsWith(args[1].toLowerCase())).toList();
    if(args.length==3&&args[0].equalsIgnoreCase("vehicle")&&(args[1].equalsIgnoreCase("give")||args[1].equalsIgnoreCase("spawn")))return vehicles.ids().stream().filter(x->x.startsWith(args[2].toLowerCase())).toList();
    if(args.length==4&&args[0].equalsIgnoreCase("vehicle")&&args[1].equalsIgnoreCase("give"))return List.of("1","2","5","10").stream().filter(x->x.startsWith(args[3])).toList();
    if(args.length==2&&(args[0].equalsIgnoreCase("give")||args[0].equalsIgnoreCase("find"))){String q=args[1].toLowerCase();return items.ids().stream().filter(x->x.startsWith(q)||x.contains(q)).limit(50).toList();}
    if(args.length==3&&args[0].equalsIgnoreCase("give"))return List.of("1","5","10","30","64").stream().filter(x->x.startsWith(args[2])).toList();return List.of();
  }
}
