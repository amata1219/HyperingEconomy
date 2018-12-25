package amata1219.hypering.economy.spigot;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CollectedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private HashMap<UUID, Long> map;

	public CollectedEvent(HashMap<UUID, Long> map){
		this.map = map;
	}

	public HashMap<UUID, Long> getMap(){
		return map;
	}

	@Override
	public HandlerList getHandlers() {
		return null;
	}

	public static HandlerList getHandlerList(){
		return handlers;
	}

}
