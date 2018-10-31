package amata1219.hypering.economy.spigot;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import amata1219.hypering.economy.callback.Callback;
import amata1219.hypering.economy.callback.Result;

public class AdminCommand implements TabExecutor{

	private SHyperingEconomy plugin;

	public AdminCommand(SHyperingEconomy plugin){
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		SHyperingEconomyAPI api = plugin.getSSideHyperingEconomyAPI();
		if(!(sender instanceof Player)){
			warn(sender, "入力されたコマンドはゲーム内でのみ実行出来ます。");
			return true;
		}
		Player player = (Player) sender;
		if(args.length == 0){

		}else if(args[0].equalsIgnoreCase("money")){
			if(args.length == 1){

			}else if(args[1].equalsIgnoreCase("add")){
				if(args.length == 2){
					error(player, "対象プレイヤーと加算する金額を指定して下さい。");
					return true;
				}else if(!isExist(args[2])){
					error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}else if(args.length == 3){
					error(player, "加算する金額を指定して下さい。");
					return true;
				}else if(!isNumber(args[3])){
					error(player, "加算する金額は半角数字で入力して下さい。");
					return true;
				}
				api.addMoney(player, getUUID(args[2]), toNumber(args[3]));
				scs(player, args[2] + "の所持金に¥" + args[3] + "加算しました。");
			}else if(args[1].equalsIgnoreCase("substract")){
				if(args.length == 2){
					error(player, "対象プレイヤーと減算する金額を指定して下さい。");
					return true;
				}else if(!isExist(args[2])){
					error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}else if(args.length == 3){
					error(player, "減算する金額を指定して下さい。");
					return true;
				}else if(!isNumber(args[3])){
					error(player, "減算する金額は半角数字で入力して下さい。");
					return true;
				}
				api.removeMoney(player, getUUID(args[2]), toNumber(args[3]));
				scs(player, args[2] + "の所持金から¥" + args[3] + "減算しました。");
			}else if(args[1].equalsIgnoreCase("see")){
				if(args.length == 2){
					error(player, "対象プレイヤーを指定して下さい。");
					return true;
				}else if(!isExist(args[2])){
					error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}
				api.getMoney(player, getUUID(args[2]), new Callback<Result>(){

					@Override
					public void done(Result result) {
						scs(player, args[2] + "さんの所持金は¥" + result.getLong() + "です。");
					}

				});
			}
		}else if(args[0].equalsIgnoreCase("ticket")){
			if(args.length == 1){

			}else if(args[1].equalsIgnoreCase("add")){
				if(args.length == 2){
					error(player, "対象プレイヤーと追加する枚数、1枚当たりの価格(任意)を指定して下さい。");
					return true;
				}else if(!isExist(args[2])){
					error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}else if(args.length == 3){
					error(player, "追加する枚数を指定して下さい。");
					return true;
				}else if(!isNumber(args[3])){
					error(player, "追加する枚数は半角数字で入力して下さい。");
					return true;
				}else if(args.length == 4){
					api.addTicket(player, getUUID(args[2]), toNumber(args[3]));
					scs(player, args[2] + "に" + args[3] + "枚チケットを追加しました(価格: 相場と同じ)。");
				}else{
					if(!isNumber(args[4])){
						error(player, "1枚当たりの価格は半角数字で入力して下さい。");
						return true;
					}
					api.addTicket(player, getUUID(args[2]), toNumber(args[3]), toNumber(args[4]));
					scs(player, args[2] + "に" + args[3] + "枚チケットを追加しました(価格: " + args[4] + ")。");
				}
			}else if(args[1].equalsIgnoreCase("substract")){
				if(args.length == 2){
					error(player, "対象プレイヤーと削除する枚数を指定して下さい。");
					return true;
				}else if(!isExist(args[2])){
					error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}else if(args.length == 3){
					error(player, "削除する枚数を指定して下さい。");
					return true;
				}else if(!isNumber(args[3])){
					error(player, "削除する枚数は半角数字で入力して下さい。");
					return true;
				}
				api.removeTicket(player, getUUID(args[2]), toNumber(args[3]));
				scs(player, args[2] + "から" + args[3] + "枚チケットを削除しました。");
			}else if(args[1].equalsIgnoreCase("see")){
				if(args.length == 2){
					error(player, "対象プレイヤーを指定して下さい。");
					return true;
				}else if(!isExist(args[2])){
					error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}
				api.getNumberOfTickets(player, getUUID(args[2]), new Callback<Result>(){

					@Override
					public void done(Result result) {
						scs(player, args[2] + "さんのチケット所持数は" + result.getLong() + "枚です。");
					}

				});
			}
		}else if(args[0].equalsIgnoreCase("median")){
			api.getMedian(player, new Callback<Result>(){

				@Override
				public void done(Result result) {
					scs(player, "あなたがいるサーバーの現在の中央値は" + result.getLong() + "です。");
				}

			});
		}else if(args[0].equalsIgnoreCase("ticketprice")){
			api.getTicketPrice(player, new Callback<Result>(){

				@Override
				public void done(Result result) {
					scs(player, "あなたがいるサーバーのチケット相場価格は¥" + result.getLong() + "です。");
				}

			});
		}else if(args[0].equalsIgnoreCase("players")){
			api.getNumberOfPlayerDataLoaded(player, new Callback<Result>(){

				@Override
				public void done(Result result) {
					scs(player, "現在ロードされているプレイヤーデータ数は" + result.getLong() + "オブジェクトです。");
				}

			});
		}
		return true;
	}

	public void msg(CommandSender sender, String message){
		sender.sendMessage(message);
	}

	public void scs(CommandSender sender, String message){
		msg(sender, ChatColor.AQUA + message);
	}

	public void error(CommandSender sender, String message){
		msg(sender, ChatColor.RED + message);
	}

	public void warn(CommandSender sender, String message){
		msg(sender, ChatColor.DARK_RED + message);
	}

	public boolean isNumber(String s){
		try{
			Long.valueOf(s);
		}catch(NumberFormatException e){
			return false;
		}
		return true;
	}

	public long toNumber(String s){
		return Long.valueOf(s).longValue();
	}

	@SuppressWarnings("deprecation")
	public UUID getUUID(String name){
		return plugin.getServer().getOfflinePlayer(name).getUniqueId();
	}

	@SuppressWarnings("deprecation")
	public boolean isExist(String name){
		return plugin.getServer().getOfflinePlayer(name).hasPlayedBefore();
	}

}
