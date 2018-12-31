package amata1219.hypering.economy;

import java.util.UUID;

import amata1219.hypering.economy.spigot.HyperingEconomy;

public class Player {

	private long money = 0L;

	private Player(){

	}

	public Player(long money){
		this.money = money;
	}

	public static Player load(UUID uuid){
		Player player = new Player();

		player.money = Database.getHyperingEconomyAPI().getMoney(HyperingEconomy.getServerName(), uuid);

		return player;
	}

	public void add(long increase){
		money += increase;
	}

	public void remove(long decrease){
		money -= money < decrease ? money : decrease;
	}

}
