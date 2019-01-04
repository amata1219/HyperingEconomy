package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MedianChain {

	public static final long DEFAULT_VALUE = 100000L;

	private String table;

	private boolean fix;

	private boolean flag;

	private long time;
	private long latest;

	private Map<Long, Long> chain = new HashMap<>();

	private MedianChain(){

	}

	public static MedianChain load(ServerName serverName){
		MedianChain chain = new MedianChain();

		chain.table = serverName.name().toLowerCase() + "_medianchain";

		chain.chain.put(0L, DEFAULT_VALUE);

		try(Connection con = SQL.getSQL().getSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM HyperingEconomyDatabase." + chain.table)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					chain.chain.put(result.getLong("time"), result.getLong("median"));

				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

		chain.time = System.nanoTime();
		chain.latest = chain.getMedian(System.nanoTime());

		return chain;
	}

	public long getMedian(long time){
		long t = 0L;
		for(long k : chain.keySet()){
			if(k <= time || k > t)
				t = k;
		}

		return chain.containsKey(t) ? chain.get(t) : DEFAULT_VALUE;
	}

	public long getTicketPrice(long time){
		return getMedian(time) / 1000;
	}

	public void setFix(boolean fix){
		this.fix = fix;
	}

	public boolean isFix(){
		return fix;
	}

	public void flag(){
		if(!flag)
			flag = true;
	}

	public void update(long median){
		if(latest == median)
			return;

		if(!flag || latest == getMedian(System.nanoTime())){
			time = System.nanoTime();
			latest = median;
			return;
		}

		chain.put(time, latest);
		SQL.getSQL().putCommand("INSERT INTO HyperingEconomyDatabase." + SQL.getSQL().name + " VALUES (" + time + "," + latest + ")");

		time = System.nanoTime();
		latest = median;
		flag = false;
	}

}
