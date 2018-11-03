package amata1219.hypering.economy.bungeecord;

import java.util.UUID;

import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;

public interface BCHyperingEconomyAPI {

	public PlayerData getPlayerData(UUID uuid);

	public long getMoney(ServerName name, UUID uuid);

	public boolean hasMoney(ServerName name, UUID uuid, long threshold);

	public void setMoney(ServerName name, UUID uuid, long money);

	public void addMoney(ServerName name, UUID uuid, long money);

	public void substractMoney(ServerName name, UUID uuid, long money);

	public void sendMoney(ServerName name, UUID sender, UUID receiver, long money);

	public String getMoneyRanking(ServerName name);
	//uuid-money,uuid-money,uuid-money...

	public long getNumberOfTickets(UUID uuid);

	public void addTicket(UUID uuid, long numberOfTickets, ServerName pricePerTicketFromTicket);

	public void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket);

	public void substractTicket(UUID uuid, long numberOfTickets);

	public boolean canBuyTicket(UUID uuid, ServerName where, long numberOfTickets);

	public void buyTicket(UUID uuid, ServerName where, long numberOfTickets);

	public boolean canSellTicket(UUID uuid, ServerName where, long numberOfTickets);

	public void sellTicket(UUID uuid, ServerName where, long numberOfTickets);

	public long getMedian(ServerName name);

	public long getTicketPrice(ServerName name);

	public int getNumberOfPlayerDataLoaded();

}