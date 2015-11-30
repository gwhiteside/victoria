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

public class VideoGameDatabase {
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;

	public VideoGameDatabase(String url, String user, String pass) {
		dbUrl = url;
		dbUser = user;
		dbPass = pass;
	}
	
	public List<VideoGame> getProducts() {
		
		List<VideoGame> videoGames = new ArrayList<VideoGame>();
		
		try {
			long startTime = System.nanoTime();
			
			Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(
				"SELECT product_id, title, product.year, product.region, system.system_id, system.name" + '\n' +
				"FROM product" + '\n' +
				"INNER JOIN system ON product.system_id = system.system_id;"
			);
			
			long endTime = System.nanoTime() - startTime;
			System.out.println("SQL query executed in: " + endTime / 1000000 + " ms");
			
			startTime = System.nanoTime();
			
			while(resultSet.next()) {
				int productId = resultSet.getInt("product_id");
				String title = resultSet.getString("title");
				int systemId = resultSet.getInt("system_id");
				String systemName = resultSet.getString("name");
				int year = resultSet.getInt("year");
				String region = resultSet.getString("region");
				videoGames.add(new VideoGame(productId, title, systemId, systemName.intern(), year, region));
			}
			
			endTime = System.nanoTime() - startTime;
			System.out.println("Result data read in: " + endTime / 1000000 + " ms");
			System.out.println("Result data array size: " + videoGames.size());
			
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		};
		
		return videoGames;
	}
	
	public String getSearchQuery(int id) {
		
		String queryString = "";
		
		try {
			Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			PreparedStatement statement = connection.prepareStatement(
				"SELECT product_id, query" + '\n' +
				"FROM search" + '\n' +
				"WHERE product_id = ?;"
			);
			
			statement.setInt(1, id);
			
			ResultSet resultSet = statement.executeQuery();
			
			if(resultSet.first()) {
				queryString = resultSet.getString("product_id");
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
