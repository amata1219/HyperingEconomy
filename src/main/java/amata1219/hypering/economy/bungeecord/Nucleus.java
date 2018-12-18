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
import amata1219.hypering.economy.ServerName;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class Nucleus extends Plugin implements Listener {

	private static Nucleus plugin;

	private Configuration config;

	private ScheduledTask rankingUpdater;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig();

		//getProxy().registerChannel("BungeeCord");

		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new Command("admin", "hypering.economy.admin"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				if(!(sender instanceof ProxiedPlayer)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
					return;
				}

				//ServerName name = ServerName.valueOf(((ProxiedPlayer) sender).getServer().getInfo().getName());
				/*if(args.length == 0){
					sender.sendMessage(new TextComponent(ChatColor.RED + "/admin fixmedian [true/false] [median]"));
					return;
				}else if(args[0].equalsIgnoreCase("fixmedian")){
					if(args.length == 1){
						sender.sendMessage(new TextComponent(ChatColor.RED + "/admin fixmedian [true/false] [median]"));
						return;
					}else if(args[1].equalsIgnoreCase("true")){
						if(args.length == 2){
							sender.sendMessage(new TextComponent(ChatColor.RED + "/admin fixmedian true [median]"));
							return;
						}

						long l = 0L;
						try{
							l = Long.valueOf(args[1]);
						}catch(NumberFormatException e){
							sender.sendMessage(new TextComponent(ChatColor.RED + "中央値は半角数字で指定して下さい。。"));
							return;
						}

						config.set("FixMedian.Enable", true);
						config.set("FixMedian.Median", l);

						try {
							ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "bcside_config.yml"));
						} catch (IOException e1) {
							e1.printStackTrace();
						}


						Database.fixMedian(l);

						Database.getEconomyServers().forEach(serverName -> Database.getHyperingEconomyAPI().updateMedian(serverName));

						ByteArrayDataOutput output = ByteStreams.newDataOutput();
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						DataOutputStream stream = new DataOutputStream(bytes);

						try{
							stream.writeUTF("FixMedian");
							stream.writeUTF(String.valueOf(l));
							stream.writeShort(123);
						}catch(IOException e){
							e.printStackTrace();
						}

						output.writeShort(bytes.toByteArray().length);
						output.write(bytes.toByteArray());

						getProxy().getServerInfo("main").sendData("BungeeCord", bytes.toByteArray());

						sender.sendMessage(new TextComponent(ChatColor.AQUA + "中央値を [" + l + "] に固定しました。"));
						return;
					}else if(args[1].equalsIgnoreCase("false")){
						config.set("FixMedian.Enable", false);
						config.set("FixMedian.Median", 100000L);

						try {
							ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "bcside_config.yml"));
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						Database.unfixMedian();

						Database.getEconomyServers().forEach(serverName -> Database.getHyperingEconomyAPI().updateMedian(serverName));

						ByteArrayDataOutput output = ByteStreams.newDataOutput();
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						DataOutputStream stream = new DataOutputStream(bytes);

						try{
							stream.writeUTF("FixMedian");
							stream.writeUTF("false");
							stream.writeShort(123);
						}catch(IOException e){
							e.printStackTrace();
						}

						output.writeShort(bytes.toByteArray().length);
						output.write(bytes.toByteArray());

						getProxy().getServerInfo("main").sendData("BungeeCord", bytes.toByteArray());

						sender.sendMessage(new TextComponent(ChatColor.AQUA + "中央値の固定を解除しました。"));
						return;
					}

				}*/
			}

		});

		Database.load(config.getString("MySQL.host"), config.getInt("MySQL.port"), config.getString("MySQL.database"), config.getString("MySQL.username"), config.getString("MySQL.password"));

		Database.registerEconomyServer(ServerName.MAIN);

		rankingUpdater = getProxy().getScheduler().schedule(this, new Runnable(){

			@Override
			public void run() {
				for(ServerName serverName : Database.getEconomyServers())
					Database.getDatabase().updateMoneyRanking(serverName);
			}

		}, 0, 5, TimeUnit.MINUTES);
	}

	@Override
	public void onDisable(){
		rankingUpdater.cancel();

		//getProxy().unregisterChannel("BungeeCord");

		Database.close();
	}

	public static Nucleus getPlugin(){
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

		UUID uuid = player.getUniqueId();

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(!api.exist(uuid))
			Database.getDatabase().create(uuid);
	}

	@EventHandler
	public void onQuit(ServerDisconnectEvent e){
		Database.getHyperingEconomyAPI().updateLastPlayed(e.getPlayer().getUniqueId());
	}

}
