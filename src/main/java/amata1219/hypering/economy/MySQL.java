package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import amata1219.hypering.economy.bungeecord.BCSideManager;

public class MySQL {

	//uuid char(36) playerdata text
	//create database HyperingEconomyDatabase character set utf8 collate utf8_general_ci;
	//- create table hyperingeconomydatabase.playerdata(uuid varchar(36), last bigint, money text, tickets bigint, ticketamounts bigint);
	//+ create table hyperingeconomydatabase.playerdata(uuid varchar(36), last bigint, tickets bigint, ticketamounts bigint, main bigint, mainflat bigint, lgw bigint);
	//2592000000
	private HikariDataSource hikari;

	private String host, database, username, password, table;
	private int port;

	public MySQL(String host, int port, String database, String username, String password, String table){
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.table = table;

		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);

		config.addDataSourceProperty("user", username);
		config.addDataSourceProperty("password", password);

		config.setInitializationFailFast(true);
		config.setConnectionInitSql("SELECT 1");

		hikari = new HikariDataSource(config);
	}

	public void close(){
		if(hikari != null)
			hikari.close();
	}

	public boolean putCommand(String command){
		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement(command)){

			statement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public PlayerData getPlayerData(UUID uuid){
		String id = uuid.toString();
		int exist = -1;

		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT COUNT(uuid) AS count FROM " + database + "." + table + "WHERE uuid like '" + id + "'")){

			try(ResultSet result = statement.executeQuery()){
				if(result.next())
					exist = result.getInt("count");

				result.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		PlayerData data = null;

		if(exist == 1){
			try(Connection con = hikari.getConnection();
					PreparedStatement statement = con.prepareStatement("SELECT * FROM + " + database + "." + table + " WHERE uuid like '" + id + "'")){

				try(ResultSet result = statement.executeQuery()){
					while(result.next()){
						Map<ServerName, Long> money = new HashMap<>();

						money.put(ServerName.main, result.getLong("main"));
						money.put(ServerName.mainflat, result.getLong("mainflat"));
						money.put(ServerName.lgw, result.getLong("lgw"));

						data = PlayerData.load(uuid, money, result.getLong("tickets"), result.getLong("ticketamounts"));

						result.close();

						return data;
					}
				}

			}catch(SQLException e){
				e.printStackTrace();
			}
		}else if(exist == 0){
			data = new PlayerData(uuid);

			putCommand("INSERT INTO " + database + "." + table + " VALUES ('" + uuid.toString()+ "'," + System.currentTimeMillis()
			+ "," + data.getTickets() + "," + data.getTicketAmounts() + "," + data.toMoneyText() + ")");

			return data;
		}

		return null;
	}

	public boolean savePlayerData(PlayerData data){
		return putCommand("UPDATE " + database + "." + table + " SET tickets = " + data.getTickets() + ",ticketamounts = "
				+ data.getTicketAmounts() + ",main = " + data.getMoney(ServerName.main) + ",mainflat = " + data.getMoney(ServerName.mainflat)
				+ ",lgw = " + data.getMoney(ServerName.lgw) + " WHERE uuid = '" + data.getUniqueId().toString() + "'");
	}

	public boolean saveLastLogined(PlayerData data){
		return putCommand("UPDATE " + database + "." + table + " SET last = " + System.currentTimeMillis() + " where uuid = '" + data.getUniqueId().toString() + "'");
	}

	public HashMap<UUID, PlayerData> getPlayerDataOfLastJoinedInWithinMonthPlayers(){
		checkConnection();
		HashMap<UUID, PlayerData> playerdata = new HashMap<UUID, PlayerData>();
		try{
			ResultSet result = statement.executeQuery("SELECT * FROM " + database + "." + table);
			while(result.next()){
				if(System.currentTimeMillis() - result.getLong("last") <= 2592000000L){
					PlayerData data = new PlayerData(manager, UUID.fromString(result.getString("uuid")), PlayerData.toMoney(result.getString("money")), result.getLong("tickets"), result.getLong("ticketamounts"));
					playerdata.put(data.getUniqueId(), data);
				}
			}
			result.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return playerdata;
	}

	public List<PlayerData> getAllPlayerData(){
		checkConnection();
		List<PlayerData> playerdata = new ArrayList<PlayerData>();
		try{
			ResultSet result = statement.executeQuery("SELECT * FROM " + database + "." + table);
			while(result.next()){
				PlayerData data = new PlayerData(manager, UUID.fromString(result.getString("uuid")), PlayerData.toMoney(result.getString("money")), result.getLong("tickets"), result.getLong("ticketamounts"));
				playerdata.add(data);
			}
			result.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return playerdata;
	}

	public boolean isOverOneMonth(UUID uuid){
		checkConnection();
		long last = 0;
		try{
			ResultSet result = statement.executeQuery("SELECT last FROM " + database + "." + table + "WHERE uuid like '" + uuid.toString() + "'");
			while(result.next()){
				last = result.getLong("last");
			}
			result.close();
		}catch(SQLException e){
			e.printStackTrace();
			return true;
		}
		return System.currentTimeMillis() - last > 2592000000L;
	}

}