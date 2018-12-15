package amata1219.hypering.economy;

import java.util.UUID;

public interface HyperingEconomyAPI {

	void updateMedian(ServerName serverName);

	long getMedian(ServerName serverName);

	MedianChain getMedianChain(ServerName serverName);

	long getTicketPrice(ServerName serverName);

	MoneyRanking getMoneyRanking(ServerName serverName);

	boolean exist(UUID uuid);

	int existSize();

	boolean active(UUID uuid);

	int activeSize();

	long getMoney(ServerName serverName, UUID uuid);

	boolean hasMoney(ServerName serverName, UUID uuid, long threshold);

	void setMoney(ServerName serverName, UUID uuid, long money);

	void addMoney(ServerName serverName, UUID uuid, long increase);

	void removeMoney(ServerName serverName, UUID uuid, long decrease);

	MoneyEditer getMoneyEditer(ServerName serverName, UUID uuid);

	long getTickets(UUID uuid);

	boolean hasTickets(UUID uuid, long threshold);

	void addTickets(UUID uuid, int increase);

	void removeTickets(UUID uuid, int decrease);

	void buyTickets(ServerName serverName, UUID uuid, int number);

	void cashTickets(ServerName serverName, UUID uuid, int number);
}
