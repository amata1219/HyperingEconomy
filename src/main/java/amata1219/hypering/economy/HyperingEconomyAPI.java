package amata1219.hypering.economy;

import java.util.UUID;

public interface HyperingEconomyAPI {

	HyperingEconomyAPI getHyperingEconomyAPI();

	void updateMedian();

	long getMedian();

	MedianChain getMedianChain();

	long getTicketPrice();

	boolean exist(UUID uuid);

	long existSize();

	boolean active(UUID uuid);

	long activeSize();

	void updateLastPlayed(UUID uuid);

	long getLastPlayed(UUID uuid);

	long getMoney(UUID uuid);

	boolean hasMoney(UUID uuid, long threshold);

	void setMoney(UUID uuid, long money);

	void addMoney(UUID uuid, long increase);

	void removeMoney(UUID uuid, long decrease);

	void sendMoney(UUID from, UUID to, long money);

	long getTickets(UUID uuid);

	boolean hasTickets(UUID uuid, long threshold);

	void addTickets(UUID uuid, long increase);

	void removeTickets(UUID uuid, long decrease);

	boolean canBuyTickets(UUID uuid, long number);

	void buyTickets(UUID uuid, long number);

	boolean canCashTickets(UUID uuid, long number);

	void cashTickets(UUID uuid, long number);

	long getTicketsValue(UUID uuid);
}
