package amata1219.hypering.economy.spigot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import amata1219.hypering.economy.Database;
import amata1219.hypering.economy.HyperingEconomyAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultEconomy implements Economy {

	private static VaultEconomy instance;

	private BukkitTask collector;

	public final static HashMap<UUID, Long> map = new HashMap<>();

	private VaultEconomy(){

	}

	public static void load(){
		instance = new VaultEconomy();

		instance.collector = new BukkitRunnable(){

			@Override
			public void run(){
				CollectedEvent event = new CollectedEvent(VaultEconomy.map);
				Bukkit.getPluginManager().callEvent(event);
				map.clear();
			}

		}.runTaskTimer(HyperingEconomy.getPlugin(), 36000, 36000L);
	}

	public static void unload(){
		instance.collector.cancel();
	}

	public static VaultEconomy getInstance(){
		return instance;
	}

	private void collect(UUID uuid, long increase){
		if(map.containsKey(uuid))
			map.put(uuid, map.get(uuid) + increase);
		else
			map.put(uuid, increase);
	}

	@Override
	public EconomyResponse bankBalance(String arg0) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
		return notSupportBank();
	}

	@Override
	public boolean createPlayerAccount(String arg0) {
		return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0) {
		return true;
	}

	@Override
	public boolean createPlayerAccount(String arg0, String arg1) {
		return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
		return true;
	}

	@Override
	public String currencyNamePlural() {
		return "¥";
	}

	@Override
	public String currencyNameSingular() {
		return "¥";
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return notSupportBank();
	}

	@SuppressWarnings("deprecation")
	@Override
	public EconomyResponse depositPlayer(String arg0, double arg1) {
		if(arg1 < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can not deposit negative amount");

		OfflinePlayer player = Bukkit.getOfflinePlayer(arg0);
		if(player.getName() == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		UUID uuid = player.getUniqueId();
		long money = Double.valueOf(arg1).longValue();

		api.getMoneyEditer(HyperingEconomy.getServerName(), uuid).add(money);

		collect(uuid, money);

		return new EconomyResponse(arg1, api.getMoney(HyperingEconomy.getServerName(), player.getUniqueId()), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double arg1) {
		if(arg1 < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can not deposit negative amount");

		if(player.getName() == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		UUID uuid = player.getUniqueId();
		long money = Double.valueOf(arg1).longValue();

		api.getMoneyEditer(HyperingEconomy.getServerName(), uuid).add(money);

		collect(uuid, money);

		return new EconomyResponse(arg1, api.getMoney(HyperingEconomy.getServerName(), player.getUniqueId()), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
		return depositPlayer(arg0, arg2);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		return depositPlayer(arg0, arg2);
	}

	@Override
	public String format(double arg0) {
		return "¥" + Double.valueOf(arg0).longValue();
	}

	@Override
	public int fractionalDigits() {
		return -1;
	}

	@SuppressWarnings("deprecation")
	@Override
	public double getBalance(String arg0) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(arg0);
		if(player.getName() == null)
			return -1;

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return -1;

		return api.getMoney(HyperingEconomy.getServerName(), player.getUniqueId());
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		if(player.getName() == null)
			return -1;

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return -1;

		return api.getMoney(HyperingEconomy.getServerName(), player.getUniqueId());
	}

	@Override
	public double getBalance(String arg0, String arg1) {
		return getBalance(arg0);
	}

	@Override
	public double getBalance(OfflinePlayer arg0, String arg1) {
		return getBalance(arg0);
	}

	@Override
	public List<String> getBanks() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "HyperingEconomy";
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean has(String arg0, double arg1) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(arg0);
		if(player.getName() == null)
			return false;

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return false;

		return api.hasMoney(HyperingEconomy.getServerName(), player.getUniqueId(), Double.valueOf(arg1).longValue());
	}

	@Override
	public boolean has(OfflinePlayer player, double arg1) {
		if(player.getName() == null)
			return false;

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return false;

		return api.hasMoney(HyperingEconomy.getServerName(), player.getUniqueId(), Double.valueOf(arg1).longValue());
	}

	@Override
	public boolean has(String arg0, String arg1, double arg2) {
		return has(arg0, arg2);
	}

	@Override
	public boolean has(OfflinePlayer arg0, String arg1, double arg2) {
		return has(arg0, arg2);
	}

	@Override
	public boolean hasAccount(String arg0) {
		return true;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0) {
		return true;
	}

	@Override
	public boolean hasAccount(String arg0, String arg1) {
		return true;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0, String arg1) {
		return true;
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return notSupportBank();
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
		return notSupportBank();
	}

	@Override
	public boolean isEnabled() {
		return HyperingEconomy.isEconomyEnable();
	}

	@SuppressWarnings("deprecation")
	@Override
	public EconomyResponse withdrawPlayer(String arg0, double arg1) {
		if(arg1 < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can not withdraw negative amount");

		OfflinePlayer player = Bukkit.getOfflinePlayer(arg0);
		if(player.getName() == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		api.getMoneyEditer(HyperingEconomy.getServerName(), player.getUniqueId()).remove(Double.valueOf(arg1).longValue());

		return new EconomyResponse(arg1, api.getMoney(HyperingEconomy.getServerName(), player.getUniqueId()), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double arg1) {
		if(arg1 < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can not withdraw negative amount");

		if(player.getName() == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		HyperingEconomyAPI api = Database.getHyperingEconomyAPI();
		if(!api.exist(player.getUniqueId()))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		api.getMoneyEditer(HyperingEconomy.getServerName(), player.getUniqueId()).remove(Double.valueOf(arg1).longValue());

		return new EconomyResponse(arg1, api.getMoney(HyperingEconomy.getServerName(), player.getUniqueId()), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
		return withdrawPlayer(arg0, arg2);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		return withdrawPlayer(arg0, arg2);
	}

	private EconomyResponse notSupportBank(){
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "HyperingEconomy does not support bank.");
	}

}
