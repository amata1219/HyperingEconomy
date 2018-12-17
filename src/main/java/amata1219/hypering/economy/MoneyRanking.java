package amata1219.hypering.economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MoneyRanking {

	private BiMap<Integer, UUID> ranking = HashBiMap.create();
	private HashMap<UUID, Long> money = new HashMap<>();

	private MoneyRanking(){

	}

	public static MoneyRanking load(ServerName serverName){
		MoneyRanking ranking = new MoneyRanking();

		List<UUID> uuids = new ArrayList<>();
		try(Connection con = Database.getHikariDataSource().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT uuid FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName())){
			try(ResultSet result = statement.executeQuery()){
				while(result.next())
					uuids.add(UUID.fromString(result.getString("uuid")));

				result.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		String columnIndex = serverName.name().toLowerCase();
		List<Long> money = Getter.getList("SELECT " + columnIndex + " FROM " + Database.getDatabaseName() + "." + Database.getPlayerDataTableName(), columnIndex);

		ranking.quickSort(uuids, money, 0, uuids.size() - 1, true);

		for(int i = 0; i < uuids.size(); i++){
			ranking.ranking.put(i + 1, uuids.get(i));
			ranking.money.put(uuids.get(i), money.get(i));
		}

		return ranking;
	}

	public int size(){
		return ranking.size();
	}

	public UUID matchedUniqueId(int rank){
		return ranking.get(rank);
	}

	public int getRank(UUID uuid){
		return ranking.inverse().get(uuid);
	}

	public long getMoney(int rank){
		UUID uuid = matchedUniqueId(rank);
		if(uuid == null)
			return 0L;

		return getMoney(uuid);
	}

	public long getMoney(UUID uuid){
		return money.get(uuid);
	}

	public boolean isInvalid(){
		return ranking.size() != money.size();
	}

	private void quickSort(List<UUID> uuids, List<Long> money, int left, int right, boolean reverse){
		if(left <= right){
			long p = money.get((left + right) >>> 1);
			int l = left;
			int r = right;
			while(l <= r){
				if(reverse){
					while(money.get(l) > p)
						l++;

					while(money.get(r) < p)
						r--;
				}else{
					while(money.get(l) < p)
						l++;

					while(money.get(r) > p)
						r--;
				}

				if(l <= r){
					UUID tmp1 = uuids.get(l);
					uuids.set(l, uuids.get(r));
					uuids.set(r, tmp1);

					Long tmp2 = money.get(l);
					money.set(l, money.get(r));
					money.set(r, tmp2);

					l++;
					r--;
				}
			}
			quickSort(uuids, money, left, r, reverse);
			quickSort(uuids, money, l, right, reverse);
		}
	}

}
