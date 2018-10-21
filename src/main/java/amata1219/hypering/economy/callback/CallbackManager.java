package amata1219.hypering.economy.callback;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class CallbackManager {

	private final CallbackHelper<Callback> callbackHelper = new CallbackHelper<Callback>();

	private Map<UUID, Callback> callbacks = new WeakHashMap<>();
	private Map<UUID, Object> objects = new WeakHashMap<>();

	public CallbackManager(){

	}

	public Map<UUID, Callback> getCallbacksMap(){
		return callbacks;
	}

	public Map<UUID, Object> getObjectsMap(){
		return objects;
	}

	public int send(UUID uuid, Callback callback){
		final WeakReference<UUID> ref = new WeakReference<>(uuid);
		final int seq = callbackHelper.getAndIncrementSeqId();
		callbackHelper.put(uuid, callback, seq);
		try{
			Object result = objects.get(uuid);
			done(ref, seq, result);
		}catch(Exception e){
			e.printStackTrace();
		}
		return seq;
	}

	private void done(final WeakReference<UUID> ref, final int seq, final Object result){
		if(ref.get() == null || result == null)
			return;
		Callback callback = callbackHelper.getAndRemove(ref.get(), seq);
		if(callback != null)
			callback.done(result);
	}

	public void removeCallbackFor(UUID uuid){
		callbackHelper.remove(uuid);
	}

}
