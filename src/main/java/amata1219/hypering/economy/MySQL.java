package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQL {

	//uuid char(36) playerdata text
	//create database HyperingEconomyDatabase character set utf8 collate utf8_general_ci;
	//create table hyperingeconomydatabase.playerdata(uuid varchar(36), last bigint, tickets bigint, ticketamounts bigint, main bigint, pata bigint);
	//2592000000

	private static MySQL mysql;

	private static HikariDataSource hikari;

	private static String database, table;

	private MySQL(){
		mysql = this;
	}

	public static void load(String host, int port, String database, String username, String password, String table){
		new MySQL();

		MySQL.database = database;
		MySQL.table = table;

		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);

		config.addDataSourceProperty("user", username);
		config.addDataSourceProperty("password", password);

		config.setInitializationFailFast(true);
		config.setConnectionInitSql("SELECT 1");

		hikari = new HikariDataSource(config);
	}

	public static MySQL getMySQL(){
		return mysql;
	}

	public static void close(){
		if(hikari != null)
			hikari.close();
	}

	public static boolean putCommand(String command){
		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			statement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static PlayerData getPlayerData(UUID uuid){
		String id = uuid.toString();
		int exist = -1;

		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT COUNT(uuid) AS count FROM " + database + "." + table + " WHERE uuid=?")){
				statement.setString(1, id);

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
					PreparedStatement statement = con.prepareStatement("SELECT tickets, ticketamounts, main, pata FROM " + database + "." + table + " WHERE uuid=?")){
					statement.setString(1, id);

				try(ResultSet result = statement.executeQuery()){
					while(result.next()){

						Map<ServerName, Long> money = new HashMap<>();

						money.put(ServerName.MAIN, result.getLong("main"));
						money.put(ServerName.PATA, result.getLong("pata"));

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

	public static List<PlayerData> getAllPlayerData(){
		List<PlayerData> list = new ArrayList<>();
		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT uuid, tickets, ticketamounts, main, pata FROM " + database + "." + table)){

			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					Map<ServerName, Long> money = new HashMap<>();

					money.put(ServerName.MAIN, result.getLong("main"));
					money.put(ServerName.PATA, result.getLong("pata"));
					list.add(PlayerData.load(UUID.fromString(result.getString("uuid")), money, result.getLong("tickets"), result.getLong("ticketamounts")));
				}

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return list;
	}

	public static boolean savePlayerData(PlayerData data){
		return putCommand("UPDATE " + database + "." + table + " SET tickets = " + data.getTickets() + ",ticketamounts = "
				+ data.getTicketAmounts() + ",main = " + data.getMoney(ServerName.MAIN) + ",pata = " + data.getMoney(ServerName.PATA) + " WHERE uuid='" + data.getUniqueId().toString() + "'");
	}

	public static boolean saveLastLogined(PlayerData data){
		return putCommand("UPDATE " + database + "." + table + " SET last = " + System.currentTimeMillis() + " WHERE uuid='" + data.getUniqueId().toString() + "'");
	}

	public static Map<UUID, PlayerData> getOfflinePlayerDataWithinMonth(){
		Map<UUID, PlayerData> map = new HashMap<>();

		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT uuid, last, tickets, ticketamounts, main, pata FROM " + database + "." + table)){

			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					if(System.currentTimeMillis() - result.getLong("last") <= 2592000000L){
						Map<ServerName, Long> money = new HashMap<>();

						money.put(ServerName.MAIN, result.getLong("main"));
						money.put(ServerName.PATA, result.getLong("pata"));

						UUID uuid = UUID.fromString(result.getString("uuid"));

						map.put(uuid, PlayerData.load(uuid, money, result.getLong("tickets"), result.getLong("ticketamounts")));
					}
				}

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return map;
	}

	public static boolean isOver(UUID uuid){
		long last = 0;
		try(Connection con = hikari.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT last FROM " + database + "." + table + " WHERE uuid=?")){
				statement.setString(1, uuid.toString());

			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					last = result.getLong("last");

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
			return true;
		}

		return System.currentTimeMillis() - last > 2592000000L;
	}

}