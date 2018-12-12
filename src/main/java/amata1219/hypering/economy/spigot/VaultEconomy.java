package amata1219.hypering.economy.spigot;

import java.util.List;

import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultEconomy implements Economy {

	@Override
	public EconomyResponse bankBalance(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String arg0) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(String arg0, String arg1) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
		return false;
	}

	@Override
	public String currencyNamePlural() {
		return null;
	}

	@Override
	public String currencyNameSingular() {
		return null;
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		return null;
	}

	@Override
	public String format(double arg0) {
		return null;
	}

	@Override
	public int fractionalDigits() {
		return 0;
	}

	@Override
	public double getBalance(String arg0) {
		return 0;
	}

	@Override
	public double getBalance(OfflinePlayer arg0) {
		return 0;
	}

	@Override
	public double getBalance(String arg0, String arg1) {
		return 0;
	}

	@Override
	public double getBalance(OfflinePlayer arg0, String arg1) {
		return 0;
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean has(String arg0, double arg1) {
		return false;
	}

	@Override
	public boolean has(OfflinePlayer arg0, double arg1) {
		return false;
	}

	@Override
	public boolean has(String arg0, String arg1, double arg2) {
		return false;
	}

	@Override
	public boolean has(OfflinePlayer arg0, String arg1, double arg2) {
		return false;
	}

	@Override
	public boolean hasAccount(String arg0) {
		return false;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0) {
		return false;
	}

	@Override
	public boolean hasAccount(String arg0, String arg1) {
		return false;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0, String arg1) {
		return false;
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
		return null;
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		return null;
	}

	private EconomyResponse notSupportBank(){
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "HyperingEconomy does not support bank.");
	}

}
