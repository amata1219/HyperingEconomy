/*package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database implements HyperingEconomyAPI {

	//uuid char(36) playerdata text
	//create database HyperingEconomyDatabase character set utf8 collate utf8_general_ci;
	//create table HyperingEconomyDatabase.playerdata(uuid varchar(36), last bigint, main bigint, pata bigint);
	//create table HyperingEconomyDatabase.ticketdata(uuid varchar(36), time bigint, number int);
	//create table HyperingEconomyDatabase.main_medianchain(time bigint, median bigint);
	//ALTER TABLE main_medianchain ADD INDEX main_medianchain_index(time, median);
	//2592000000

	private static Database database;

	private HikariDataSource source;

	private String databaseName;
	private String playerDataTableName;
	private String ticketDataTableName;
	private String mainMedianChainTableName;

	private MedianChain chain;
	private long median;
	private final HashMap<UUID, Player> players = new HashMap<>();

	private Database(){

	}

	public static void load(String host, int port, String databaseName, String userName, String password){
		Database database = new Database();

		database.databaseName = databaseName;
		database.playerDataTableName = "playerdata";
		database.ticketDataTableName = "ticketdata";
		database.mainMedianChainTableName = "main_medianchain";

		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName);

		config.addDataSourceProperty("user", userName);
		config.addDataSourceProperty("password", password);

		config.setMaximumPoolSize(30);
		config.setMinimumIdle(30);
		config.setMaxLifetime(1800000);
		config.setConnectionTimeout(5000);

		config.setConnectionInitSql("SELECT 1");

		database.source = new HikariDataSource(config);

		Database.database = database;


	}

	public static Database getDatabase(){
		return database;
	}

	public static HyperingEconomyAPI getHyperingEconomyAPI(){
		return (HyperingEconomyAPI) database;
	}

	public static void registerEconomyServer(ServerName serverName){
		database.chain.put(serverName, MedianChain.load(serverName);
		database.median.put(serverName, MedianChain.DEFAULT_VALUE;

		database.updateMedian(serverName);

	}

	public static void unregisterEconomySerber(ServerName serverName){
		database.chain.remove(serverName);
		database.median.remove(serverName);
	}

	public static Set<ServerName> getEconomyServers(){
		return database.chain.keySet();
	}

	public static HikariDataSource getHikariDataSource(){
		return Database.database.source;
	}

	public static String getDatabaseName(){
		return Database.database.databaseName;
	}

	public static String getPlayerDataTableName(){
		return Database.database.playerDataTableName;
	}

	public static String getTicketDataTableName(){
		return Database.database.ticketDataTableName;
	}

	public static String getMainMedianChainTableName(){
		return Database.database.mainMedianChainTableName;
	}

	public static void close(){
		Database database = Database.database;

		if(database.source != null)
			database.source.close();
	}

	public static void putCommand(String command){
		try(Connection con = Database.database.source.getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			statement.executeUpdate();
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private final List<Long> list = new ArrayList<>();
	private final Map<String, Long> map = new HashMap<>();

	@Override
	public void updateMedian(ServerName serverName) {
		if(!chain.containsKey(serverName))
			return;

		String columnIndex = serverName.name().toLowerCase();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.playerdata")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					if(System.currentTimeMillis() - result.getLong("last") < 2592000000L)
						map.put(result.getString("uuid"), result.getLong(columnIndex));
				}
				result.close();
				statement.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.ticketdata")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					String uuid = result.getString("uuid");
					if(!map.containsKey(uuid))
						return;

					map.put(uuid, map.get(uuid) + chain.get(serverName).getTicketPrice(result.getLong("time")) * result.getInt("number"));
				}
				result.close();
				statement.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		for(long assets : map.values()){
			if(assets >= 500)
				list.add(assets);
		}

		int size = list.size();

		MedianChain mc = chain.get(serverName);

		if(size < 10){
			median.put(serverName, MedianChain.DEFAULT_VALUE);
			mc.setFix(true);
		}else{
			list.sort(Comparator.reverseOrder());

			long m = list.get(size / 2);

			if(m < 5000){
				median.put(serverName, MedianChain.DEFAULT_VALUE);
				mc.setFix(true);
			}else{
				median.put(serverName, m);
				if(mc.isFix())
					mc.setFix(false);
			}
		}

		chain.get(serverName).update(median.get(serverName));

		list.clear();
		map.clear();
	}

	@Override
	public long getMedian(ServerName serverName){
		return median.get(serverName);
	}

	@Override
	public MedianChain getMedianChain(ServerName serverName){
		return chain.get(serverName);
	}

	@Override
	public long getTicketPrice(ServerName serverName){
		return getMedian(serverName) / 1000L;
	}

	public void create(UUID uuid){
		putCommand("INSERT INTO HyperingEconomyDatabase.playerdata VALUES ('" + uuid.toString() + "'," + System.currentTimeMillis() + "," + 0L + "," + 0L + ")");
	}

	public void delete(UUID uuid){
		putCommand("DELETE FROM HyperingEconomyDatabase.playerdata WHERE uuid = '" + uuid.toString() + "'");
	}

	@Override
	public boolean exist(UUID uuid) {
		return Getter.getLong("SELECT COUNT(uuid) AS count FROM HyperingEconomyDatabase.playerdata WHERE uuid = '" + uuid.toString() + "'", "count") == 1;
	}

	@Override
	public long existSize(){
		return Getter.getLong("SELECT COUNT(uuid) AS count FROM HyperingEconomyDatabase.playerdata", "count");
	}

	@Override
	public boolean active(UUID uuid){
		return Getter.getLong(uuid, "last") >= System.currentTimeMillis() - 2592000000L;
	}

	@Override
	public long activeSize(){
		return Getter.getLong("SELECT COUNT(uuid) AS count FROM HyperingEconomyDatabase.playerdata WHERE last >= " + (System.currentTimeMillis() -  2592000000L), "count");
	}

	@Override
	public void updateLastPlayed(UUID uuid){
		Saver.saveLong(uuid, "last", System.currentTimeMillis());
	}

	@Override
	public long getLastPlayed(UUID uuid){
		return Getter.getLong(uuid, "last");
	}

	@Override
	public long getMoney(ServerName serverName, UUID uuid) {
		return Getter.getLong(uuid, serverName.name().toLowerCase());
	}

	@Override
	public boolean hasMoney(ServerName serverName, UUID uuid, long threshold) {
		return getMoney(serverName, uuid) >= threshold;
	}

	@Override
	public void setMoney(ServerName serverName, UUID uuid, long money) {
		Saver.saveLong(uuid, serverName.name().toLowerCase(), money);

		updateMedian(serverName);
	}

	@Override
	public void addMoney(ServerName serverName, UUID uuid, long increase){
		setMoney(serverName, uuid, getMoney(serverName, uuid) + increase);
	}

	@Override
	public void removeMoney(ServerName serverName, UUID uuid, long decrease) {
		setMoney(serverName, uuid, getMoney(serverName, uuid) - decrease);
	}

	@Override
	public MoneyEditer getMoneyEditer(ServerName serverName, UUID uuid){
		return new MoneyEditer(serverName, uuid);
	}

	@Override
	public long getTickets(UUID uuid) {
		return Getter.getLong("SELECT SUM(number) AS sum FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'", "sum");
	}

	@Override
	public boolean hasTickets(UUID uuid, long threshold) {
		return getTickets(uuid) >= threshold;
	}

	@Override
	public void addTickets(UUID uuid, long increase) {
		for(long i = increase; i > 0; i /= Integer.MAX_VALUE)
			Database.putCommand("INSERT INTO HyperingEconomyDatabase.ticketdata VALUES ('" + uuid.toString() + "'," + System.nanoTime() + "," + i + ")");

		for(ServerName serverName : Database.getEconomyServers())
			Database.getHyperingEconomyAPI().getMedianChain(serverName).flag();
	}

	@Override
	public void removeTickets(ServerName serverName, UUID uuid, long decrease) {
		if(!hasTickets(uuid, decrease))
			return;

		long time = Getter.getLong("SELECT time FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'", "time");
		int number = Getter.getInt("SELECT number FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time, "number");

		if(number > decrease){
			Database.putCommand("UPDATE HyperingEconomyDatabase.ticketdata SET number = " + (number - decrease) + " WHERE time = " + time);
			Database.getHyperingEconomyAPI().updateMedian(serverName);
			return;
		}

		Database.putCommand("DELETE FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time);

		if(number < decrease)
			removeTickets(serverName, uuid, decrease - number);
	}

	@Override
	public boolean canBuyTickets(ServerName serverName, UUID uuid, long number){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		return api.getMoney(serverName, uuid) >= api.getTicketPrice(serverName) * number;
	}

	@Override
	public void buyTickets(ServerName serverName, UUID uuid, long number){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		long price = number * api.getTicketPrice(serverName);
		if(!api.hasMoney(serverName, uuid, price))
			return;

		api.getMoneyEditer(serverName, uuid).remove(price);
		addTickets(uuid, number);
	}

	@Override
	public boolean canCashTickets(UUID uuid, long number){
		return getTickets(uuid) >= number;
	}

	@Override
	public void cashTickets(ServerName serverName, UUID uuid, long number){
		if(!hasTickets(uuid, number) || number <= 0)
			return;

		long time = Getter.getLong("SELECT time FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'", "time");
		int l = Getter.getInt("SELECT number FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time, "number");

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(l > number){
			Database.putCommand("UPDATE HyperingEconomyDatabase.ticketdata SET number = " + (l - number) + " WHERE time = " + time);
			api.getMoneyEditer(serverName, uuid).add(Double.valueOf(api.getMedianChain(serverName).getTicketPrice(time) / 10D * 9D).longValue() * number);
			Database.getHyperingEconomyAPI().updateMedian(serverName);
			return;
		}

		Database.putCommand("DELETE FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time);
		api.getMoneyEditer(serverName, uuid).add(Double.valueOf(api.getMedianChain(serverName).getTicketPrice(time) / 10D * 9D).longValue() * l);

		if(l < number)
			cashTickets(serverName, uuid, number - l);
	}

	@Override
	public long getTicketsValue(ServerName serverName, UUID uuid){
		return getTicketsValue(serverName, uuid.toString());
	}

	public static long getTicketsValue(ServerName serverName, String uuid){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		MedianChain chain = api.getMedianChain(serverName);

		long sum = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT time, number FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					sum += chain.getTicketPrice(result.getLong("time")) * result.getInt("number");

				result.close();
				statement.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return sum;
	}

}*/