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
import amata1219.hypering.economy.MySQL;
import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;
import amata1219.hypering.economy.Utility;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class BCSideManager implements Listener, BCSideHyperingEconomyAPI{

	/*
	 * メインフラット、買った土地に建物を建ててから転売が出来るのと、運営に売却して買った時のチケットをもらってフラットに出来る機能
	 */

	private BCSideHyperingEconomy plugin;
	private static BCSideManager manager;

	private MySQL sql;

	private Map<UUID, PlayerData> playerdata = new HashMap<>();
	private Map<UUID, PlayerData> within_month = new HashMap<>();;
	private Map<UUID, PlayerData> will_save = new HashMap<>();;

	private Map<ServerName, String> money_rankings = new HashMap<>();;

	private List<ScheduledTask> tasklist = new ArrayList<>();

	private Map<ServerName, List<Long>> for_calc = new HashMap<>();;
	private Map<ServerName, Long> median = new HashMap<>();;

	private int save_interval = 10, enable_median = 2;
	private long initial_possesion_money = 5000;

	public BCSideManager(BCSideHyperingEconomy plugin){
		manager = this;
		this.plugin = plugin;
		Configuration config = plugin.getConfig();
		sql = new MySQL(this, config.getString("MySQL.host"), config.getInt("MySQL.port"), config.getString("MySQL.database"), config.getString("MySQL.username"), config.getString("MySQL.password"), config.getString("MySQL.table"));
		if(!sql.connect()){
			System.out.print("BCSideManager is invalid");
		}
		save_interval = config.getInt("SaveInterval");
		enable_median = config.getInt("EnableMedian");
		initial_possesion_money = config.getLong("InitialPossessionMoney");
		loadPlayerDataOfOnlinePlayers();
		loadPlayerDataOfLastJoinedInWithinMonthPlayers();
		for(ServerName name : ServerName.values()){
			for_calc.put(name, new ArrayList<Long>());
			updateMedian(name);
		}
	}

	@Override
	public BCSideHyperingEconomy getPlugin(){
		return plugin;
	}

	public static BCSideManager getManager(){
		return manager;
	}

	@Override
	public MySQL getMySQL(){
		return sql;
	}

	@Override
	public Configuration getConfig(){
		return plugin.getConfig();
	}

	@Override
	public PlayerData getPlayerData(UUID uuid){
		PlayerData data = getPlayerDataFromMaps(uuid);
		if(data != null){
			return data;
		}
		data = sql.getPlayerData(uuid);
		will_save.put(uuid, data);
		return data;
	}

	private PlayerData getPlayerDataFromMaps(UUID uuid){
		if(playerdata.containsKey(uuid)){
			return playerdata.get(uuid);
		}else if(within_month.containsKey(uuid)){
			return within_month.get(uuid);
		}
		return null;
	}

	@Override
	public PlayerData getPlayerData(ProxiedPlayer player){
		return getPlayerData(player.getUniqueId());
	}

	public Map<UUID, PlayerData> getPlayerDataMap(){
		return playerdata;
	}

	public Map<UUID, PlayerData> getWithinMonthMap(){
		return within_month;
	}

	public Map<UUID, PlayerData> getWillSaveMap(){
		return will_save;
	}

	public Map<ServerName, String> getMoneyRankingMap(){
		return money_rankings;
	}

	public void startTaskRunnable(){
		plugin.getProxy().getScheduler().schedule(plugin, new PlayerDataSaveTaskRunnable(this), 0, save_interval, TimeUnit.MINUTES);
	}

	public void stopTaskRunnable(){
		tasklist.forEach(task -> task.cancel());
	}

	public void loadPlayerDataOfOnlinePlayers(){
		plugin.getProxy().getPlayers().forEach(player ->{
			playerdata.put(player.getUniqueId(), sql.getPlayerData(player.getUniqueId()));
		});
	}

	public void loadPlayerDataOfLastJoinedInWithinMonthPlayers(){
		within_month = sql.getPlayerDataOfLastJoinedInWithinMonthPlayers();
	}

	public long getTicketPrice(ServerName name){
		if(getNumberOfPlayerDataLoaded() < enable_median){
			return initial_possesion_money / 1000;
		}
		return median.get(name) / 1000;
	}

	public long getMedian(ServerName name){
		if(getNumberOfPlayerDataLoaded() < enable_median){
			return initial_possesion_money;
		}
		return median.get(name);
	}

	public int getNumberOfPlayerDataLoaded(){
		return playerdata.size() + within_month.size();
	}

	public void updateMedian(ServerName name){
		List<Long> calc = for_calc.get(name);
		for(PlayerData data : playerdata.values()){
			if(data.getTotalAssets(name) > 0){
				calc.add(data.getTotalAssets(name));
			}
		}
		for(PlayerData data : within_month.values()){
			if(data.getTotalAssets(name) > 0){
				calc.add(data.getTotalAssets(name));
			}
		}
		if(calc.isEmpty()){
			median.put(name, 0L);
			return;
		}
		Collections.sort(calc);
		int n = calc.size();
		if(n == 0){
			median.put(name, 0L);
			return;
		}
		if(n % 2 == 0){
			median.put(name, (Long) ((calc.get(n / 2 - 1) + calc.get(n / 2)) / 2));
		}else{
			median.put(name, (Long) (calc.get((n) / 2)));
		}
		calc.clear();
	}

	public void loadPlayerData(ProxiedPlayer player){
		UUID uuid = player.getUniqueId();
		PlayerData data = sql.getPlayerData(uuid);
		sql.saveLastLogined(data);
		if(within_month.containsKey(uuid)){
			within_month.get(uuid).save();
			within_month.remove(uuid);
		}else if(will_save.containsKey(uuid)){
			will_save.get(uuid).save();
			will_save.remove(uuid);
		}
		playerdata.put(uuid, data);
	}

	public void unloadPlayerData(ProxiedPlayer player){
		UUID uuid = player.getUniqueId();
		PlayerData data = playerdata.get(uuid);
		data.save();
		playerdata.remove(uuid);
		within_month.put(uuid, data);
	}

	@EventHandler
	public void onLogin(PostLoginEvent e){
		loadPlayerData(e.getPlayer());
	}

	@EventHandler
	public void onLogout(ServerDisconnectEvent e){
		unloadPlayerData(e.getPlayer());
	}

	@EventHandler
	public void onReceive(PluginMessageEvent e){
		if(!e.getTag().equals("BungeeCord"))return;
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(e.getData()));
		String ch = Utility.getString(input);
		UUID sender = UUID.fromString(Utility.getString(input));
		UUID uuid = UUID.fromString(Utility.getString(input));
		PlayerData data = getPlayerData(uuid);
		ServerInfo server = plugin.getProxy().getPlayer(uuid).getServer().getInfo();
		ServerName name = ServerName.valueOf(server.getName());
		if(ch.equals(Channel.getMoney.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnMoney, sender, String.valueOf(data.getMoney(name))));
		}else if(ch.equals(Channel.hasMoney.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnHasMoney, sender, String.valueOf(data.getMoney(name) >= Utility.getLong(input))));
		}else if(ch.equals(Channel.setMoney.toString())){
			data.setMoney(name, Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.addMoney.toString())){
			data.addMoney(name, Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.substractMoney.toString())){
			data.removeMoney(name, Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.sendMoney.toString())){
			data.sendMoney(name, UUID.fromString(Utility.getString(input)), Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.getMoneyRanking.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnMoneyRanking, sender, getMoneyRanking(name)));
		}else if(ch.equals(Channel.getNumberOfTickets.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnNumberOfTickets, sender, String.valueOf(data.getTickets())));
		}else if(ch.equals(Channel.addTicket1.toString())){
			data.addTickets(Long.valueOf(Utility.getString(input)).longValue(), name);
		}else if(ch.equals(Channel.addTicket2.toString())){
			data.addTickets(Long.valueOf(Utility.getString(input)).longValue(), Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.substractTicket.toString())){
			data.removeTicket(Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.buyTicket.toString())){
			data.buyTicket(name, Long.valueOf(Utility.getString(input)).longValue(), getTicketPrice(name));
		}else if(ch.equals(Channel.sellTicket.toString())){
			data.sellTicket(name, Long.valueOf(Utility.getString(input)).longValue());
		}else if(ch.equals(Channel.getMedian.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnMedian, sender, String.valueOf(getMedian(name))));
		}else if(ch.equals(Channel.getTicketPrice.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnTicketPrice, sender, String.valueOf(getTicketPrice(name))));
		}else if(ch.equals(Channel.getNumberOfPlayerDataLoaded.toString())){
			server.sendData("BungeeCord", Utility.toByteArray(Channel.returnTicketPrice, sender, String.valueOf(getNumberOfPlayerDataLoaded())));
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
		getPlayerData(uuid).setMoney(name, money);
	}

	@Override
	public void addMoney(ServerName name, UUID uuid, long money){
		getPlayerData(uuid).addMoney(name, money);
	}

	@Override
	public void substractMoney(ServerName name, UUID uuid, long money){
		getPlayerData(uuid).removeMoney(name, money);
	}

	@Override
	public void sendMoney(ServerName name, UUID sender, UUID receiver, long money){
		getPlayerData(sender).sendMoney(name, receiver, money);
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
		getPlayerData(uuid).addTickets(numberOfTickets, pricePerTicketFromServer);
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket){
		getPlayerData(uuid).addTickets(numberOfTickets, pricePerTicket);
	}

	@Override
	public void substractTicket(UUID uuid, long numberOfTickets){
		getPlayerData(uuid).removeTicket(numberOfTickets);
	}

	@Override
	public void buyTicket(UUID uuid, ServerName where, long numberOfTickets){
		getPlayerData(uuid).buyTicket(where, numberOfTickets, getMedian(where));
	}

	@Override
	public void sellTicket(UUID uuid, ServerName where, long numberOfTickets){
		getPlayerData(uuid).sellTicket(where, numberOfTickets);
	}

}