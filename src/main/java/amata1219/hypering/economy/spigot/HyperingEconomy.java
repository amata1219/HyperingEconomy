package amata1219.hypering.economy.spigot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.hypering.economy.Money;
import amata1219.hypering.economy.SQL;
import amata1219.hypering.economy.Saver;
import amata1219.hypering.economy.ServerName;
import net.milkbowl.vault.economy.Economy;

public class HyperingEconomy extends JavaPlugin implements Listener {

	private static HyperingEconomy plugin;

	private static ServerName serverName;

	private final Map<String, CommandExecutor> commands = new HashMap<>();

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		serverName = ServerName.valueOf(getConfig().getString("Aliases").toUpperCase());

		SQL.enable();

		commands.put("he", new CommandExecutor(){

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return true;
			}

		});

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

		getServer().getServicesManager().register(Economy.class, VaultEconomy.getInstance(), this, ServicePriority.Normal);
	}

	@EventHandler
	public void onEnable(PluginEnableEvent e){
		if(!e.getPlugin().getName().equals("Vault"))
			return;

		loadVaultEconomy();

		PluginEnableEvent.getHandlerList().unregister((JavaPlugin) this);
	}

	/*@EventHandler
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

	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		SQL.getSQL().updateLastPlayed(e.getPlayer().getUniqueId());
	}*/

}
