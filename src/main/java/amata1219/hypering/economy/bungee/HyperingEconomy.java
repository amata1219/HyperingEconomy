package amata1219.hypering.economy.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.google.common.io.ByteStreams;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class HyperingEconomy extends Plugin implements Listener {

	private HikariDataSource source;

	@Override
	public void onEnable(){
		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		File file = new File(getDataFolder() + File.separator + "conf.yml");

		if(!file.exists()){
			try{
				getDataFolder().mkdirs();

				file.createNewFile();
			}catch(IOException e){

			}

			try(FileOutputStream output = new FileOutputStream(file);
					InputStream input = getResourceAsStream("conf.yml")){
					ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		Configuration section = null;
		try{
			section = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file).getSection("MySQL");
		}catch(IOException e){
			e.printStackTrace();
		}

		config.setJdbcUrl("jdbc:mysql://" + section.getString("host") + ":" + section.getInt("port") + "/" + section.getString("database"));

		config.addDataSourceProperty("user", section.getString("username"));
		config.addDataSourceProperty("password", section.getString("password"));

		config.setMaximumPoolSize(30);
		config.setMinimumIdle(30);
		config.setMaxLifetime(1800000);
		config.setConnectionTimeout(5000);

		config.setConnectionInitSql("SELECT 1");

		source = new HikariDataSource(config);

		getProxy().getPluginManager().registerListener(this, this);
	}

	@Override
	public void onDisable(){
		if(source != null && !source.isClosed())
			source.close();
	}

	@EventHandler
	public void onJoin(PostLoginEvent e){
		UUID uuid = e.getPlayer().getUniqueId();
		if(!exist(uuid))
			create(uuid);
	}

	@EventHandler
	public void onQuit(PlayerDisconnectEvent e){
		updateLastPlayed(e.getPlayer().getUniqueId());
	}

	public void putCommand(String command){
		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			statement.executeUpdate();
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	public void create(UUID uuid){
		putCommand("INSERT INTO HyperingEconomyDatabase.playerdata VALUES ('" + uuid.toString() + "'," + System.currentTimeMillis() + "," + 0L + "," + 0L + ")");
	}

	public boolean exist(UUID uuid){
		long l = 0;

		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT COUNT(uuid) AS count FROM HyperingEconomyDatabase.playerdata WHERE uuid = '" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					l = result.getLong("count");
					break;
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

		return l == 1;
	}

	public void updateLastPlayed(UUID uuid){
		putCommand("UPDATE HyperingEconomyDatabase.playerdata SET last = " + System.currentTimeMillis() + " WHERE uuid = '" + uuid.toString() + "'");
	}

}
