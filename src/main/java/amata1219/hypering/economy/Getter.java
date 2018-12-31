package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Getter {

	public static int getInt(String command, String columnIndex){
		int i = 0;

		try(Connection con = SQL.getSQL().getSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					i = result.getInt(columnIndex);
					break;
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
			SQL.getSQL().wish(new Future(){

				@Override
				public void done() {
					getInt(command, columnIndex);
				}

			});
		}

		return i;
	}

	public static long getLong(String command, String columnIndex){
		long l = 0;

		try(Connection con = SQL.getSQL().getSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					l = result.getLong(columnIndex);
					break;
				}
				result.close();
			}
			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
			SQL.getSQL().wish(new Future(){

				@Override
				public void done() {
					getInt(command, columnIndex);
				}

			});
		}

		return l;
	}

	public static int getInt(UUID uuid, String columnIndex){
		return getInt("SELECT " + columnIndex + " FROM HyperingEconomyDatabase.playerdata WHERE uuid='" + uuid.toString() + "'", columnIndex);
	}

	public static long getLong(UUID uuid, String columnIndex){
		return getLong("SELECT " + columnIndex + " FROM HyperingEconomyDatabase.playerdata WHERE uuid='" + uuid.toString() + "'", columnIndex);
	}

	/*public static List<Long> getList(String command, String columnIndex){
		List<Long> list = new ArrayList<>();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					list.add(result.getLong(columnIndex));

				result.close();
				statement.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return list;
	}*/

}