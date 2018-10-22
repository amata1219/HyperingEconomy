package amata1219.hypering.economy.spigot;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import amata1219.hypering.economy.callback.Callback;

public class MainCommand implements TabExecutor{

	private SHyperingEconomy plugin;

	public MainCommand(SHyperingEconomy plugin){
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
		UUID uuid = player.getUniqueId();
		if(args.length == 0){

		}else if(args[0].equalsIgnoreCase("moneysee")){
			if(args.length == 1){
				api.getMoney(player, player.getUniqueId(), new Callback<Result>(){

					@Override
					public void done(Result result) {
						scs(player, "あなたの所持金は¥" + result.getLong() + "です。");
					}

				});
			}else{
				if(!isExist(args[1])){
					error(sender, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}
				api.getMoney(player, getUUID(args[1]), new Callback<Result>(){

					@Override
					public void done(Result result) {
						scs(player, args[1] + "さんの所持金は¥" + result.getLong() + "です。");
					}

				});
			}
		}else if(args[0].equalsIgnoreCase("ticketsee")){
			if(args.length == 1){
				api.getNumberOfTickets(player, uuid, new Callback<Result>(){

					@Override
					public void done(Result result) {
						scs(player, "あなたのチケット所持数は" + result.getLong() + "枚です。");
					}

				});
			}else{
				if(!isExist(args[1])){
					error(sender, "指定されたプレイヤーはオフラインまたは存在しません。");
					return true;
				}
				api.getNumberOfTickets(player, getUUID(args[1]), new Callback<Result>(){

					@Override
					public void done(Result result) {
						scs(player, args[1] + "さんのチケット所持数は" + result.getLong() + "枚です。");
					}

				});
			}
		}else if(args[0].equalsIgnoreCase("ranking")){
			api.getMoneyRanking(player, new Callback<Result>(){

				@Override
				public void done(Result result) {
					scs(player, "所持金ランキング(テキスト処理してない)" + result.getString());
				}

			});
		}else if(args[0].equalsIgnoreCase("sendmoney")){
			if(args.length == 1){
				error(player, "送金相手と金額を指定して下さい。");
				return true;
			}else if(!isExist(args[1])){
				error(player, "指定されたプレイヤーはオフラインまたは存在しません。");
				return true;
			}else if(args.length == 2){
				error(player, "送金額を指定して下さい。");
				return true;
			}else if(!isNumber(args[2])){
				error(player, "送金額は半角数字で指定して下さい。");
				return true;
			}
			api.sendMoney(player, player.getUniqueId(), getUUID(args[1]), toNumber(args[2]));
			scs(player, args[1] + "さんに¥" + args[2] + "送金しました。");
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
