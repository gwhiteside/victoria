package net.georgewhiteside.victoria;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.georgewhiteside.utility.FileUtil;

import com.google.common.collect.ImmutableList;

public class Database {
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;
	
	private List<VideoGame> videoGameList;
	private int initialCapacity = 4000; // very minor optimization; set higher than number of products in database
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	public Database(String url, String user, String pass) {
		log.info("Initializing database access object...");
		
		dbUrl	= url;
		dbUser	= user;
		dbPass	= pass;
		
		videoGameList = ImmutableList.copyOf(getAllVideoGamesFromDb());
	}
	
	private List<VideoGame> getAllVideoGamesFromDb() {
		// TODO try with resources
		List<VideoGame> videoGames = new ArrayList<VideoGame>(initialCapacity);
		String query = FileUtil.loadTextResource("/res/get_video_games.sql");
		
		try {
			Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			while(resultSet.next()) {
				int productId		= resultSet.getInt("product_id");
				String title		= resultSet.getString("title");
				int systemId		= resultSet.getInt("system_id");
				String systemName	= resultSet.getString("name");
				int year			= resultSet.getInt("year");
				String region		= resultSet.getString("region");
				videoGames.add(new VideoGame(productId, title, systemId, systemName.intern(), year, region));
			}
			
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		};
		
		return videoGames;
	}
	
	public List<VideoGame> getAllVideoGames() {
		return videoGameList;
	}
	
	
	private List<VideoGameSale> getSales(int videoGameId) {
		String query = FileUtil.loadTextResource("/res/get_prices.sql");
		List<VideoGameSale> list = new ArrayList<VideoGameSale>();
		
		try(Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			PreparedStatement statement = connection.prepareStatement(query);) {
			
			statement.setInt(1, videoGameId);
			
			try(ResultSet resultSet = statement.executeQuery(query);) {
				while(resultSet.next()) {
					long saleId		= resultSet.getLong("sale_id");
					long timestamp	= resultSet.getLong("timestamp");
					int price		= resultSet.getInt("price");
					String title	= resultSet.getString("title");
					list.add(new VideoGameSale(saleId, videoGameId, title, price, timestamp));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public void getSales(VideoGame vg) {
		getSales(vg.getId());
	}
	
	public long getLastUpdateTimestamp(VideoGame videoGame) {
		String query = FileUtil.loadTextResource("/res/get_search_query.sql");
		long timestamp = 0;
		
		try(Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			PreparedStatement statement = connection.prepareStatement(query);) {
			
			statement.setInt(1, videoGame.getId());
			
			try(ResultSet resultSet = statement.executeQuery();) {
				if(resultSet.first()) {
					timestamp = resultSet.getLong("updated");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return timestamp;
	}
	
	public long getSecondsSinceUpdate(VideoGame videoGame) {
		// TimeUnit.SECONDS
		long updateUnixTime = getLastUpdateTimestamp(videoGame);
		long currentUnixTime = System.currentTimeMillis() / 1000;
		return currentUnixTime - updateUnixTime;
	}
	
	public List<VideoGameSale> getPriceHistoryRange(int videoGameId, long start, long end) {
		List<VideoGameSale> list = new ArrayList<VideoGameSale>();
		
		
		
		return list;
	}
	
	
	private String getSearchQuery(int id) {
		// TODO try with resources
		String query = FileUtil.loadTextResource("/res/get_search_query.sql");
		String queryString = null;
		
		try {
			Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			PreparedStatement statement = connection.prepareStatement(query);
			
			statement.setInt(1, id);
			
			ResultSet resultSet = statement.executeQuery();
			
			if(resultSet.first()) {
				queryString = resultSet.getString("query");
				if(resultSet.next()) {
					throwProductSearchIntegrityException(id);
				}
			}
			
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		};
		
		return queryString;
	}
	
	/**
	 * 
	 * @param id
	 * @return Search string for given VideoGame if it exists, or null if it doesn't.
	 */
	public String getSearchQuery(VideoGame vg) {
		return getSearchQuery(vg.getId());
	}
	
	private void setSearchQuery(int id, String queryString) {
		// does an insert/ignore followed by update in a single transaction...
		// not sure this is the best and simplest way of portably doing this without additional libraries
		
		String insertQuery = FileUtil.loadTextResource("/res/insert_search_string.sql");
		String updateQuery = FileUtil.loadTextResource("/res/update_search_string.sql");
		
		try(Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
			PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
			
			connection.setAutoCommit(false);
			
			insertStatement.setInt(1, id);
			insertStatement.setString(2, queryString);
			insertStatement.execute();
			//insertStatement.getUpdateCount();
			
			updateStatement.setString(1, queryString);
			updateStatement.setInt(2, id);
			updateStatement.execute();
			
			connection.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		log.info("Set search query for product_id={}: {}", id, queryString);
	}
	
	public void setSearchQuery(VideoGame vg, String queryString) {
		setSearchQuery(vg.getId(), queryString);
	}
	
	
	
	private void throwProductSearchIntegrityException(int id) {
		// Should never happen, but if it does, it would be a bad idea to continue normally.
		log.error("This never needs to be more than one result! Bailing. videogames.search id={}", id);
		throw new IllegalArgumentException("Should be a 1:0..1 relationship between videogames.product and videogames.search!");
	}
}
