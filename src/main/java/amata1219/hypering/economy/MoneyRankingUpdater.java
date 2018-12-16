package amata1219.hypering.economy;

public class MoneyRankingUpdater implements Runnable {

	public MoneyRankingUpdater(){

	}

	@Override
	public void run() {
		for(ServerName serverName : Database.getEconomyServers())
			Database.getDatabase().updateMoneyRanking(serverName);
	}

}
