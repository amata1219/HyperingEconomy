package amata1219.hypering.economy.spigot;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class SSideHyperingEconomy extends JavaPlugin{

	private static SSideHyperingEconomy plugin;
	private SSideManager manager;

	private HashMap<String, TabExecutor> commands;

	@Override
	public void onEnable(){
		plugin = this;
		saveDefaultConfig();
		manager = new SSideManager(plugin);
		getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", manager);
		getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		getServer().getPluginManager().registerEvents(manager, plugin);
		commands = new HashMap<String, TabExecutor>();
		commands.put("main", new MainCommand(plugin));
		commands.put("admin", new AdminCommand(plugin));
	}

	@Override
	public void onDisable(){

	}

	public static SSideHyperingEconomy getPlugin(){
		return plugin;
	}

	public SSideManager getManager(){
		return manager;
	}

	public SSideHyperingEconomyAPI getSSideHyperingEconomyAPI(){
		return manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		return commands.get(command.getName()).onCommand(sender, command, label, args);
	}

}
