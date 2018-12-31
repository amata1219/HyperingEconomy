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

import org.bukkit.configuration.ConfigurationSection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import amata1219.hypering.economy.spigot.HyperingEconomy;

public class SQL implements HyperingEconomyAPI {

	//uuid char(36) playerdata text
	//create database HyperingEconomyDatabase character set utf8 collate utf8_general_ci;
	//create table HyperingEconomyDatabase.playerdata(uuid varchar(36), last bigint, main bigint, pata bigint);
	//create table HyperingEconomyDatabase.ticketdata(uuid varchar(36), time bigint, number int);
	//create table HyperingEconomyDatabase.main_medianchain(time bigint, median bigint);
	//ALTER TABLE main_medianchain ADD INDEX main_medianchain_index(time, median);
	//2592000000

	private static SQL sql = new SQL();

	private HikariDataSource source;

	private MedianChain chain;
	private Map<UUID, Player> players = new HashMap<>();

	private List<Future> futures = new ArrayList<>();

	private SQL(){
		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		ConfigurationSection section = HyperingEconomy.getPlugin().getConfig().getConfigurationSection("MySQL");

		config.setJdbcUrl("jdbc:mysql://" + section.getString("host") + ":" + section.getInt("port") + "/" + section.getString("database"));

		config.addDataSourceProperty("user", section.getString("username"));
		config.addDataSourceProperty("password", section.getString("password"));

		config.setMaximumPoolSize(30);
		config.setMinimumIdle(30);
		config.setMaxLifetime(1800000);
		config.setConnectionTimeout(5000);

		config.setConnectionInitSql("SELECT 1");

		source = new HikariDataSource(config);

		ServerName serverName = HyperingEconomy.getServerName();

		chain = MedianChain.load(serverName);

		String columnIndex = serverName.name().toLowerCase();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.playerdata")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					if(System.currentTimeMillis() - result.getLong("last") < 2592000000L)
						players.put(UUID.fromString(result.getString("uuid")), new Player(result.getLong(columnIndex)));
				}
				result.close();
				statement.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

	}

	public static SQL getSQL(){
		return sql;
	}

	public void close(){
		if(source != null && !source.isClosed())
			source.close();
	}

	public void wish(Future future){
		futures.add(future);
		System.out.println("Added to future list.");
	}

	public long nano(){
		return System.nanoTime();
	}

	public void putCommand(String command){
		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			statement.executeUpdate();
			statement.close();
		}catch(SQLException e){
			wish(new Future(){

				@Override
				public void done() {
					putCommand(command);
				}

			});
		}
	}

	@Override
	public long getMedian() {
		return chain.getMedian(nano());
	}

	@Override
	public MedianChain getMedianChain() {
		return null;
	}

	@Override
	public long getTicketPrice() {
		return 0;
	}

	@Override
	public boolean exist(UUID uuid) {
		return false;
	}

	@Override
	public long existSize() {
		return 0;
	}

	@Override
	public boolean active(UUID uuid) {
		return false;
	}

	@Override
	public long activeSize() {
		return 0;
	}

	@Override
	public void updateLastPlayed(UUID uuid) {
	}

	@Override
	public long getLastPlayed(UUID uuid) {
		return 0;
	}

	@Override
	public long getMoney(UUID uuid) {
		return 0;
	}

	@Override
	public boolean hasMoney(UUID uuid, long threshold) {
		return false;
	}

	@Override
	public void setMoney(UUID uuid, long money) {
	}

	@Override
	public void addMoney(UUID uuid, long increase) {
	}

	@Override
	public void removeMoney(UUID uuid, long decrease) {
	}

	@Override
	public long getTickets(UUID uuid) {
		return 0;
	}

	@Override
	public boolean hasTickets(UUID uuid, long threshold) {
		return false;
	}

	@Override
	public void addTickets(UUID uuid, long increase) {
	}

	@Override
	public void removeTickets(UUID uuid, long decrease) {
	}

	@Override
	public boolean canBuyTickets(UUID uuid, long number) {
		return false;
	}

	@Override
	public void buyTickets(UUID uuid, long number) {
	}

	@Override
	public boolean canCashTickets(UUID uuid, long number) {
		return false;
	}

	@Override
	public void cashTickets(UUID uuid, long number) {
	}

	@Override
	public long getTicketsValue(UUID uuid) {
		return 0;
	}

}
