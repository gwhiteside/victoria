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

import net.georgewhiteside.utility.FileUtil;

import com.google.common.collect.ImmutableList;

public class VideoGameDatabase {
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;
	
	private List<VideoGame> videoGameList;
	private int initialCapacity = 4000; // very minor optimization; set higher than number of products in database

	public VideoGameDatabase(String url, String user, String pass) {
		dbUrl	= url;
		dbUser	= user;
		dbPass	= pass;
		
		videoGameList = ImmutableList.copyOf(getAllVideoGamesFromDb());
	}
	
	private List<VideoGame> getAllVideoGamesFromDb() {
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
	
	/*
	public List<VideoGameSale> getPriceHistory(int videoGameId) {
		String query = FileUtil.loadTextResource("/res/get_prices.sql");
		List<VideoGameSale> list = new ArrayList<VideoGameSale>();
		
		try (
		Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(query);
		){
			while(resultSet.next()) {
				long id			= resultSet.getLong("");
				int productId	= resultSet.getInt("");
				long timestamp	= resultSet.getLong("timestamp");
				int price		= resultSet.getInt("price");
				list.add(new VideoGameSale(videoGameId, videoGameId, videoGameId, videoGameId));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public void getPriceHistory(VideoGame vg) {
		getPriceHistory(vg.getId());
	}
	
	*/
	
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
	
	public String getSearchQuery(int id) {
		String query = FileUtil.loadTextResource("/res/get_search_query.sql");
		String queryString = "";
		
		try {
			Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			PreparedStatement statement = connection.prepareStatement(query);
			
			statement.setInt(1, id);
			
			ResultSet resultSet = statement.executeQuery();
			
			// TODO better to check for size 0, size 1, and size > 1; result should never be > 1
			if(resultSet.first()) {
				queryString = resultSet.getString("query");
			}
			
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		};
		
		return queryString;
	}
	
	public String getSearchQuery(VideoGame vg) {
		return getSearchQuery(vg.getId());
	}
}
