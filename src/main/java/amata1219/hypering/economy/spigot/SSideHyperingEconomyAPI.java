package amata1219.hypering.economy.spigot;

import java.util.UUID;

import org.bukkit.entity.Player;

import amata1219.hypering.economy.callback.Callback;

public interface SSideHyperingEconomyAPI{

	public boolean isEnableEconomy();

	public void getMoney(Player sender, UUID uuid, Callback callback);

	public void hasMoney(Player sender, UUID uuid, long threshold, Callback callback);

	public void setMoney(Player sender, UUID uuid, long money);

	public void setMoney(UUID uuid, long money);

	public void addMoney(Player sender, UUID uuid, long money);

	public void addMoney(UUID uuid, long money);

	public void substractMoney(Player sender, UUID uuid, long money);

	public void substractMoney(UUID uuid, long money);

	public void sendMoney(Player player, UUID sender, UUID receiver, long money);

	public void sendMoney(UUID sender, UUID receiver, long money);

	public void getMoneyRanking(Player sender, Callback callback);
	//uuid-money,uuid-money,uuid-money…
	public void getNumberOfTickets(Player sender, UUID uuid, Callback callback);

	public void addTicket(Player sender, UUID uuid, long numberOfTickets);

	public void addTicket(UUID uuid, long numberOfTickets);

	public void addTicket(Player sender, UUID uuid, long numberOfTickets, long pricePerTicket);

	public void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket);

	public void substractTicket(Player sender, UUID uuid, long numberOfTickets);

	public void substractTicket(UUID uuid, long numberOfTickets);

	public void buyTicket(Player sender, UUID uuid, long numberOfTickets);

	public void buyTicket(UUID uuid, long numberOfTickets);

	public void sellTicket(Player sender, UUID uuid, long numberOfTickets);

	public void sellTicket(UUID uuid, long numberOfTickets);

	public void getMedian(Player sender, Callback callback);

	public void getTicketPrice(Player sender, Callback callback);

	public void getNumberOfPlayerDataLoaded(Player sender, Callback callback);

}