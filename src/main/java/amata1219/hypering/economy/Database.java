package amata1219.hypering.economy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {

	private static Database database;

	private HikariDataSource source;

	private String databaseName;
	private String tableName;

	private Database(){

	}

	public static void load(String host, int port, String databaseName, String userName, String password, String tableName){
		Database database = new Database();

		database.databaseName = databaseName;
		database.tableName = tableName;

		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName);

		config.addDataSourceProperty("user", userName);
		config.addDataSourceProperty("password", password);

		config.setInitializationFailFast(true);
		config.setConnectionInitSql("SELECT 1");

		database.source = new HikariDataSource(config);

		Database.database = database;
	}

	public static Database getDatabase(){
		return database;
	}

	public static String getDatabaseName(){
		return Database.database.databaseName;
	}

	public static String getTableName(){
		return Database.database.tableName;
	}

	public static void close(){
		Database database = Database.database;

		if(database.source != null)
			database.source.close();
	}

}
