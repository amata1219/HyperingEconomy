package amata1219.hypering.economy.callback;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CallbackHelper<T> {

	private final Map<Integer, T> callbacks = new WeakHashMap<>();

	private final AtomicInteger seqId = new AtomicInteger(1);

	public int getAndIncrementSeqId(){
		return seqId.getAndIncrement();
	}

	public synchronized T getAndRemove(int seqId){
		T callback = callbacks.get(seqId);
		callbacks.remove(seqId);
		return callback;
	}

	public synchronized void put(int seqId, T callback){
		if(callback == null)
			return;

		callbacks.put(seqId, callback);
	}

	public synchronized void remove(int seqId){
		callbacks.remove(seqId);
	}

}
