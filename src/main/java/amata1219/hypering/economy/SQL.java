package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import amata1219.hypering.economy.spigot.CollectedEvent;
import amata1219.hypering.economy.spigot.HyperingEconomy;

public class SQL implements HyperingEconomyAPI {

	//uuid char(36) playerdata text
	//create database HyperingEconomyDatabase character set utf8 collate utf8_general_ci;
	//create table HyperingEconomyDatabase.playerdata(uuid varchar(36), last bigint, main bigint, pata bigint);
	//create table HyperingEconomyDatabase.ticketdata(uuid varchar(36), time bigint, number int);
	//create table HyperingEconomyDatabase.main_medianchain(time bigint, median bigint);
	//ALTER TABLE main_medianchain ADD INDEX main_medianchain_index(time, median);
	//2592000000

	private static SQL sql;

	public String name;

	private HikariDataSource source;
	private BukkitTask saver, fixer;

	private MedianChain chain;
	public Map<UUID, Money> playerdata = new HashMap<>();

	private List<Future> futures = new ArrayList<>();

	public final static HashMap<UUID, Long> map = new HashMap<>();
	private BukkitTask collector;

	private SQL(){

	}

	public static void enable(){
		sql = new SQL();

		ServerName serverName = HyperingEconomy.getServerName();

		sql.name = serverName.name().toLowerCase();

		sql.load();

		sql.chain = MedianChain.load(serverName);

		String columnIndex = serverName.name().toLowerCase();
		AtomicInteger count = new AtomicInteger();
		try(Connection con = sql.source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.playerdata")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					if(System.currentTimeMillis() - result.getLong("last") < 2592000000L){
						sql.playerdata.put(UUID.fromString(result.getString("uuid")), new Money(result.getLong(columnIndex)));
						count.incrementAndGet();
					}
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

		System.out.println(count.get() + " player data loaded.");

		sql.collector = new BukkitRunnable(){

			@Override
			public void run(){
				CollectedEvent event = new CollectedEvent(SQL.map);
				Bukkit.getPluginManager().callEvent(event);
				map.clear();
			}

		}.runTaskTimer(HyperingEconomy.getPlugin(), 36000, 36000L);
	}

	private void load(){
		close();

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

		cancel();

		fixer = new BukkitRunnable(){
			@Override
			public void run(){
				futures.forEach(Future::done);
			}
		}.runTaskTimerAsynchronously(HyperingEconomy.getPlugin(), 1200L, 1200L);

		saver = new BukkitRunnable(){
			@Override
			public void run(){
				for(Entry<UUID, Money> entry : playerdata.entrySet()){
					Money money = entry.getValue();
					if(!money.isChanged())
						continue;

					Saver.saveLong(entry.getKey(), name, money.get());

					money.clear();
				}
			}
		}.runTaskTimerAsynchronously(HyperingEconomy.getPlugin(), 19000L, 19000L);
	}

	public static SQL getSQL(){
		return sql;
	}

	public HikariDataSource getSource(){
		return source;
	}

	public void close(){
		if(source != null && !source.isClosed())
			source.close();
	}

	public void cancel(){
		if(saver != null && !saver.isCancelled())
			saver.cancel();

		if(fixer != null && !fixer.isCancelled())
			fixer.cancel();

		if(collector != null && !collector.isCancelled())
			collector.cancel();
	}

	public void wish(Future future){
		futures.add(future);
		System.out.println("Added to list of necessary to execute again.");
		load();
		System.out.println("Reloaded HikariDataSource.");

	}

	public long nano(){
		return System.nanoTime();
	}

	public long millis(){
		return System.currentTimeMillis();
	}

	public static void collect(UUID uuid, long increase){
		if(map.containsKey(uuid))
			map.put(uuid, map.get(uuid) + increase);
		else
			map.put(uuid, increase);
	}

