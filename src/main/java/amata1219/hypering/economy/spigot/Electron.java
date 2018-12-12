package amata1219.hypering.economy.spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class Electron extends JavaPlugin {

	private static Electron plugin;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();
	}

	@Override
	public void onDisable(){

	}

	public static Electron getPlugin(){
		return plugin;
	}

	public boolean isEconomyEnable(){
		return getConfig().getBoolean("EnableEconomy");
	}

}
