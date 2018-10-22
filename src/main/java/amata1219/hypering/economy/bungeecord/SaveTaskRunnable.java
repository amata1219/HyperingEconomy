package amata1219.hypering.economy.bungeecord;

import amata1219.hypering.economy.MySQL;
import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;

public class SaveTaskRunnable implements Runnable{

	public SaveTaskRunnable(){
		sort();
	}

	@Override
	public void run(){
		BCManager.getManager().getPlayerDataMap().values().forEach(data -> data.save());
		sort();
	}

	private void sort(){
		for(ServerName name : ServerName.values()){
			PlayerData[] data = (PlayerData[]) MySQL.getAllPlayerData().toArray(new PlayerData[]{});
			for(int i = 0; i < data.length - 1; i++){
				for(int j = 0; j < data.length - i - 1; j++){
					if(data[j].getMoney(name) < data[j + 1].getMoney(name)){
						PlayerData asc = data[j];
						data[j] = data[j + 1];
						data[j + 1] = asc;
					}
				}
			}
			BCManager.getManager().getMoneyRankingMap().put(name, normalize(name, data));
		}
	}

	private String normalize(ServerName name, PlayerData[] data){
		if(data.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		for(PlayerData d : data)
			sb.append("," + d.getUniqueId().toString() + "-" + d.getMoney(name));


		return sb.substring(1);
	}

}
