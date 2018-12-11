package amata1219.hypering.economy;

import java.util.UUID;

public class Getter<T> {

	@SuppressWarnings("unchecked")
	public T get(String command, String columnIndex){
		return (T) Database.getResult(command, columnIndex);
	}

	@SuppressWarnings("unchecked")
	public T get(UUID uuid, String columnIndex){
		return (T) Database.getResult("SELECT " + columnIndex + " FROM " + Database.getDatabaseName() + "." + Database.getTableName() + " WHERE uuid='" + uuid.toString() + "'", columnIndex);
	}

}