	public void putCommand(String command){
		new BukkitRunnable(){

			@Override
			public void run() {
				try(Connection con = source.getConnection();
						PreparedStatement statement = con.prepareStatement(command)){
					statement.executeUpdate();
					statement.close();
				}catch(SQLException e){
					e.printStackTrace();
					wish(new Future(){

						@Override
						public void done() {
							putCommand(command);
						}

					});
				}
			}

		}.runTaskAsynchronously(HyperingEconomy.getPlugin());
	}

	@Override
	public HyperingEconomyAPI getHyperingEconomyAPI(){
		return (HyperingEconomyAPI) this;
	}

	private final Comparator<Money> comparator = new Comparator<Money>(){

		@Override
		public int compare(Money o1, Money o2) {
			return -Long.compare(o1.get(), o2.get());
		}

	};

	@Override
	public void updateMedian() {
		HashMap<UUID, Money> map = new HashMap<>();
		playerdata.forEach((k, v) -> map.put(k, v.clone()));

		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.ticketdata")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					UUID uuid = UUID.fromString(result.getString("uuid"));
					if(!map.containsKey(uuid))
						continue;

					Money money = map.get(uuid);
					money.add(chain.getTicketPrice(result.getLong("time")) * result.getInt("number"));
					if(money.get() < 500)
						map.remove(uuid);
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
			wish(new Future(){

				@Override
				public void done() {
					updateMedian();
				}

			});
		}

		List<Money> list = new ArrayList<>(map.values());

		int size = list.size();

