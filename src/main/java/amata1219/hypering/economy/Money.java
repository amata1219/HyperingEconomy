package amata1219.hypering.economy;

import java.util.UUID;

public class Money {

	private long money = 0L;

	private boolean flag;

	private Money(){

	}

	public Money(long money){
		this.money = money;
	}

	public static Money load(UUID uuid){
		Money player = new Money();

		player.money = SQL.getSQL().getHyperingEconomyAPI().getMoney(uuid);

		return player;
	}

	public boolean isChanged(){
		return flag;
	}

	public void flag(){
		flag = true;
	}

	public void clear(){
		flag = false;
	}

	public long get(){
		return money;
	}

	public boolean has(long threshold){
		return money >= threshold;
	}

	public void set(long money){
		this.money = money;

		flag();
	}

	public void add(long increase){
		money += increase;

		flag();
	}

	public void remove(long decrease){
		money -= money < decrease ? money : decrease;

		flag();
	}

}
