package kr.gampa.battlegrounds.command;

public record GiveRequest(String query,int amount){
  public static GiveRequest parse(String[] args){
    if(args.length<2||!args[0].equalsIgnoreCase("give"))throw new IllegalArgumentException("usage");
    int amount=1;
    if(args.length>=3){
      try{amount=Integer.parseInt(args[2]);}catch(NumberFormatException e){throw new IllegalArgumentException("amount");}
      if(amount<=0||amount>64)throw new IllegalArgumentException("amount");
    }
    return new GiveRequest(args[1].toLowerCase(),amount);
  }
}
