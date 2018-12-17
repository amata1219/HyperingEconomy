package amata1219.hypering.economy;

import java.util.UUID;

public class Saver {

	public static void save(UUID uuid, String columnIndex, Object value){
		Database.putCommand("UPDATE " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " SET " + columnIndex + "=" + value + " WHERE uuid='" + uuid.toString() + "'");
	}

}
