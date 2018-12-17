package amata1219.hypering.economy;

import java.util.UUID;

public class MoneyEditer {

	private ServerName serverName;
	private UUID uuid;

	public MoneyEditer(ServerName serverName, UUID uuid){
		this.serverName = serverName;
		this.uuid = uuid;
	}

	public void add(long increase){
		Database.getHyperingEconomyAPI().addMoney(serverName, uuid, increase);
	}

	public void remove(long decrease){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(!api.hasMoney(serverName, uuid, decrease))
			return;

		api.removeMoney(serverName, uuid, decrease);
	}

	public void send(UUID to, long money){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(!api.exist(to))
			return;

		if(!api.hasMoney(serverName, uuid, money))
			return;

		remove(money);
		api.addMoney(serverName, to, money);
	}

}
