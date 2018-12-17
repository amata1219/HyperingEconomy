package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Getter {

	public static Object get(String command, String columnIndex){
		return Database.getResult(command, columnIndex);
	}

	public static Object get(UUID uuid, String columnIndex){
		return Database.getResult("SELECT " + columnIndex + " FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName() + " WHERE uuid='" + uuid.toString() + "'", columnIndex);
	}

	public static List<Long> getList(String command, String columnIndex){
		List<Long> list = new ArrayList<>();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					list.add((Long) result.getObject(columnIndex));

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return list;
	}

}