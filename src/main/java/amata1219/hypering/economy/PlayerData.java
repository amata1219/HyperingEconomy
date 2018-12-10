package amata1219.hypering.economy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import amata1219.hypering.economy.bungeecord.BCManager;

public class PlayerData {

	private UUID uuid;
	private Map<ServerName, Long> money = new HashMap<>();
	private long tickets, ticketAmounts;

	private PlayerData(){

	}

	public PlayerData(UUID uuid){
		this.uuid = uuid;

		BCManager manager = BCManager.getManager();

		Arrays.asList(ServerName.values()).forEach(serverName -> {
			money.put(serverName, manager.getMedian(serverName));
			manager.updateMedian(serverName);
		});
	}

	public static PlayerData load(UUID uuid, Map<ServerName, Long> money, long tickets, long ticketAmounts){
		PlayerData data = new PlayerData();

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
		MySQL.savePlayerData(this);
	}

	public long getMoney(ServerName name){
		return money.get(name);
	}

	public void setMoney(ServerName name, long money, boolean save){
		this.money.put(name, money < 0 ? 0 : money);

		BCManager.getManager().updateMedian(name);

		if(save)
			save();
	}

	public void setMoneyNonUpdate(ServerName name, long money, boolean save){
		this.money.put(name, money < 0 ? 0 : money);

		//BCManager.getManager().updateMedian(name);

		if(save)
			save();
	}

	public void addMoney(ServerName name, long money, boolean save){
		long l = 0;

		try{
			l = getMoney(name) + money;
		}catch(Exception e){
			l = Long.MAX_VALUE;
		}

		setMoney(name, l, save);
	}

	public void removeMoney(ServerName name, long money, boolean save){
		setMoney(name, getMoney(name) - money, save);
	}

	public void sendMoney(ServerName name, UUID to, long money, boolean save){
		money = money > getMoney(name) ? getMoney(name) : money;

		PlayerData data = BCManager.getManager().getPlayerData(to);
		if(data == null)
			return;

		data.addMoney(name, money, save);
		if(!save)
			data.save();

		removeMoney(name, money, save);
	}

	public String toMoneyText(){
		return getMoney(ServerName.main) + "," + getMoney(ServerName.pata);
	}

	public long getTickets(){
		return tickets;
	}

	public void setTickets(long tickets, boolean save){
		this.tickets = tickets;

		if(save)
			save();
	}

	public void addTickets(long tickets, boolean save){
		try{
			this.tickets += tickets;
		}catch(Exception e){
			this.tickets = Long.MAX_VALUE;
		}

		if(save)
			save();
	}

	public void removeTickets(long tickets, boolean save){
		this.tickets = tickets > this.tickets ? 0 : this.tickets - tickets;

		if(save)
			save();
	}

	public long getTicketAmounts(){
		return ticketAmounts;
	}

	public void setTicketAmounts(long ticketAmounts, boolean save){
		this.ticketAmounts = ticketAmounts;

		if(save)
			save();
	}

	public void addTicketAmounts(long ticketAmounts, boolean save){
		try{
			this.ticketAmounts += ticketAmounts;
		}catch(Exception e){
			this.ticketAmounts = Long.MAX_VALUE;
		}

		if(save)
			save();
	}

	public void removeTicketAmounts(long ticketAmounts, boolean save){
		this.ticketAmounts = ticketAmounts > this.ticketAmounts ? 0 : this.ticketAmounts - ticketAmounts;

		if(save)
			save();
	}

	public long getAmountPerTicket(){
		if(ticketAmounts == 0 || tickets == 0)
			return 0;

		return ticketAmounts / tickets;
	}

	public void addTickets(long tickets, ServerName name, boolean save){
		addTickets(tickets, BCManager.getManager().getTicketPrice(name) * tickets, save);
	}

	public void addTickets(long tickets, long amountPerTicket, boolean save){
		addTickets(tickets, false);
		addTicketAmounts(amountPerTicket * tickets, save);

		for(ServerName name  : ServerName.values())
			BCManager.getManager().updateMedian(name);
	}

	public void removeTicket(long tickets, boolean save, boolean update){
		tickets = tickets > this.tickets ? 0 : tickets;

		for(long l = tickets; l > 0; l--){
			removeTicketAmounts(getAmountPerTicket(), false);
			removeTickets(1, false);
		}

		if(save)
			save();

		if(update){
			for(ServerName name  : ServerName.values())
				BCManager.getManager().updateMedian(name);
		}
	}

	public void buyTicket(ServerName name, long tickets, long amountPerTicket, boolean save){
		setMoney(name, getMoney(name) - tickets * amountPerTicket, false);
		addTickets(tickets, amountPerTicket, save);
	}

	public void sellTicket(ServerName name, long tickets, boolean save){
		tickets = tickets > getTickets() ? getTickets() : tickets;

		for(long l = tickets; l > 0; l--){
			double d = getAmountPerTicket() / 10D * 9D;
			setMoneyNonUpdate(name, getMoney(name) + (long) d, false);
			removeTicket(1, false, false);
		}

		BCManager.getManager().updateMedian(name);

		if(save)
			save();
	}

	public long getTotalAssets(ServerName name){
		return getMoney(name) + getTicketAmounts();
	}

	public boolean isNothingTotalAssets(ServerName name){
		return getTotalAssets(name) <= 0;
	}

}
