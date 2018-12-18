package amata1219.hypering.economy.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import amata1219.hypering.economy.Database;
import amata1219.hypering.economy.ServerName;
import net.milkbowl.vault.economy.Economy;

public class Electron extends JavaPlugin {

	private static Electron plugin;

	private static ServerName serverName;

	private static boolean loadedVaultEconomy;

	private BukkitTask rankingUpdater;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		//getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		//getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		serverName = ServerName.valueOf(getConfig().getString("Aliases").toUpperCase());

		Database.load(getConfig().getString("MySQL.host"), getConfig().getInt("MySQL.port"), getConfig().getString("MySQL.database"), getConfig().getString("MySQL.username"), getConfig().getString("MySQL.password"));

		if(serverName == ServerName.MAIN)
			Database.registerEconomyServer(serverName);

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

		rankingUpdater = new BukkitRunnable(){

			@Override
			public void run() {
				for(ServerName serverName : Database.getEconomyServers())
					Database.getDatabase().updateMoneyRanking(serverName);
			}

		}.runTaskTimer(this, 0, 6000);
	}

	@Override
	public void onDisable(){
		rankingUpdater.cancel();

		//getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
		//getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord", this);

		Database.close();

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
		return loadedVaultEconomy;
	}

	/*@Override
	public void onPluginMessageReceived(String tag, Player player, byte[] data) {
		if(!tag.equals("BungeeCord"))
			return;

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

		String s = null;
		try {
			s = stream.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(!s.equals("FixMedian"))
			return;

		try {
			s = stream.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(s.equals("false"))
			Database.unfixMedian();
		else
			Database.fixMedian(Long.valueOf(s));

		Database.getEconomyServers().forEach(serverName -> Database.getHyperingEconomyAPI().updateMedian(serverName));
	}*/

}
