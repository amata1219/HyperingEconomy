package amata1219.hypering.economy;

import java.util.UUID;

public class Saver {

	public static void saveLong(UUID uuid, String columnIndex, long value){
		SQL.getSQL().putCommand("UPDATE HyperingEconomyDatabase.playerdata SET " + columnIndex + " = " + value + " WHERE uuid = '" + uuid.toString() + "'");
	}

}
