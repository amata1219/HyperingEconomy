package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database implements HyperingEconomyAPI {

	//uuid char(36) playerdata text
	//create database HyperingEconomyDatabase character set utf8 collate utf8_general_ci;
	//create table hyperingeconomydatabase.playerdata(uuid varchar(36), last bigint, tickets bigint, ticketamounts bigint, main bigint, pata bigint);
	//2592000000

	private static Database database;

	private HikariDataSource source;

	private String databaseName;
	private String tableName;

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

	public static void load(String host, int port, String databaseName, String userName, String password, String tableName){
		Database database = new Database();

		database.databaseName = databaseName;
		database.tableName = tableName;

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

	public static HikariDataSource getHikariDataSource(){
		return Database.database.source;
	}

	public static String getDatabaseName(){
		return Database.database.databaseName;
	}

	public static String getTableName(){
		return Database.database.tableName;
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
				while(result.next()){
					result.close();

					return result.getObject(columnIndex);
				}
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void updateMedian(ServerName serverName) {
		String columnIndex = serverName.name().toLowerCase();

		List<Long> list = new Getter<Long>().getList("SELECT " + columnIndex + ",ticketamounts FROM " + Database.getDatabaseName() + "." + Database.getTableName() + " WHERE last<=2592000000 AND " + columnIndex + ">0", columnIndex, "ticketamounts");

		/*try(Connection con = Database.database.source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT " + columnIndex + "," + "ticketamounts" + " FROM " + Database.getDatabaseName() + "." + Database.getTableName() + " WHERE last<=2592000000 AND " + columnIndex + ">0")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					list.add(result.getLong(columnIndex) + result.getLong("ticketamounts"));

				result.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}*/

		if(list.isEmpty())
			median.put(serverName, 5000L);

		list.sort(Comparator.reverseOrder());

		int size = list.size();

		if(size % 2 == 0)
			median.put(serverName, list.get(size / 2));
		else
			median.put(serverName, list.get(size / 2));
	}

	@Override
	public long getMedian(ServerName serverName){
		return median.get(serverName);
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
		putCommand("INSERT INTO " + Database.getDatabaseName() + "." + Database.getTableName() + " VALUES ('" + uuid.toString() + "'," + System.currentTimeMillis() + "," + 0L + "," + 0L + "," + 0L + "," + 0L + ")");
	}

	public void delete(UUID uuid){
		putCommand("DELETE FROM " + Database.getDatabaseName() + "." + Database.getTableName() + " WHERE uuid='" + uuid.toString() + "'");
	}

	@Override
	public boolean exist(UUID uuid) {
		return new Getter<Integer>().get("SELECT COUNT(uuid) AS count FROM " + Database.getDatabaseName() + "." + Database.getTableName() + " WHERE uuid='" + uuid.toString() + "'", "count") == 1;
	}

	@Override
	public int existSize(){
		return new Getter<Integer>().get("SELECT COUNT(uuid) AS count FROM " + Database.getDatabaseName() + "." + Database.getTableName(), "count");
	}

	@Override
	public boolean active(UUID uuid){
		return new Getter<Integer>().get(uuid, "last") <= 2592000000L;
	}

	@Override
	public int activeSize(){
		return new Getter<Integer>().get("SELECT COUNT(uuid) AS count FROM " + Database.getDatabaseName() + "." + Database.getTableName() + " WHERE last<=2592000000", "count");
	}

	@Override
	public long getMoney(ServerName serverName, UUID uuid) {
		return new Getter<Long>().get(uuid, serverName.name().toLowerCase());
	}

	@Override
	public boolean hasMoney(ServerName serverName, UUID uuid, long threshold) {
		return getMoney(serverName, uuid) >= threshold;
	}

	@Override
	public void setMoney(ServerName serverName, UUID uuid, long money) {
		new Saver<Long>().save(uuid, serverName.name().toLowerCase(), money);

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
		return new Getter<Long>().get(uuid, "tickets");
	}

	@Override
	public void setTickets(UUID uuid, long tickets){
		new Saver<Long>().save(uuid, "tickets", tickets);
	}

	@Override
	public boolean hasTickets(UUID uuid, long threshold) {
		return getTickets(uuid) >= threshold;
	}

	@Override
	public void addTickets(UUID uuid, long increase) {
		new Saver<Long>().save(uuid, "tickets", getTickets(uuid) + increase);
	}

	@Override
	public void removeTickets(UUID uuid, long decrease) {
		new Saver<Long>().save(uuid, "tickets", getTickets(uuid) - decrease);
	}

	@Override
	public long getTicketsValue(UUID uuid) {
		return new Getter<Long>().get(uuid, "ticketamounts");
	}

	@Override
	public long getTicketsValuePerTicket(UUID uuid) {
		long tickets = getTickets(uuid);
		if(tickets <= 0)
			return 0;

		return getTicketsValue(uuid) / tickets;
	}

	@Override
	public void setTicketsValue(UUID uuid, long ticketsValue) {
		new Saver<Long>().save(uuid, "ticketamounts", ticketsValue);
	}

	@Override
	public void addTicketsValue(UUID uuid, long increase) {
		new Saver<Long>().save(uuid, "ticketamounts", getTicketsValue(uuid) + increase);
	}

	@Override
	public void removeTicketsValue(UUID uuid, long decrease) {
		new Saver<Long>().save(uuid, "ticketamounts", getTicketsValue(uuid) + decrease);
	}

	@Override
	public TicketEditer getTicketEditer(ServerName serverName, UUID uuid) {
		return new TicketEditer(serverName, uuid);
	}
}
