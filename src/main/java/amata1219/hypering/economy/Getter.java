package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Getter<T> {

	@SuppressWarnings("unchecked")
	public T get(String command, String columnIndex){
		return (T) Database.getResult(command, columnIndex);
	}

	@SuppressWarnings("unchecked")
	public T get(UUID uuid, String columnIndex){
		return (T) Database.getResult("SELECT " + columnIndex + " FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " WHERE uuid='" + uuid.toString() + "'", columnIndex);
	}

	@SuppressWarnings("unchecked")
	public List<T> getList(String command, String columnIndex){
		List<T> list = new ArrayList<>();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					list.add((T) result.getObject(columnIndex));

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return list;
	}

	public List<Long> getList(String command, String... columnIndexes){
		List<Long> list = new ArrayList<>();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					long l = 0L;

					for(String columnIndex : columnIndexes)
						l += result.getLong(columnIndex);

					list.add(l);
				}

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return list;
	}

}