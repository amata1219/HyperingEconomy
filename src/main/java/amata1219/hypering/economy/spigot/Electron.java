package amata1219.hypering.economy.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.hypering.economy.ServerName;
import net.milkbowl.vault.economy.Economy;

public class Electron extends JavaPlugin {

	private static Electron plugin;

	private static ServerName serverName;

	private static boolean loadVaultEconomy;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		serverName = ServerName.valueOf(getConfig().getString("Aliases"));

		new Listener(){
			private Electron electron;

			public void setElectron(Electron electron){
				this.electron = electron;

				Plugin plugin = electron.getServer().getPluginManager().getPlugin("Vault");
				if(plugin == null)
					return;

				if(!plugin.isEnabled())
					electron.getServer().getPluginManager().registerEvents(this, electron);
				else
					loadVaultEconomy();
			}

			private void loadVaultEconomy(){
				VaultEconomy.load();

				electron.getServer().getServicesManager().register(Economy.class, VaultEconomy.getInstance(), electron, ServicePriority.Normal);
			}

			@EventHandler
			public void onEnable(PluginEnableEvent e){
				if(!e.getPlugin().getName().equals("Vault"))
					return;

				loadVaultEconomy();

				PluginEnableEvent.getHandlerList().unregister(electron);
			}
		}.setElectron(this);
	}

	@Override
	public void onDisable(){
		getServer().getServicesManager().unregisterAll(this);

		HandlerList.unregisterAll(this);
	}

	public static Electron getPlugin(){
		return plugin;
	}

	public static ServerName getServerName(){
		return serverName;
	}

	public static boolean isEconomyEnable(){
		return plugin.getConfig().getBoolean("EnableEconomy");
	}

	public static boolean isLoadedVaultEconomy(){
		return loadVaultEconomy;
	}

}
