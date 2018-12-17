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

		return chain;
	}

	public long getMedian(long time){
		return (long) Getter.get("SELECT median FROM " + Database.getDatabaseName() + "." + table + " WHERE time <= " + time + " ORDER BY time DESC ", "median");
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
	}

}
