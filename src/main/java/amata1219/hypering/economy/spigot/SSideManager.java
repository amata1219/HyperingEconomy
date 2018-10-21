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
import amata1219.hypering.economy.Utility;
import amata1219.hypering.economy.callback.Callback;
import amata1219.hypering.economy.callback.CallbackManager;

public class SSideManager implements Listener, PluginMessageListener, SSideHyperingEconomyAPI{

	private SSideHyperingEconomy plugin;

	private CallbackManager callbackManager;

	public SSideManager(SSideHyperingEconomy plugin){
		this.plugin = plugin;
		callbackManager = new CallbackManager();
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] data){
		if(!channel.equals("BungeeCord"))return;
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
		String ch = Utility.getString(input);
		UUID sender = UUID.fromString(Utility.getString(input));
		Object object = null;
		if(ch.equals(Channel.returnMoney.toString())){
			object = Utility.getLong(input);
		}else if(ch.equals(Channel.returnMoneyRanking.toString())){
			object  = Utility.getString(input);
		}else if(ch.equals(Channel.returnNumberOfTickets.toString())){
			object = Utility.getLong(input);
		}else if(ch.equals(Channel.returnMedian.toString())){
			object = Utility.getLong(input);
		}else if(ch.equals(Channel.returnTicketPrice.toString())){
			object = Utility.getLong(input);
		}else if(ch.equals(Channel.returNumberOfPlayerDataLoaded.toString())){
			object = Utility.getInt(input);
		}else{
			return;
		}
		callbackManager.getObjectsMap().put(sender, object);
		plugin.getManager().getCallbackManager().send(sender, callbackManager.getCallbacksMap().get(sender));
	}

	public CallbackManager getCallbackManager(){
		return callbackManager;
	}

	private List<Will> will = new ArrayList<Will>();

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		will.forEach(w -> w.done(e.getPlayer()));

		if(!e.getPlayer().hasPlayedBefore()){
			new BukkitRunnable(){
				public void run(){
					e.getPlayer().saveData();
				}
			}.runTaskLater(plugin, 50);
		}
	}

	public SSideHyperingEconomy getPlugin(){
		return plugin;
	}

	public boolean isEnableEconomy(){
		return plugin.getConfig().getBoolean("EnableEconomy");
	}

	private void send(Player player, byte[] bytes){
		player.sendPluginMessage(plugin, "BungeeCord", bytes);
	}

	@Override
	public void getMoney(Player sender, UUID uuid, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.getMoney, sender.getUniqueId(), uuid, ""));
	}

	@Override
	public void hasMoney(Player sender, UUID uuid, long threshold, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.hasMoney, sender.getUniqueId(), uuid, String.valueOf(threshold)));
	}

	@Override
	public void setMoney(Player sender, UUID uuid, long money){
		send(sender, Utility.toByteArray(Channel.setMoney, sender.getUniqueId(), uuid, String.valueOf(money)));
	}

	@Override
	public void setMoney(UUID uuid, long money){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				setMoney(player, uuid, money);
			}

		});
	}

	@Override
	public void addMoney(Player sender, UUID uuid, long money){
		send(sender, Utility.toByteArray(Channel.addMoney, sender.getUniqueId(), uuid, String.valueOf(money)));
	}

	@Override
	public void addMoney(UUID uuid, long money){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				addMoney(player, uuid, money);
			}

		});
	}

	@Override
	public void substractMoney(Player sender, UUID uuid, long money){
		send(sender, Utility.toByteArray(Channel.substractMoney, sender.getUniqueId(), uuid, String.valueOf(money)));
	}

	@Override
	public void substractMoney(UUID uuid, long money){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				substractMoney(player, uuid, money);
			}

		});
	}

	@Override
	public void sendMoney(Player player, UUID sender, UUID receiver, long money){
		send(player, Utility.toByteArray(Channel.sendMoney, sender, sender, receiver.toString(), String.valueOf(money)));
	}

	@Override
	public void sendMoney(UUID sender, UUID receiver, long money){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				sendMoney(player, sender, receiver, money);
			}

		});
	}

	@Override
	public void getMoneyRanking(Player sender, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.getMoneyRanking, sender.getUniqueId(), sender.getUniqueId(), String.valueOf("")));
	}

	@Override
	public void getNumberOfTickets(Player sender, UUID uuid, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.getNumberOfTickets, sender.getUniqueId(), uuid, ""));
	}

	@Override
	public void addTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Utility.toByteArray(Channel.addTicket1, sender.getUniqueId(), uuid, String.valueOf(numberOfTickets)));
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				addTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void addTicket(Player sender, UUID uuid, long numberOfTickets, long pricePerTicket){
		send(sender, Utility.toByteArray(Channel.addTicket2, sender.getUniqueId(), uuid, String.valueOf(numberOfTickets), String.valueOf(pricePerTicket)));
	}

	@Override
	public void addTicket(UUID uuid, long numberOfTickets, long pricePerTicket){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				addTicket(player, uuid, numberOfTickets, pricePerTicket);
			}

		});
	}

	@Override
	public void substractTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Utility.toByteArray(Channel.substractTicket, sender.getUniqueId(), uuid, String.valueOf(numberOfTickets)));
	}

	@Override
	public void substractTicket(UUID uuid, long numberOfTickets){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				substractTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void buyTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Utility.toByteArray(Channel.buyTicket, sender.getUniqueId(), uuid, String.valueOf(numberOfTickets)));
	}

	@Override
	public void buyTicket(UUID uuid, long numberOfTickets){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				buyTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void sellTicket(Player sender, UUID uuid, long numberOfTickets){
		send(sender, Utility.toByteArray(Channel.sellTicket, sender.getUniqueId(), uuid, String.valueOf(numberOfTickets)));
	}

	@Override
	public void sellTicket(UUID uuid, long numberOfTickets){
		will.add(new Will(){

			@Override
			public void done(Player player) {
				sellTicket(player, uuid, numberOfTickets);
			}

		});
	}

	@Override
	public void getMedian(Player sender, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.getMedian, sender.getUniqueId(), sender.getUniqueId(), ""));
	}

	@Override
	public void getTicketPrice(Player sender, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.getTicketPrice, sender.getUniqueId(), sender.getUniqueId(), ""));
	}

	@Override
	public void getNumberOfPlayerDataLoaded(Player sender, Callback callback){
		callbackManager.getCallbacksMap().put(sender.getUniqueId(), callback);
		send(sender, Utility.toByteArray(Channel.getNumberOfPlayerDataLoaded, sender.getUniqueId(), sender.getUniqueId(), ""));
	}

}
