package amata1219.hypering.economy;

public class MedianChain {

	private String table;

	private boolean flag;

	private long time;
	private long latest;

	private MedianChain(){

	}

	public static MedianChain load(ServerName serverName){
		MedianChain chain = new MedianChain();

		chain.table = serverName.name().toLowerCase() + "_medianchain";

		chain.latest = Getter.getLong("SELECT COUNT(time) AS count FROM HyperingEconomyDatabase." + chain.table, "count") > 0 ? chain.getMedian(System.nanoTime()) : 5000L;

		return chain;
	}

	public long getMedian(long time){
		long l = Getter.getLong("SELECT median FROM HyperingEconomyDatabase." + table + " WHERE time <= " + time + " ORDER BY time DESC", "median");
		return l == 0 ? 5000L : (long) l;
	}

	public long getTicketPrice(long time){
		return getMedian(time) / 1000;
	}

	public void flag(){
		if(!flag)
			flag = true;
	}

	public void update(long median){
		if(latest == median)
			return;

		if(!flag){
			time = System.nanoTime();
			latest = median;
			return;
		}

		if(latest > 0 && time > 0)
			Database.putCommand("INSERT INTO " + Database.getDatabaseName() + "." + table + " VALUES (" + time + "," + latest + ")");

		time = System.nanoTime();
		latest = median;
		flag = false;
	}

}
