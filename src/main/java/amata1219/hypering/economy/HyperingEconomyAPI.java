package amata1219.hypering.economy;

import java.util.UUID;

public interface HyperingEconomyAPI {

	void updateMedian(ServerName serverName);

	long getMedian(ServerName serverName);

	//void save(UUID uuid);

	boolean exist(UUID uuid);

	int existSize();

	boolean active(UUID uuid);

	int activeSize();

	long getMoney(ServerName serverName, UUID uuid);

	boolean hasMoney(ServerName serverName, UUID uuid, long threshold);

	void setMoney(ServerName serverName, UUID uuid, long money);

	void addMoney(ServerName serverName, UUID uuid, long increase);

	void removeMoney(ServerName serverName, UUID uuid, long decrease);

	long getTickets(UUID uuid);

	boolean hasTickets(UUID uuid, long threshold);

	void addTickets(UUID uuid, long increase);

	void removeTickets(UUID uuid, long decrease);

	long getTicketsValue(UUID uuid);

	long getTicketsValuePerTicket(UUID uuid);

}
