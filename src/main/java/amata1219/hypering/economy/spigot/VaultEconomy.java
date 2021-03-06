package amata1219.hypering.economy.spigot;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import amata1219.hypering.economy.HyperingEconomyAPI;
import amata1219.hypering.economy.SQL;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultEconomy implements Economy {

	private static VaultEconomy instance;
	private HyperingEconomyAPI api;

	private VaultEconomy(){

	}

	public static void load(){
		instance = new VaultEconomy();
		instance.api = SQL.getSQL().getHyperingEconomyAPI();
	}

	public static void unload(){
	}

	public static VaultEconomy getInstance(){
		return instance;
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

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		api.addMoney(uuid, Double.valueOf(arg1).longValue());

		return new EconomyResponse(arg1, api.getMoney(uuid), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double arg1) {
		if(arg1 < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can not deposit negative amount");

		if(player.getName() == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		api.addMoney(uuid, Double.valueOf(arg1).longValue());

		return new EconomyResponse(arg1, api.getMoney(uuid), ResponseType.SUCCESS, "");
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

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return -1;

		return api.getMoney(uuid);
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		if(player.getName() == null)
			return -1;

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return -1;

		return api.getMoney(uuid);
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

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return false;

		return api.hasMoney(uuid, Double.valueOf(arg1).longValue());
	}

	@Override
	public boolean has(OfflinePlayer player, double arg1) {
		if(player.getName() == null)
			return false;

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return false;

		return api.hasMoney(uuid, Double.valueOf(arg1).longValue());
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

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		api.removeMoney(uuid, Double.valueOf(arg1).longValue());

		return new EconomyResponse(arg1, api.getMoney(uuid), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double arg1) {
		if(arg1 < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can not withdraw negative amount");

		if(player.getName() == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		UUID uuid = player.getUniqueId();
		if(!SQL.getSQL().playerdata.containsKey(uuid) && !player.hasPlayedBefore() && !api.exist(uuid))
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Player not exist");

		api.removeMoney(uuid, Double.valueOf(arg1).longValue());

		return new EconomyResponse(arg1, api.getMoney(uuid), ResponseType.SUCCESS, "");
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
