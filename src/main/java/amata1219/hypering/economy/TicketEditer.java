package amata1219.hypering.economy;

import java.util.UUID;

public class TicketEditer {

	private UUID uuid;

	public TicketEditer(UUID uuid){
		this.uuid = uuid;
	}

	public void add(ServerName serverName, long increase){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		api.addTickets(uuid, increase);
		api.addTicketsValue(uuid, increase * api.getMedian(serverName));
	}

	public void remove(ServerName serverName, long decrease){
		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();

		if(api.getTickets(uuid) < decrease){
			api.setTicketsValue(uuid, 0L);
			return;
		}

		for(long i = decrease; i > 0; i--){
			api.removeTickets(uuid, decrease);
			api.removeTicketsValue(uuid, decrease * api.getTicketsValuePerTicket(uuid));
		}
	}

	public void plus(ServerName serverName){
		add(serverName, 1L);
	}

	public void minus(ServerName serverName){
		remove(serverName, 1L);
	}

}
