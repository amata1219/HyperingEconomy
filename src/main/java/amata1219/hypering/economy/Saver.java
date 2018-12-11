package amata1219.hypering.economy;

import java.util.UUID;

public class Saver<T> {

	public void save(UUID uuid, String columnIndex, T value){
		Database.putCommand("UPDATE " + Database.getDatabaseName() + "." + Database.getTableName() + " SET " + columnIndex + "=" + value + " WHERE uuid='" + uuid.toString() + "'");
	}

}
