/*
 * 本プラグインの著作権は、amata1219(Twitter@amata1219)に帰属します。
 * また、本プラグインの二次配布、改変使用、自作発言を禁じます。
 */

package amata1219.hypering.economy.bungeecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new Command("admin", "hypering.economy.admin"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				/*if(!(sender instanceof ProxiedPlayer)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
					return;
				}

				ServerName name = ServerName.valueOf(((ProxiedPlayer) sender).getServer().getInfo().getName());*/

				if(args.length == 0){

				}else if(args[0].equalsIgnoreCase("median")){
					sender.sendMessage(new TextComponent(ChatColor.AQUA + "メインサーバーの中央値: " + Database.getHyperingEconomyAPI().getMedian(ServerName.MAIN)));
					sender.sendMessage(new TextComponent(ChatColor.AQUA + "固定されているか: " + Database.getHyperingEconomyAPI().getMedianChain(ServerName.MAIN).isFix()));
				}else if(args[0].equalsIgnoreCase("demo")){
					ServerName serverName = ServerName.MAIN;


					String columnIndex = serverName.name().toLowerCase();

					List<Long> list = new ArrayList<>();

					try(Connection con = Database.getHikariDataSource().getConnection();
							PreparedStatement statement = con.prepareStatement("SELECT " + columnIndex + ", uuid FROM HyperingEconomyDatabase.playerdata WHERE last >= " + (System.currentTimeMillis() - 2592000000L) + " AND " + columnIndex + " >= 500")){
						try(ResultSet result = statement.executeQuery()){
							while(result.next())
								list.add(result.getLong(columnIndex) + Database.getTicketsValue(serverName, result.getString("uuid")));

							result.close();
							statement.close();
						}
					}catch(SQLException e){
						e.printStackTrace();
					}

					int size = list.size();
					if(list.isEmpty()){
						sender.sendMessage(new TextComponent(ChatColor.AQUA + "条件を満たしたプレイヤーデータがありません。"));
						return;
					}

					list.sort(Comparator.reverseOrder());

					long m = list.get(size / 2);

					sender.sendMessage(new TextComponent(ChatColor.AQUA + "デモ計算結果: " + m));
					sender.sendMessage(new TextComponent(ChatColor.AQUA + "有効プレイヤーデータ数: " + size));
				}
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