		if(size < 10){
			chain.update(MedianChain.DEFAULT_VALUE);
			chain.setFix(true);
		}else{
			list.sort(comparator);

			long m = list.get(size / 2).get();

			if(m < 5000){
				chain.update(MedianChain.DEFAULT_VALUE);
				chain.setFix(true);
			}else{
				chain.update(m);
				chain.setFix(false);
			}
		}
	}

	@Override
	public long getMedian() {
		return chain.getMedian(nano());
	}

	@Override
	public MedianChain getMedianChain() {
		return chain;
	}

	@Override
	public long getTicketPrice() {
		return chain.getTicketPrice(nano());
	}

	public void create(UUID uuid){
		putCommand("INSERT INTO HyperingEconomyDatabase.playerdata VALUES ('" + uuid.toString() + "'," + System.currentTimeMillis() + "," + 0L + "," + 0L + ")");

		playerdata.put(uuid, new Money(0L));
	}

	public void delete(UUID uuid){
		putCommand("DELETE FROM HyperingEconomyDatabase.playerdata WHERE uuid = '" + uuid.toString() + "'");

		playerdata.remove(uuid);
	}

	@Override
	public boolean exist(UUID uuid) {
		if(playerdata.containsKey(uuid))
			return true;

		return Getter.getLong("SELECT COUNT(uuid) AS count FROM HyperingEconomyDatabase.playerdata WHERE uuid = '" + uuid.toString() + "'", "count") == 1;
	}

	@Override
	public long existSize() {
		return Getter.getLong("SELECT COUNT(uuid) AS count FROM HyperingEconomyDatabase.playerdata", "count");
	}

	@Override
	public boolean active(UUID uuid) {
		if(playerdata.containsKey(uuid))
			return true;

		return millis() - Getter.getLong(uuid, "last") < 2592000000L;
	}

	@Override
	public long activeSize() {
		long count = 0;
		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.playerdata")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					if(millis() - result.getLong("last") < 2592000000L)
						count++;
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

		return count;
	}

	@Override
	public void updateLastPlayed(UUID uuid) {
		Saver.saveLong(uuid, "last", millis());
	}

	@Override
	public long getLastPlayed(UUID uuid) {
		return Getter.getLong(uuid, "last");
	}

	@Override
	public long getMoney(UUID uuid) {
		return playerdata.containsKey(uuid) ? playerdata.get(uuid).get() : Getter.getLong(uuid, name);
	}

	@Override
	public boolean hasMoney(UUID uuid, long threshold) {
		return playerdata.containsKey(uuid) ? playerdata.get(uuid).has(threshold) : Getter.getLong(uuid, name) >= threshold;
	}

	@Override
	public void setMoney(UUID uuid, long money) {
		if(playerdata.containsKey(uuid))
			playerdata.get(uuid).set(money);
		else
			Saver.saveLong(uuid, name, money);
	}

	@Override
	public void addMoney(UUID uuid, long increase) {
		if(playerdata.containsKey(uuid))
			playerdata.get(uuid).add(increase);
		else
			Saver.saveLong(uuid, name, Getter.getLong(uuid, name) + increase);

		collect(uuid, increase);
	}

	@Override
	public void removeMoney(UUID uuid, long decrease) {
		if(playerdata.containsKey(uuid)){
			playerdata.get(uuid).remove(decrease);
			return;
		}

		long money = Getter.getLong(uuid, name);

		Saver.saveLong(uuid, name, money >= decrease ? money - decrease : 0L);
	}

	@Override
	public void sendMoney(UUID from, UUID to, long money){
		removeMoney(from, money);
		addMoney(to, money);
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
		updateMedian();

		for(long i = increase; i > 0; i /= Integer.MAX_VALUE)
			putCommand("INSERT INTO HyperingEconomyDatabase.ticketdata VALUES ('" + uuid.toString() + "'," + nano() + "," + i + ")");

		chain.flag();
	}

	@Override
	public void removeTickets(UUID uuid, long decrease) {
		updateMedian();
		Decrease d = new Decrease(decrease);
		//hasTickets(UUID, long)している前提
		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					int number = result.getInt("number");
					long time = result.getLong("time");
					if(!(number < decrease)){
						if(number == decrease)
							putCommand("DELETE FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time);
						else
							putCommand("UPDATE HyperingEconomyDatabase.ticketdata SET number = " + (number - decrease) + " WHERE time = " + time);
						d.set(0);
						break;
					}

					putCommand("DELETE FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time);

					d.set(decrease -= number);
					continue;
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
			wish(new Future(){

				@Override
				public void done() {
					removeTickets(uuid, d.get());
				}

			});
		}
	}

	class Decrease {

		long decrease;

		Decrease(long decrease){
			this.decrease = decrease;
		}

		long get(){
			return decrease;
		}

		void set(long newDecrease){
			decrease = newDecrease;
		}

	}

	@Override
	public boolean canBuyTickets(UUID uuid, long number) {
		updateMedian();

		return getMoney(uuid) >= getTicketPrice() * number;
	}

	@Override
	public void buyTickets(UUID uuid, long number) {
		updateMedian();

		long price = number * getTicketPrice();
		if(!hasMoney(uuid, price))
			return;

		removeMoney(uuid, price);
		addTickets(uuid, number);
	}

	@Override
	public boolean canCashTickets(UUID uuid, long number) {
		return getTickets(uuid) >= number;
	}

	@Override
	public void cashTickets(UUID uuid, long number) {
		updateMedian();
		//hasTickets(UUID, long)している前提
		Decrease d = new Decrease(number);
		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					int maisu = result.getInt("number");
					long time = result.getLong("time");
					if(!(maisu < number)){
						if(maisu == number)
							putCommand("DELETE FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time);
						else
							putCommand("UPDATE HyperingEconomyDatabase.ticketdata SET number = " + (maisu - number) + " WHERE time = " + time);

						addMoney(uuid, Double.valueOf(getMedianChain().getTicketPrice(time) / 10D * 9D).longValue() * number);
						d.set(0);
						break;
					}

					putCommand("DELETE FROM HyperingEconomyDatabase.ticketdata WHERE time = " + time);
					addMoney(uuid, Double.valueOf(getMedianChain().getTicketPrice(time) / 10D * 9D).longValue() * maisu);
					d.set(number -= maisu);
					continue;
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
			wish(new Future(){

				@Override
				public void done() {
					removeTickets(uuid, d.get());
				}

			});
		}
	}

	@Override
	public long getTicketsValue(UUID uuid) {
		long sum = 0L;

		try(Connection con = source.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT time, number FROM HyperingEconomyDatabase.ticketdata WHERE uuid = '" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					sum += chain.getTicketPrice(result.getLong("time")) * result.getInt("number");

				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

		return sum;
	}

}
