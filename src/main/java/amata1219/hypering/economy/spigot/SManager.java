package amata1219.hypering.economy.spigot;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import amata1219.hypering.economy.Channel;
import amata1219.hypering.economy.HyperingEconomyChannel;
import amata1219.hypering.economy.Util;
import amata1219.hypering.economy.callback.Callback;
import amata1219.hypering.economy.callback.CallbackManager;
import amata1219.hypering.economy.callback.Result;

public class SManager implements Listener, PluginMessageListener, SHyperingEconomyAPI{

	private static SManager manager;

	private CallbackManager callbackManager = new CallbackManager();

	private List<Will> wills = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public SManager(){
		manager = this;

		SHyperingEconomy plugin = SHyperingEconomy.getPlugin();

		String str = plugin.getConfig().getString("Wills");
		if(str != null){
			Object obj = Util.fromBase64(str);
			if(obj != null)
				wills = (ArrayList<Will>) obj;
		}
		plugin.getConfig().set("Wills", null);
		plugin.saveConfig();
		plugin.reloadConfig();
	}

	public static SManager getManager(){
		return manager;
	}

	public void unload(){
		SHyperingEconomy plugin = SHyperingEconomy.getPlugin();
		plugin.getConfig().set("Wills", Util.toBase64(wills));
		plugin.saveConfig();
		plugin.reloadConfig();
	}

	@Override
	public void onPluginMessageReceived(String ch, Player player, byte[] data){
		if(!ch.equals("BungeeCord"))
			return;

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
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

		int seqId = Integer.valueOf(channel.getMessage()).intValue();

		switch(sub){
		case Channel.RETURN_GET_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			long money1 = Long.valueOf(channel.getMessage()).longValue();

			callbackManager.done(seqId, new Result(money1));
			break;
		case Channel.RETURN_HAS_MONEY:
			channel.read(stream);
			if(channel.isNull())
				return;

			boolean has2 = Boolean.valueOf(channel.getMessage()).booleanValue();

			callbackManager.done(seqId, new Result(has2));
			break;
		case Channel.RETURN_GET_TICKETS:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number3 = Long.valueOf(channel.getMessage()).longValue();

			callbackManager.done(seqId, new Result(number3));
			break;
		case Channel.RETURN_GET_MEDIAN:
			channel.read(stream);
			if(channel.isNull())
				return;

			long median4 = Long.valueOf(channel.getMessage()).longValue();

			callbackManager.done(seqId, new Result(median4));
			break;
		case Channel.RETURN_GET_TICKET_PRICE:
			channel.read(stream);
			if(channel.isNull())
				return;

			long price5 = Long.valueOf(channel.getMessage()).longValue();

			callbackManager.done(seqId, new Result(price5));
			break;
		case Channel.RETURN_GET_NUMBER_OF_PLAYER_DATA_LOADED:
			channel.read(stream);
			if(channel.isNull())
				return;

			long number6 = Long.valueOf(channel.getMessage()).longValue();

			callbackManager.done(seqId, new Result(number6));
			break;
		default:
			break;
		}
	}

	public CallbackManager getCallbackManager(){
		return callbackManager;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		wills.forEach(w -> w.done(e.getPlayer()));

		if(!e.getPlayer().hasPlayedBefore()){
			new BukkitRunnable(){
				public void run(){
					e.getPlayer().saveData();
				}
			}.runTaskLater(SHyperingEconomy.getPlugin(), 50);
		}
	}

	public boolean isEnableEconomy(){
		return SHyperingEconomy.getPlugin().getConfig().getBoolean("EnableEconomy");
	}

	private void send(Player player, byte[] bytes){
		player.sendPluginMessage(SHyperingEconomy.getPlugin(), "BungeeCord", bytes);
	}

	@Override
	public void getMoney(Player sender, UUID uuid, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.GET_MONEY, uuid.toString(), String.valueOf(seqId)));
	}

	@Override
	public void hasMoney(Player sender, UUID uuid, long threshold, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.HAS_MONEY, uuid.toString(), String.valueOf(threshold), String.valueOf(seqId)));
	}

	@Override
	public void setMoney(Player sender, UUID uuid, long money){
		send(sender, Util.toByteArray(Channel.SET_MONEY, uuid.toString(), String.valueOf(money)));
	}

	@Override
	public void setMoney(UUID uuid, long money){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				setMoney(player, uuid, money);
			}

		});
	}

	@Override
	public void addMoney(Player sender, UUID uuid, long money){
		send(sender, Util.toByteArray(Channel.ADD_MONEY, uuid.toString(), String.valueOf(money)));
	}

	@Override
	public void addMoney(UUID uuid, long money){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				addMoney(player, uuid, money);
			}

		});
	}

	@Override
	public void removeMoney(Player sender, UUID uuid, long money){
		send(sender, Util.toByteArray(Channel.REMOVE_MONEY, uuid.toString(), String.valueOf(money)));
	}

	@Override
	public void removeMoney(UUID uuid, long money){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				removeMoney(player, uuid, money);
			}

		});
	}

	@Override
	public void sendMoney(Player player, UUID sender, UUID receiver, long money){
		send(player, Util.toByteArray(Channel.SEND_MONEY, sender.toString(), receiver.toString(), String.valueOf(money)));
	}

	@Override
	public void sendMoney(UUID sender, UUID receiver, long money){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				sendMoney(player, sender, receiver, money);
			}

		});
	}

	@Override
	public void getMoneyRanking(Player sender, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.GET_MONEY_RANKING, sender.getUniqueId().toString(), String.valueOf(seqId)));
	}

	@Override
	public void getNumberOfTickets(Player sender, UUID uuid, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.GET_TICKETS, uuid.toString(), String.valueOf(seqId)));
	}

	@Override
	public void addTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Util.toByteArray(Channel.ADD_TICKET1, uuid.toString(), String.valueOf(numberOfTickets)));
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				addTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void addTicket(Player sender, UUID uuid, long numberOfTickets, long pricePerTicket){
		send(sender, Util.toByteArray(Channel.ADD_TICKET2, uuid.toString(), String.valueOf(numberOfTickets), String.valueOf(pricePerTicket)));
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				addTicket(player, uuid, numberOfTickets, pricePerTicket);
			}

		});
	}

	@Override
	public void removeTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Util.toByteArray(Channel.REMOVE_TICKET, uuid.toString(), String.valueOf(numberOfTickets)));
	}

	@Override
	public void removeTicket(UUID uuid, long numberOfTickets){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				removeTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void buyTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Util.toByteArray(Channel.BUY_TICKET, uuid.toString(), String.valueOf(numberOfTickets)));
	}

	@Override
	public void buyTicket(UUID uuid, long numberOfTickets){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				buyTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void sellTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Util.toByteArray(Channel.SELL_TICKET, uuid.toString(), String.valueOf(numberOfTickets)));
	}

	@Override
	public void sellTicket(UUID uuid, long numberOfTickets){
		wills.add(new Will(){

			@Override
			public void done(Player player) {
				sellTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void getMedian(Player sender, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.GET_MEDIAN, sender.getUniqueId().toString(), String.valueOf(seqId)));
	}

	@Override
	public void getTicketPrice(Player sender, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.GET_TICKET_PRICE, sender.getUniqueId().toString(), String.valueOf(seqId)));
	}

	@Override
	public void getNumberOfPlayerDataLoaded(Player sender, Callback<Result> callback){
		int seqId = callbackManager.send(callback);
		send(sender, Util.toByteArray(Channel.GET_NUMBER_OF_PLAYER_DATA_LOADED, sender.getUniqueId().toString(), String.valueOf(seqId)));
	}

}
