package amata1219.hypering.economy.spigot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.hypering.economy.Database;
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

		Database.load(getConfig().getString("MySQL.host"), getConfig().getInt("MySQL.port"), getConfig().getString("MySQL.database"), getConfig().getString("MySQL.username"), getConfig().getString("MySQL.password"));

		if(serverName == ServerName.MAIN)
			Database.registerEconomyServer(serverName);

		commands.put("he", new CommandExecutor(){

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return true;
			}

		});

		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable(){
		Database.close();

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

}
