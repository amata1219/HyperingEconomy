package amata1219.hypering.economy.bungeecord;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import amata1219.hypering.economy.MySQL;
import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DebugCommand extends Command {

	public DebugCommand(){
		super("admin", "hypering.economy.admin");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)){
			send(sender, "ゲーム内から実行して下さい");
		}

		ServerName name = ServerName.valueOf(((ProxiedPlayer) sender).getServer().getInfo().getName());

		if(args.length == 0){
			sender.sendMessage(new TextComponent("/admin [d/top]"));
			return;
		}else if(args[0].equalsIgnoreCase("d")||args[0].equalsIgnoreCase("debug")||args[0].equalsIgnoreCase("status")){
			if(args.length == 1){
				sender.sendMessage(new TextComponent("/admin d [data/money/ticket/median]"));
				return;
			}else if(args[1].equalsIgnoreCase("data")){
				if(args.length == 2){
					sender.sendMessage(new TextComponent("/admin d data] [player]"));
					return;
				}

				PlayerData data = MySQL.getPlayerData(toUUID(args[2]));

				send(sender, args[2] + " のステータス");

				send(sender, "所持金: ¥" + data.getMoney(name));
				send(sender, "チケット: " + data.getTickets());
				send(sender, "チケットの価値" + data.getTicketAmounts());
				send(sender, "チケット1枚当たりの価値" + data.getAmountPerTicket());

			}else if(args[1].equalsIgnoreCase("money")){
				if(args.length == 2){
					sender.sendMessage(new TextComponent("/admin d money [add/remove/see] [player]"));
					return;
				}else if(args.length == 3){
					sender.sendMessage(new TextComponent("/admin d money [add/remove/see] [player]"));
					return;
				}

				PlayerData data = MySQL.getPlayerData(toUUID(args[3]));

				if(args[2].equalsIgnoreCase("see")){
					send(sender, args[3] + " の所持金は ¥" + data.getMoney(name) + " です。");
					return;
				}

				if(args.length == 4){
					sender.sendMessage(new TextComponent("/admin d money [add/remove] [player] [money]"));
					return;
				}

				long money = 0;

				try{
					money = Long.valueOf(args[4]);
				}catch(NumberFormatException e){
					sender.sendMessage(new TextComponent("/admin d money [add/remove] [player] [money]"));
					return;
				}

				if(args[2].equalsIgnoreCase("add")){
					data.addMoney(name, money, true);
					sender.sendMessage(new TextComponent(args[3] + " の所持金に ¥" + money + " 追加しました。"));
					return;
				}else if(args[2].equalsIgnoreCase("remove")){
					data.removeMoney(name, money, true);
					sender.sendMessage(new TextComponent(args[3] + " の所持金から ¥" + money + " 削除しました。"));
					return;
				}
			}else if(args[1].equalsIgnoreCase("ticket")){
				if(args.length == 2){
					sender.sendMessage(new TextComponent("/admin d ticket [add/remove/see] [player]"));
					return;
				}else if(args.length == 3){
					sender.sendMessage(new TextComponent("/admin d ticket [add/remove/see] [player]"));
					return;
				}

				PlayerData data = MySQL.getPlayerData(toUUID(args[3]));

				if(args[2].equalsIgnoreCase("see")){
					send(sender, args[3] + " の所持チケットは " + data.getMoney(name) + "枚 です。");
					return;
				}

				if(args.length == 4){
					sender.sendMessage(new TextComponent("/admin d ticket [add/remove] [player] [money]"));
					return;
				}

				long tickets = 0;

				try{
					tickets = Long.valueOf(args[4]);
				}catch(NumberFormatException e){
					sender.sendMessage(new TextComponent("/admin d ticket [add/remove] [player] [money]"));
					return;
				}

				if(args[2].equalsIgnoreCase("add")){
					data.addMoney(name, tickets, true);
					sender.sendMessage(new TextComponent(args[3] + " の所持チケットに " + tickets + "枚 追加しました。"));
					return;
				}else if(args[2].equalsIgnoreCase("remove")){
					data.removeMoney(name, tickets, true);
					sender.sendMessage(new TextComponent(args[3] + " の所持チケットから " + tickets + "枚 削除しました。"));
					return;
				}
			}else if(args[1].equalsIgnoreCase("median")){
				send(sender, name.toString() + ": " + BCManager.getManager().getMedian(name));
				return;
			}else if(args[1].equalsIgnoreCase("ticketprice")){
				send(sender, name.toString() + ": " + BCManager.getManager().getTicketPrice(name));
				return;
			}
		}else if(args[0].equalsIgnoreCase("top")){
			int number = 0;
			if(args.length == 1){
				number = 10;
			}else{
				try{
					number = Integer.valueOf(args[4]);
				}catch(NumberFormatException e){
					sender.sendMessage(new TextComponent("/admin top [number]"));
					return;
				}
			}

			String[] s = BCManager.getManager().getMoneyRanking(name).split(",");

			number = number > s.length ? s.length : number;

			send(sender, "所持金TOP" + number);

			for(int i = 0; i < number; i++){
				String[] data = s[i].split("-");
				send(sender, "1. " + data[0] + ": " + data[1]);
			}
		}
	}

	public void send(CommandSender sender, String s){
		sender.sendMessage(new TextComponent(ChatColor.GOLD + s));
	}

	public UUID toUUID(String name){
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
	}

}
