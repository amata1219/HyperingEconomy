package amata1219.hypering.economy.bungeecord;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import amata1219.hypering.economy.Channel;
import amata1219.hypering.economy.HyperingEconomyChannel;
import amata1219.hypering.economy.MySQL;
import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;
import amata1219.hypering.economy.Util;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class BCManager implements Listener, BCHyperingEconomyAPI {
	private static BCManager manager;

	private Map<UUID, PlayerData> players = new HashMap<>(), withinMonth = new HashMap<>();

	private Map<ServerName, String> money_rankings = new HashMap<>();

	private List<ScheduledTask> taskList = new ArrayList<>();

	private Map<ServerName, Long> median = new HashMap<>();

	private int saveInterval = 10, enableMedian = 2;
	private long initialPossesionMoney = 5000;

	public BCManager(){
		manager = this;

		Configuration config = BCHyperingEconomy.getPlugin().getConfig();

		MySQL.load(config.getString("MySQL.host"), config.getInt("MySQL.port"), config.getString("MySQL.database"),
				config.getString("MySQL.username"), config.getString("MySQL.password"), config.getString("MySQL.table"));

		saveInterval = config.getInt("SaveInterval");
		enableMedian = config.getInt("EnableMedian");

		initialPossesionMoney = config.getLong("InitialPossessionMoney");

		loadPlayerDataOfOnlinePlayers();
		loadWithinMonth();

		for(ServerName name : ServerName.values()){
			median.put(name, 0L);

			updateMedian(name);
		}
	}

	public static BCManager getManager(){
		return manager;
	}

	@Override
	public PlayerData getPlayerData(UUID uuid){
		if(players.containsKey(uuid))
			return players.get(uuid);

		if(withinMonth.containsKey(uuid))
			return withinMonth.get(uuid);

		return null;
	}

	public Map<UUID, PlayerData> getPlayerDataMap(){
		return players;
	}

	public Map<UUID, PlayerData> getWithinMonthMap(){
		return withinMonth;
	}

	public Map<ServerName, String> getMoneyRankingMap(){
		return money_rankings;
	}

	public void startTaskRunnable(){
		BCHyperingEconomy.getPlugin().getProxy().getScheduler().schedule(BCHyperingEconomy.getPlugin(), new SaveTaskRunnable(), 0, saveInterval, TimeUnit.MINUTES);
	}

	public void stopTaskRunnable(){
		taskList.forEach(task -> task.cancel());
	}

	public void loadPlayerDataOfOnlinePlayers(){
		BCHyperingEconomy.getPlugin().getProxy().getPlayers().forEach(player ->{
			UUID uuid = player.getUniqueId();
			players.put(uuid, MySQL.getPlayerData(uuid));
		});
	}

	public void loadWithinMonth(){
		withinMonth = MySQL.getWithinMonth();
	}

	public long getTicketPrice(ServerName name){
		if(getNumberOfPlayerDataLoaded() < enableMedian)
			return initialPossesionMoney / 1000;

		return median.get(name) / 1000;
	}

	public long getMedian(ServerName name){
		if(getNumberOfPlayerDataLoaded() < enableMedian)
			return initialPossesionMoney;

		return median.get(name);
	}

	public int getNumberOfPlayerDataLoaded(){
		return players.size() + withinMonth.size();
	}

	public void updateMedian(ServerName name){
		List<Long> list = new ArrayList<>();

		players.values().stream().filter(data -> data.getTotalAssets(name) > 0).forEach(data -> list.add(data.getTotalAssets(name)));

		withinMonth.values().stream().filter(data -> data.getTotalAssets(name) > 0).forEach(data -> list.add(data.getTotalAssets(name)));

		if(list.isEmpty()){
			median.put(name, 0L);
			return;
		}

		Collections.sort(list);
		int n = list.size();

		if(n == 0)
			median.put(name, 0L);
		else if(n % 2 == 0)
			median.put(name, (Long) ((list.get(n / 2 - 1) + list.get(n / 2)) / 2));
		else
			median.put(name, (Long) (list.get((n) / 2)));
	}

	public void loadPlayerData(ProxiedPlayer player){
		UUID uuid = player.getUniqueId();

		PlayerData data = MySQL.getPlayerData(uuid);
		MySQL.saveLastLogined(data);

		if(withinMonth.containsKey(uuid)){
			withinMonth.get(uuid).save();
			withinMonth.remove(uuid);
		}
		players.put(uuid, data);
	}

	public void unloadPlayerData(ProxiedPlayer player){
		UUID uuid = player.getUniqueId();
		PlayerData data = players.get(uuid);
		data.save();
		players.remove(uuid);
		withinMonth.put(uuid, data);
	}

	@EventHandler
	public void onLogin(PostLoginEvent e){
		loadPlayerData(e.getPlayer());

		//DEBUG↓
		BCHyperingEconomy.getPlugin().getConfig().set("DEBUG." + e.getPlayer().getName(), e.getPlayer().getUniqueId().toString());
	}

	@EventHandler
	public void onLogout(ServerDisconnectEvent e){
		unloadPlayerData(e.getPlayer());
	}

	@EventHandler
	public void onReceive(PluginMessageEvent e){
		if(!e.getTag().equals("BungeeCord"))
			return;

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(e.getData()));
		HyperingEconomyChannel channel = HyperingEconomyChannel.newInstance(stream);

		channel.read(stream);
		if(channel.isNull() || !channel.getMessage().equals(HyperingEconomyChannel.PACKET_ID))
			return;

		channel.read(stream);
		if(channel.isNull())
			return;

		String sub = channel.getMessage();

		channel.read(stream);
		if(channel.isNull())
			return;

		UUID uuid = UUID.fromString(channel.getMessage());

		PlayerData data = getPlayerData(uuid);

		boolean save = false;

		if(data == null){
			data = MySQL.getPlayerData(uuid);
			save = true;
		}

		ServerInfo server = BCHyperingEconomy.getPlugin().getProxy().getPlayer(uuid).getServer().getInfo();
		ServerName name = ServerName.valueOf(server.getName());

		switch(sub){
		case Channel.GET_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId1 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_GET_MONEY, seqId1, String.valueOf(data.getMoney(name))));
			break;
		case Channel.HAS_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			long threshold = Long.valueOf(channel.getMessage()).longValue();

			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId2 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_HAS_MONEY, seqId2, String.valueOf(data.getMoney(name) >= threshold)));
			break;
		case Channel.SET_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			long money3 = Long.valueOf(channel.getMessage()).longValue();

			data.setMoney(name, money3, true, save);
			break;
		case Channel.ADD_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			long money4 = Long.valueOf(channel.getMessage()).longValue();

			data.addMoney(name, money4, save);
			break;
		case Channel.REMOVE_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			long money5 = Long.valueOf(channel.getMessage()).longValue();

			data.removeMoney(name, money5, save);
			break;
		case Channel.SEND_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			UUID sender6 = UUID.fromString(channel.getMessage());

			channel.read(stream);
			if(channel.isNull())
				return;

			long money6 = Long.valueOf(channel.getMessage()).longValue();

			data.sendMoney(name, sender6, money6, true);
			break;
		case Channel.GET_MONEY_RANKING:
			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId7 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_MONEY_RANKING, seqId7, getMoneyRanking(name)));
			break;
		case Channel.GET_TICKETS:
			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId8 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_GET_TICKETS, seqId8, String.valueOf(data.getTickets())));
			break;
		case Channel.ADD_TICKET1:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number9 = Long.valueOf(channel.getMessage()).longValue();

			data.addTickets(number9, name, save);
			break;
		case Channel.ADD_TICKET2:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number10 = Long.valueOf(channel.getMessage()).longValue();

			channel.read(stream);
			if(channel.isNull())
				return;

			long ppt10 = Long.valueOf(channel.getMessage()).longValue();

			data.addTickets(number10, ppt10, save);
			break;
		case Channel.REMOVE_TICKET:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number11 = Long.valueOf(channel.getMessage()).longValue();

			data.removeTicket(number11, save);
			break;
		case Channel.CAN_BUY_TICKET:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number17 = Long.valueOf(channel.getMessage()).longValue();

			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId17 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_HAS_MONEY, seqId17, String.valueOf(data.getMoney(name) >= getTicketPrice(name) * number17)));
			break;
		case Channel.BUY_TICKET:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number12 = Long.valueOf(channel.getMessage()).longValue();

			data.buyTicket(name, number12, getTicketPrice(name), save);
			break;
		case Channel.CAN_SELL_TICKET:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number18 = Long.valueOf(channel.getMessage()).longValue();

			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId18 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_HAS_MONEY, seqId18, String.valueOf(data.getTickets() >= number18)));
			break;
		case Channel.SELL_TICKET:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number13 = Long.valueOf(channel.getMessage()).longValue();

			data.sellTicket(name, number13, save);
			break;
		case Channel.GET_MEDIAN:
			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId14 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_GET_MEDIAN, seqId14, String.valueOf(getMedian(name))));
			break;
		case Channel.GET_TICKET_PRICE:
			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId15 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_GET_TICKET_PRICE, seqId15, String.valueOf(getTicketPrice(name))));
			break;
		case Channel.GET_NUMBER_OF_PLAYER_DATA_LOADED:
			channel.read(stream);
			if(channel.isNull())
				return;

			String seqId16 = channel.getMessage();

			server.sendData("BungeeCord", Util.toByteArray(Channel.RETURN_GET_NUMBER_OF_PLAYER_DATA_LOADED, seqId16, String.valueOf(getNumberOfPlayerDataLoaded())));
			break;
		default:
			break;
		}

	}

	@Override
	public long getMoney(ServerName name, UUID uuid){
		return getPlayerData(uuid).getMoney(name);
	}

	@Override
	public boolean hasMoney(ServerName name, UUID uuid, long threshold){
		return getPlayerData(uuid).getMoney(name) >= threshold;
	}

	@Override
	public void setMoney(ServerName name, UUID uuid, long money){
		getPlayerData(uuid).setMoney(name, money, true, false);
	}

	@Override
	public void addMoney(ServerName name, UUID uuid, long money){
		getPlayerData(uuid).addMoney(name, money, false);
	}

	@Override
	public void substractMoney(ServerName name, UUID uuid, long money){
		getPlayerData(uuid).removeMoney(name, money, false);
	}

	@Override
	public void sendMoney(ServerName name, UUID sender, UUID receiver, long money){
		getPlayerData(sender).sendMoney(name, receiver, money, false);
	}

	@Override
	public String getMoneyRanking(ServerName name){
		return money_rankings.get(name);
	}

	@Override
	public long getNumberOfTickets(UUID uuid){
		return getPlayerData(uuid).getTickets();
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets, ServerName pricePerTicketFromServer){
		getPlayerData(uuid).addTickets(numberOfTickets, pricePerTicketFromServer, false);
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket){
		getPlayerData(uuid).addTickets(numberOfTickets, pricePerTicket, false);
	}

	@Override
	public void substractTicket(UUID uuid, long numberOfTickets){
		getPlayerData(uuid).removeTicket(numberOfTickets, false);
	}

	@Override
	public void buyTicket(UUID uuid, ServerName where, long numberOfTickets){
		getPlayerData(uuid).buyTicket(where, numberOfTickets, getMedian(where), false);
	}

	@Override
	public void sellTicket(UUID uuid, ServerName where, long numberOfTickets){
		getPlayerData(uuid).sellTicket(where, numberOfTickets, false);
	}

	@Override
	public boolean canBuyTicket(UUID uuid, ServerName where, long numberOfTickets) {
		return getPlayerData(uuid).getMoney(where) >= getTicketPrice(where) * numberOfTickets;
	}

	@Override
	public boolean canSellTicket(UUID uuid, ServerName where, long numberOfTickets) {
		return getPlayerData(uuid).getTickets() >= numberOfTickets;
	}

}