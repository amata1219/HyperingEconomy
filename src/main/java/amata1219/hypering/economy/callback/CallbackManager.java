package amata1219.hypering.economy.callback;

import amata1219.hypering.economy.spigot.Result;

public class CallbackManager {

	private final CallbackHelper<Callback<Result>> helper = new CallbackHelper<>();

	public int send(Callback<Result> callback){
		final int seqId = helper.getAndIncrementSeqId();
		helper.put(seqId, callback);
		return seqId;
	}

	public void done(final int seqId, final Result result){
		Callback<Result> callback = helper.getAndRemove(seqId);
		if(callback != null)
			callback.done(result);
	}

}
