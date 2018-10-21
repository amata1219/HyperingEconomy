package amata1219.hypering.economy.callback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CallbackHelper<T> {

	private final Map<UUID, HashMap<Integer, T>> callbacks = new WeakHashMap<>();

	private final AtomicInteger seqId = new AtomicInteger(1);

	public int getAndIncrementSeqId(){
		return seqId.getAndIncrement();
	}

	public synchronized T getAndRemove(UUID uuid, int seq){
		if(uuid == null)
			return null;

		HashMap<Integer, T> map = callbacks.get(uuid);
		if(map == null)
			return null;

		T callback = map.get(seq);
		map.remove(seq);
		if(map.size() == 0)
			callbacks.remove(uuid);
		return callback;
	}

	public synchronized void put(UUID uuid, T callback, int seq){
		if(uuid == null || callback == null)
			return;

		HashMap<Integer, T> map = callbacks.get(uuid);
		if(map == null){
			map = new HashMap<Integer, T>();
			callbacks.put(uuid, map);
		}
		map.put(seq, callback);
	}

	public synchronized void remove(UUID uuid){
		if(uuid == null)
			return;
		callbacks.remove(uuid);
	}

}