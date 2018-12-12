/*
 * 本プラグインの著作権は、amata1219(Twitter@amata1219)に帰属します。
 * また、本プラグインの二次配布、改変使用、自作発言を禁じます。
 */

package amata1219.hypering.economy.bungeecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteStreams;

import amata1219.hypering.economy.Database;
import amata1219.hypering.economy.HyperingEconomyAPI;
import amata1219.hypering.economy.MoneyRankingUpdater;
import amata1219.hypering.economy.ServerName;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class Necleus extends Plugin implements Listener {

	private static Necleus plugin;

	private Configuration config;

	private ScheduledTask rankingUpdater;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new Command("admin", "hypering.economy.admin"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				if(!(sender instanceof ProxiedPlayer)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
					return;
				}

				//ServerName name = ServerName.valueOf(((ProxiedPlayer) sender).getServer().getInfo().getName());
			}

		});

		Database.load(config.getString("MySQL.host"), config.getInt("MySQL.port"), config.getString("MySQL.database"), config.getString("username"), config.getString("password"), config.getString("MySQL.table"));

		rankingUpdater = getProxy().getScheduler().schedule(this, new MoneyRankingUpdater(), 0, 5, TimeUnit.MINUTES);
	}

	@Override
	public void onDisable(){
		rankingUpdater.cancel();

		Database.close();
	}

	public static Necleus getPlugin(){
		return plugin;
	}

	public void saveDefaultConfig(){
		File file = new File(getDataFolder(), "bcside_config.yml");

		if(!file.exists()){
			try{
				getDataFolder().mkdirs();

				file.createNewFile();
			}catch(IOException e){

			}

			try(FileOutputStream output = new FileOutputStream(file);
					InputStream input = getResourceAsStream("bcside_config.yml")){
					ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
				return;
			}
		}

		try{
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public Configuration getConfig(){
		return config;
	}

	@EventHandler
	public void onJoin(PostLoginEvent e){
		ProxiedPlayer player = e.getPlayer();
		if(!player.getServer().getInfo().getName().equals("main"))
			return;

		UUID uuid = player.getUniqueId();

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(!api.exist(uuid)){
			Database.getDatabase().create(uuid);

			for(ServerName serverName : ServerName.values())
				api.setMoney(serverName, uuid, api.getMedian(serverName));
		}
	}

}
