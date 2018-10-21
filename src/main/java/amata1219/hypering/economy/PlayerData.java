package amata1219.hypering.economy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import amata1219.hypering.economy.bungeecord.BCSideManager;

public class PlayerData{

	private BCSideManager manager;

	private UUID uuid;
	private Map<ServerName, Long> money = new HashMap<>();
	private long tickets, ticketAmounts;

	public PlayerData(UUID uuid){
		manager = BCSideManager.getManager();

		this.uuid = uuid;

		Arrays.asList(ServerName.values()).forEach(serverName -> {
			money.put(serverName, manager.getMedian(serverName));

			manager.updateMedian(serverName);
		});
	}

	public static PlayerData load(UUID uuid, Map<ServerName, Long> money, long tickets, long ticketAmounts){
		PlayerData data = new PlayerData(uuid);

		data.manager = BCSideManager.getManager();

		data.uuid = uuid;
		data.money = money;
		data.tickets = tickets;
		data.ticketAmounts = ticketAmounts;

		return data;
	}

	public UUID getUniqueId(){
		return uuid;
	}

	public String getUUID(){
		return uuid.toString();
	}

	public void save(){
		manager.getMySQL().savePlayerData(this);
	}

	public long getMoney(ServerName name){
		return money.get(name);
	}

	public void setMoney(ServerName name, long money, boolean update){
		this.money.put(name, money < 0 ? 0 : money);

		if(update)
			manager.updateMedian(name);
	}

	public void addMoney(ServerName name, long money){
		setMoney(name, getMoney(name) + money, true);
	}

	public void removeMoney(ServerName name, long money){
		setMoney(name, getMoney(name) - money, true);
	}

	public void sendMoney(ServerName name, UUID to, long money){
		money = money > getMoney(name) ? getMoney(name) : money;

		PlayerData data = manager.getPlayerData(to);
		if(data == null)
			return;

		data.addMoney(name, money);
		data.save();

		removeMoney(name, money);
	}

	public String toMoneyText(){
		return getMoney(ServerName.main) + "," + getMoney(ServerName.mainflat) + "," + getMoney(ServerName.lgw);
	}

	public long getTickets(){
		return tickets;
	}

	public void setTickets(long tickets){
		this.tickets = tickets;
	}

	public void addTickets(long tickets){
		this.tickets += tickets;
	}

	public void removeTickets(long tickets){
		this.tickets = tickets > this.tickets ? 0 : this.tickets - tickets;
	}

	public long getTicketAmounts(){
		return ticketAmounts;
	}

	public void setTicketAmounts(long ticketAmounts){
		this.ticketAmounts = ticketAmounts;
	}

	public void addTicketAmounts(long ticketAmounts){
		this.ticketAmounts += ticketAmounts;
	}

	public void removeTicketAmounts(long ticketAmounts){
		this.ticketAmounts = ticketAmounts > this.ticketAmounts ? 0 : this.ticketAmounts - ticketAmounts;
	}

	public long getAmountPerTicket(){
		return ticketAmounts / tickets;
	}

	public void addTickets(long tickets, ServerName name){
		addTickets(tickets, manager.getTicketPrice(name) * tickets);
	}
	public void addTickets(long tickets, long amountPerTicket){
		addTickets(tickets);
		addTicketAmounts(amountPerTicket * tickets);

	}

	public void removeTicket(long tickets){
		tickets = tickets > this.tickets ? this.tickets : tickets;

		for(long l = tickets; l > 0; l--){
			removeTicketAmounts(getAmountPerTicket());
			removeTickets(1);
		}
	}

	public void buyTicket(ServerName name, long tickets, long amountPerTicket){
		setMoney(name, getMoney(name) - tickets * amountPerTicket, false);
		addTickets(tickets, amountPerTicket);
	}

	public void sellTicket(ServerName name, long tickets){
		tickets = tickets > getTickets() ? getTickets() : tickets;

		for(long l = tickets; l > 0; l--){
			setMoney(name, getMoney(name) - getAmountPerTicket(), false);
			removeTicket(1);
		}
	}

	public long getTotalAssets(ServerName name){
		return getMoney(name) + getTicketAmounts();
	}

}
