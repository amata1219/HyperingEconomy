package amata1219.hypering.economy.spigot;

import java.util.UUID;

import org.bukkit.entity.Player;

import amata1219.hypering.economy.callback.Callback;
import amata1219.hypering.economy.callback.Result;

public interface SHyperingEconomyAPI{

	boolean isEnableEconomy();

	void getMoney(Player sender, UUID uuid, Callback<Result> callback);

	void hasMoney(Player sender, UUID uuid, long threshold, Callback<Result> callback);

	void setMoney(Player sender, UUID uuid, long money);

	void setMoney(UUID uuid, long money);

	void addMoney(Player sender, UUID uuid, long money);

	void addMoney(UUID uuid, long money);

	void removeMoney(Player sender, UUID uuid, long money);

	void removeMoney(UUID uuid, long money);

	void sendMoney(Player player, UUID sender, UUID receiver, long money);

	void sendMoney(UUID sender, UUID receiver, long money);

	void getMoneyRanking(Player sender, Callback<Result> callback);
	//uuid-money,uuid-money,uuid-money...

	void getNumberOfTickets(Player sender, UUID uuid, Callback<Result> callback);

	void addTicket(Player sender, UUID uuid, long numberOfTickets);

	void addTicket(UUID uuid, long numberOfTickets);

	void addTicket(Player sender, UUID uuid, long numberOfTickets, long pricePerTicket);

	void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket);

	void removeTicket(Player sender, UUID uuid, long numberOfTickets);

	void removeTicket(UUID uuid, long numberOfTickets);

	void buyTicket(Player sender, UUID uuid, long numberOfTickets);

	void buyTicket(UUID uuid, long numberOfTickets);

	void sellTicket(Player sender, UUID uuid, long numberOfTickets);

	void sellTicket(UUID uuid, long numberOfTickets);

	void getMedian(Player sender, Callback<Result> callback);

	void getTicketPrice(Player sender, Callback<Result> callback);

	void getNumberOfPlayerDataLoaded(Player sender, Callback<Result> callback);

}