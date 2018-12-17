package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Getter {

	public static int getInt(String command, String columnIndex){
		int i = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					i = result.getInt(columnIndex);
					break;
				}

				result.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return i;
	}

	public static long getLong(String command, String columnIndex){
		long l = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					l = result.getLong(columnIndex);
					break;
				}

				result.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return l;
	}

	public static int getInt(UUID uuid, String columnIndex){
		int i = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT " + columnIndex + " FROM HyperingEconomyDatabase.playerdata WHERE uuid='" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					i = result.getInt(columnIndex);
					break;
				}

				result.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return i;
	}

	public static long getLong(UUID uuid, String columnIndex){
		long l = 0;

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT " + columnIndex + " FROM HyperingEconomyDatabase.playerdata WHERE uuid='" + uuid.toString() + "'")){
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					l = result.getLong(columnIndex);
					break;
				}

				result.close();
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return l;
	}

	public static List<Long> getList(String command, String columnIndex){
		List<Long> list = new ArrayList<>();

		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement(command)){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					list.add(result.getLong(columnIndex));

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return list;
	}

}