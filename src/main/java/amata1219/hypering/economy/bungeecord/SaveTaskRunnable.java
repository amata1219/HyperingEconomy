package amata1219.hypering.economy.bungeecord;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import amata1219.hypering.economy.MySQL;
import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;

public class SaveTaskRunnable implements Runnable{

	private HashMap<ServerName, Comparator<PlayerData>> comparators = new HashMap<>();

	public SaveTaskRunnable(){
		Arrays.asList(ServerName.values()).forEach(name -> {
			comparators.put(name, new Comparator<PlayerData>(){

				@Override
				public int compare(PlayerData o1, PlayerData o2) {
					return Long.compare(o1.getMoney(name), o2.getMoney(name));
				}

			}.reversed());
		});

		sort();
	}

	@Override
	public void run(){
		BCManager.getManager().getPlayerDataMap().values().forEach(data -> data.save());
		sort();
	}

	private void sort(){
		List<PlayerData> list = MySQL.getAllPlayerData();
		for(ServerName name : ServerName.values()){
			list.sort(comparators.get(name));

			BCManager.getManager().getMoneyRankingMap().put(name, normalize(name, list));
		}
	}

	private String normalize(ServerName name, List<PlayerData> list){
		StringBuilder sb = new StringBuilder("");
		if(list.isEmpty())
			return "";

		list.forEach(data -> sb.append("," + data.getUniqueId().toString() + "#" + data.getMoney(name)));

		return sb.toString().substring(1);
	}

}
