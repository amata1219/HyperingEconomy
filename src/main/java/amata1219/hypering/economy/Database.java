package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
	//2592000000

	private static Database database;

	private HikariDataSource source;

	private String databaseName;
	private String playerDataTableName;
	private String ticketDataTableName;
	private String mainMedianChainTableName;

	private HashMap<ServerName, MedianChain> chain = new HashMap<>();
	private HashMap<ServerName, Long> median = new HashMap<>();
	private HashMap<ServerName, MoneyRanking> ranking = new HashMap<>();

	private Database(){

	}

	public static void main(String[] args){
		new Measurer(){

			@Override
			public void execute() {

			}

		}.println();
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

		config.setInitializationFailFast(true);
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
		database.chain.put(serverName, MedianChain.load(serverName));
		database.median.put(serverName, 5000L);
		database.ranking.put(serverName, MoneyRanking.load(serverName));

		database.updateMedian(serverName);
	}

	public static void unregisterEconomySerber(ServerName serverName){
		database.chain.remove(serverName);
		database.median.remove(serverName);
		database.ranking.remove(serverName);
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
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	public static Object getResult(String command, String columnIndex){
		try(Connection con = Database.database.source.getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){

				Object obj = null;
				while(result.next()){
					obj = result.getObject(columnIndex);
					break;
				}

				result.close();
				return obj;
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void updateMedian(ServerName serverName) {
		if(!chain.containsKey(serverName))
			return;

		String columnIndex = serverName.name().toLowerCase();

		List<Long> list = new ArrayList<>();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT " + columnIndex + ", uuid FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " WHERE last<=2592000000 AND " + columnIndex + ">0")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					list.add(result.getLong(columnIndex) + TicketDatabase.getTicketsValue(serverName, result.getString("uuid")));

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		int size = list.size();
		if(size == 0 || list.isEmpty()){
			median.put(serverName, 5000L);
		}else{
			list.sort(Comparator.reverseOrder());

			if(size % 2 == 0)
				median.put(serverName, list.get(size / 2));
			else
				median.put(serverName, list.get(size / 2));
		}

		chain.get(serverName).update(median.get(serverName));
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

	@Override
	public MoneyRanking getMoneyRanking(ServerName serverName){
		return ranking.get(serverName);
	}

	void updateMoneyRanking(ServerName serverName){
		ranking.put(serverName, MoneyRanking.load(serverName));
	}

	public void create(UUID uuid){
		putCommand("INSERT INTO " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " VALUES ('" + uuid.toString() + "'," + System.currentTimeMillis() + "," + median.get(ServerName.MAIN) + "," + 5000L + ")");
	}

	public void delete(UUID uuid){
		putCommand("DELETE FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " WHERE uuid='" + uuid.toString() + "'");
	}

	@Override
	public boolean exist(UUID uuid) {
		return (long) Getter.get("SELECT COUNT(uuid) AS count FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " WHERE uuid='" + uuid.toString() + "'", "count") == 1;
	}

	@Override
	public int existSize(){
		return Long.valueOf((long) Getter.get("SELECT COUNT(uuid) AS count FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName(), "count")).intValue();
	}

	@Override
	public boolean active(UUID uuid){
		return (long) Getter.get(uuid, "last") <= 2592000000L;
	}

	@Override
	public int activeSize(){
		return Long.valueOf((long) Getter.get("SELECT COUNT(uuid) AS count FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " WHERE last<=2592000000", "count")).intValue();
	}

	@Override
	public void updateLastPlayed(UUID uuid){
		Saver.save(uuid, "last", System.currentTimeMillis());
	}

	@Override
	public long getMoney(ServerName serverName, UUID uuid) {
		return (long) Getter.get(uuid, serverName.name().toLowerCase());
	}

	@Override
	public boolean hasMoney(ServerName serverName, UUID uuid, long threshold) {
		return getMoney(serverName, uuid) >= threshold;
	}

	@Override
	public void setMoney(ServerName serverName, UUID uuid, long money) {
		Saver.save(uuid, serverName.name().toLowerCase(), money);

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
		return TicketDatabase.getTickets(uuid);
	}

	@Override
	public boolean hasTickets(UUID uuid, long threshold) {
		return TicketDatabase.hasTickets(uuid, threshold);
	}

	@Override
	public void addTickets(UUID uuid, int increase) {
		TicketDatabase.addTickets(uuid, System.nanoTime(), increase);
	}

	@Override
	public void removeTickets(UUID uuid, int decrease) {
		TicketDatabase.removeTickets(uuid, decrease);
	}

	@Override
	public void buyTickets(ServerName serverName, UUID uuid, int number){
		TicketDatabase.buyTickets(serverName, uuid, number);
	}

	@Override
	public void cashTickets(ServerName serverName, UUID uuid, int number){
		TicketDatabase.cashTickets(serverName, uuid, number);
	}

}
