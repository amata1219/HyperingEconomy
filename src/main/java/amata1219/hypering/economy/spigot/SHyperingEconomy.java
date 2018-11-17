package amata1219.hypering.economy.spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class SHyperingEconomy extends JavaPlugin{

	private static SHyperingEconomy plugin;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		new SManager();

		getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", SManager.getManager());
		getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

		getServer().getPluginManager().registerEvents(SManager.getManager(), plugin);
	}

	@Override
	public void onDisable(){
		SManager.getManager().unload();
	}

	public static SHyperingEconomy getPlugin(){
		return plugin;
	}

	public SHyperingEconomyAPI getSSideHyperingEconomyAPI(){
		return SManager.getManager();
	}
}
