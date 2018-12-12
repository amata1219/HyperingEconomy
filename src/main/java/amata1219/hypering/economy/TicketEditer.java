package amata1219.hypering.economy;

import java.util.UUID;

public class TicketEditer {

	private ServerName serverName;
	private UUID uuid;

	public TicketEditer(ServerName serverName, UUID uuid){
		this.serverName = serverName;
		this.uuid = uuid;
	}

	public void add(long increase){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		api.addTickets(uuid, increase);
		api.addTicketsValue(uuid, increase * api.getMedian(serverName));
	}

	public void remove(long decrease){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		long number = api.getTickets(uuid);

		if(number < decrease){
			api.setTicketsValue(uuid, 0L);
			return;
		}

		api.removeTickets(uuid, decrease);

		long value = api.getTicketsValue(uuid);
		for(long i = decrease; i > 0; i--)
			value -= value / (number - i);

		api.setTicketsValue(uuid, value);
	}

	public void buy(long number){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(!canBuy(number))
			return;

		api.removeMoney(serverName, uuid, number * api.getMedian(serverName));

		add(number);
	}

	public boolean canBuy(long number){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		return number * api.getMedian(serverName) <= api.getMoney(serverName, uuid);
	}

	public void cash(long number){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(!canCash(number))
			return;

		api.addMoney(serverName, uuid, Double.valueOf(number * api.getMedian(serverName) / 10D * 9D).longValue());

		remove(number);
	}

	public boolean canCash(long number){
		return Database.getHyperingEconomyAPI().hasTickets(uuid, number);
	}

}
