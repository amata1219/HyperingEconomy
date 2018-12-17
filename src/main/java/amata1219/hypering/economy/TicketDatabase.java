package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TicketDatabase {

	public static long getTickets(UUID uuid){
		System.out.println(Getter.get("SELECT SUM(number) AS sum FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE uuid='" + uuid.toString() + "'", "sum"));

		return (long) Getter.get("SELECT SUM(number) AS sum FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE uuid='" + uuid.toString() + "'", "sum");
	}

	public static boolean hasTickets(UUID uuid, long threshold){
		return getTickets(uuid) >= threshold;
	}

	public static void addTickets(UUID uuid, long time, int increase){
		Database.putCommand("INSERT INTO " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " VALUES ('" + uuid.toString() + "'," + time + "," + increase + ")");

		for(ServerName serverName : Database.getEconomyServers())
			Database.getHyperingEconomyAPI().getMedianChain(serverName).flag();
	}

	public static void removeTickets(UUID uuid, int decrease){
		if(!hasTickets(uuid, decrease))
			return;

		long time = (long) Getter.get("SELECT time FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE uuid='" + uuid.toString() + "'", "time");
		int l = (int) Getter.get("SELECT number FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE time=" + time, "number");

		if(l > decrease){
			Database.putCommand("UPDATE " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " SET number=" + (l - decrease) + " WHERE time=" + time);
			return;
		}

		Database.putCommand("DELETE FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE time=" + time);

		if(l < decrease)
			TicketDatabase.removeTickets(uuid, decrease - l);
	}

	public static boolean canBuyTickets(ServerName serverName, UUID uuid, int threshold){
		return Database.getHyperingEconomyAPI().getMoney(serverName, uuid) >= Database.getHyperingEconomyAPI().getTicketPrice(serverName) * threshold;
	}

	public static void buyTickets(ServerName serverName, UUID uuid, int number){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		long price = number * api.getTicketPrice(serverName);
		if(!api.hasMoney(serverName, uuid, price))
			return;

		api.getMoneyEditer(serverName, uuid).remove(price);
		TicketDatabase.addTickets(uuid, System.nanoTime(), number);
	}

	public static boolean canCashTickets(UUID uuid, int threshold){
		return getTickets(uuid) >= threshold;
	}

	public static void cashTickets(ServerName serverName, UUID uuid, int number){
		if(!hasTickets(uuid, number))
			return;

		long time = (long) Getter.get("SELECT time FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE uuid='" + uuid.toString() + "'", "time");
		int l = (int) Getter.get("SELECT number FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE time=" + time, "number");

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(l > number){
			Database.putCommand("UPDATE " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " SET number=" + (l - number) + " WHERE time=" + time);
			api.getMoneyEditer(serverName, uuid).add(Double.valueOf(api.getMedianChain(serverName).getTicketPrice(time) * number / 10D * 9D).longValue());
			return;
		}

		Database.putCommand("DELETE FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE time=" + time);
		api.getMoneyEditer(serverName, uuid).add(Double.valueOf(api.getMedianChain(serverName).getTicketPrice(time) / 1000 * l / 10D * 9D).longValue());

		if(l < number)
			TicketDatabase.cashTickets(serverName, uuid, number - l);
	}

	public static long getTicketsValue(ServerName serverName, UUID uuid){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		MedianChain chain = api.getMedianChain(serverName);

		long sum = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT time, number FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE uuid='" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					sum += chain.getTicketPrice(result.getLong("time")) * result.getInt("number");
					break;
				}
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return sum;
	}

	public static long getTicketsValue(ServerName serverName, String uuid){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		MedianChain chain = api.getMedianChain(serverName);

		long sum = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT time, number FROM " + Database.getDatabaseName() + "." + Database.getTicketDataTableName() + " WHERE uuid='" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					sum += chain.getTicketPrice(result.getLong("time")) * result.getInt("number");
					break;
				}
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return sum;
	}

}
