package amata1219.hypering.economy;

import java.util.UUID;

public interface HyperingEconomyAPI {

	void updateMedian(ServerName serverName);

	long getMedian(ServerName serverName);

	MedianChain getMedianChain(ServerName serverName);

	long getTicketPrice(ServerName serverName);

	MoneyRanking getMoneyRanking(ServerName serverName);

	boolean exist(UUID uuid);

	long existSize();

	boolean active(UUID uuid);

	long activeSize();

	void updateLastPlayed(UUID uuid);

	long getLastPlayed(UUID uuid);

	long getMoney(ServerName serverName, UUID uuid);

	boolean hasMoney(ServerName serverName, UUID uuid, long threshold);

	void setMoney(ServerName serverName, UUID uuid, long money);

	void addMoney(ServerName serverName, UUID uuid, long increase);

	void removeMoney(ServerName serverName, UUID uuid, long decrease);

	MoneyEditer getMoneyEditer(ServerName serverName, UUID uuid);

	long getTickets(UUID uuid);

	boolean hasTickets(UUID uuid, long threshold);

	void addTickets(UUID uuid, long increase);

	void removeTickets(ServerName serverName, UUID uuid, long decrease);

	boolean canBuyTickets(ServerName serverName, UUID uuid, long number);

	void buyTickets(ServerName serverName, UUID uuid, long number);

	boolean canCashTickets(UUID uuid, long number);

	void cashTickets(ServerName serverName, UUID uuid, long number);

	long getTicketsValue(ServerName serverName, UUID uuid);
}
