package amata1219.hypering.economy.bungeecord;

import amata1219.hypering.economy.MySQL;
import amata1219.hypering.economy.PlayerData;
import amata1219.hypering.economy.ServerName;

public class PlayerDataSaveTaskRunnable implements Runnable{

	private BCSideManager manager;
	private MySQL sql;

	public PlayerDataSaveTaskRunnable(BCSideManager manager){
		this.manager = manager;
		sql = manager.getMySQL();
		sort();
	}

	@Override
	public void run(){
		sql.checkConnection();
		manager.getPlayerDataMap().values().forEach(data -> data.save());
		manager.getWillSaveMap().forEach((k, v) -> {
			v.save();
			manager.getWillSaveMap().remove(k);
		});
		sort();
	}

	private void sort(){
		for(ServerName name : ServerName.values()){
			PlayerData[] data = (PlayerData[]) sql.getAllPlayerData().toArray(new PlayerData[]{});
			for(int i = 0; i < data.length - 1; i++){
				for(int j = 0; j < data.length - i - 1; j++){
					if(data[j].getMoney(name) < data[j + 1].getMoney(name)){
						PlayerData asc = data[j];
						data[j] = data[j + 1];
						data[j + 1] = asc;
					}
				}
			}
			manager.getMoneyRankingMap().put(name, normalize(name, data));
		}
	}

	private String normalize(ServerName name, PlayerData[] data){
		if(data.length == 0){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(PlayerData d : data){
			sb.append("," + d.getUniqueId().toString() + "-" + d.getMoney(name));
		}
		return sb.substring(1);
	}

}
