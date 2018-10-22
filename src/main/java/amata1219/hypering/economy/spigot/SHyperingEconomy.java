package amata1219.hypering.economy.spigot;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class SHyperingEconomy extends JavaPlugin{

	private static SHyperingEconomy plugin;

	private HashMap<String, TabExecutor> commands;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		SManager.load();

		getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", SManager.getManager());
		getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

		getServer().getPluginManager().registerEvents(SManager.getManager(), plugin);

		commands = new HashMap<>();

		commands.put("main", new MainCommand(plugin));
		commands.put("admin", new AdminCommand(plugin));
	}

	@Override
	public void onDisable(){

	}

	public static SHyperingEconomy getPlugin(){
		return plugin;
	}

	public SHyperingEconomyAPI getSSideHyperingEconomyAPI(){
		return SManager.getManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		return commands.get(command.getName()).onCommand(sender, command, label, args);
	}

}
