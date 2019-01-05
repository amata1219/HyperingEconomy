package amata1219.hypering.economy.spigot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.hypering.economy.Money;
import amata1219.hypering.economy.SQL;
import amata1219.hypering.economy.Saver;
import amata1219.hypering.economy.ServerName;
import net.milkbowl.vault.economy.Economy;

public class HyperingEconomy extends JavaPlugin implements Listener, CommandExecutor {

	private static HyperingEconomy plugin;

	private static ServerName serverName;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		serverName = ServerName.valueOf(getConfig().getString("Aliases").toUpperCase());

		SQL.enable();

		getCommand("he").setExecutor(this);

		getServer().getPluginManager().registerEvents(this, this);

		getServer().getOnlinePlayers().forEach(player -> {
			UUID uuid = player.getUniqueId();

			SQL sql = SQL.getSQL();
			if(sql.playerdata.containsKey(uuid))
				return;

			if(!sql.exist(uuid))
				sql.create(uuid);
			else
				sql.playerdata.put(uuid, Money.load(uuid));
		});
	}

	@Override
	public void onDisable(){
		SQL sql = SQL.getSQL();

		for(Entry<UUID, Money> entry : sql.playerdata.entrySet()){
			Money money = entry.getValue();
			if(!money.isChanged())
				continue;

			Saver.saveLong(entry.getKey(), sql.name, money.get());

			money.clear();
		}

		sql.cancel();
		sql.close();

		getServer().getServicesManager().unregisterAll(this);

		HandlerList.unregisterAll((JavaPlugin) this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0){

		}else if(args[0].equalsIgnoreCase("median")){
			sender.sendMessage("中央値: " + String.valueOf(SQL.getSQL().getMedian()));
		}else if(args[0].equalsIgnoreCase("demo")){
			HashMap<UUID, Money> map = new HashMap<>();
			SQL.getSQL().playerdata.forEach((k, v) -> map.put(k, v.clone()));
			try(Connection con = SQL.getSQL().getSource().getConnection();
					PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.ticketdata")){
				try(ResultSet result = statement.executeQuery()){
					while(result.next()){
						UUID uuid = UUID.fromString(result.getString("uuid"));
						if(!map.containsKey(uuid))
							continue;

						Money money = map.get(uuid);
						money.add(SQL.getSQL().getMedianChain().getTicketPrice(result.getLong("time")) * result.getInt("number"));
					}
					result.close();
				}
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}

			List<Money> list = new ArrayList<>();
			map.values().forEach(v -> {
				if(v.get() >= 500)
					list.add(v);
			});
			list.sort(new Comparator<Money>(){

				@Override
				public int compare(Money o1, Money o2) {
					return -Long.compare(o1.get(), o2.get());
				}

			});
			int size = list.size();
			sender.sendMessage("有効プレイヤー数: " + size);
			sender.sendMessage("計算結果: " + String.valueOf(list.get(size / 2).get()));
		}else if(args[0].equalsIgnoreCase("see")){
			if(args.length == 1)
				return true;

			OfflinePlayer p = getServer().getOfflinePlayer(args[1]);
			if(p == null || p.getName() == null)
				return true;

			sender.sendMessage(p.getName() + "の所持金は¥" + SQL.getSQL().getMoney(p.getUniqueId()) + "です。");
		}else if(args[0].equalsIgnoreCase("set")){
			if(args.length <= 2)
				return true;

			OfflinePlayer p = getServer().getOfflinePlayer(args[1]);
			if(p == null || p.getName() == null)
				return true;

			long l = 0;
			try{
				l = Long.valueOf(args[2]);
			}catch(NumberFormatException e){
				return true;
			}

			SQL.getSQL().setMoney(p.getUniqueId(), l);
			sender.sendMessage(p.getName() + "の所持金を¥" + l + "に書き換えました。");
		}
		return true;
	}

	public static HyperingEconomy getPlugin(){
		return plugin;
	}

	public static ServerName getServerName(){
		return serverName;
	}

	public static boolean isEconomyEnable(){
		return plugin.getConfig().getBoolean("EnableEconomy");
	}

	private void loadVaultEconomy(){
		VaultEconomy.load();

		getServer().getServicesManager().register(Economy.class, VaultEconomy.getInstance(), this, ServicePriority.Lowest);
	}

	@EventHandler
	public void onEnable(PluginEnableEvent e){
		if(!e.getPlugin().getName().equals("Vault"))
			return;

		loadVaultEconomy();

		PluginEnableEvent.getHandlerList().unregister((JavaPlugin) this);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		UUID uuid = e.getPlayer().getUniqueId();

		SQL sql = SQL.getSQL();
		if(sql.playerdata.containsKey(uuid))
			return;

		if(!sql.exist(uuid))
			sql.create(uuid);
		else
			sql.playerdata.put(uuid, Money.load(uuid));
	}

}
