package net.georgewhiteside.victoria.datareaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Dataloader {
	
	static final String DB_URL = "jdbc:mysql://basementserv:3306/videogames";
	static final String DB_USER = "victoria";
	static final String DB_PASS = "victoria";
	
	static Connection connection;
	static PreparedStatement stmtInsertProduct;
	
	public static void main(String[] args) {
		
		try {
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			stmtInsertProduct = connection.prepareStatement(
				"INSERT INTO product (title, year, system_id, region)" + '\n' +
				"VALUES(?, ?, ?, ?);"
			);
			
			gameboy();
			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void gameboy() {
		String file = "gameboygames.csv";
		boolean doRollback = false;
		int failCount = 0;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String row;
			
			connection.setAutoCommit(false);
			
			while((row = reader.readLine()) != null) {
				String[] columns = row.split(";");
				
				String title = columns[0];
				String year = columns[2];
				String region = "usa";
				
				stmtInsertProduct.setString(1, title);
				stmtInsertProduct.setString(2, year);
				stmtInsertProduct.setInt(3, 76);
				stmtInsertProduct.setString(4, region);
				
				int rows = stmtInsertProduct.executeUpdate();
				
				if(rows < 1) {
					doRollback = true;
					failCount++;
					System.out.println("failed: " + title + " -- " + year);
				}
			}
			
			stmtInsertProduct.close();
			
			if(doRollback) {
				System.out.println("" + failCount + " failures");
				System.out.println("Rolling back database changes...");
				connection.rollback();
			} else {
				System.out.println("Committing database changes");
				connection.commit();
				connection.setAutoCommit(true);
			}
			
			connection.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void gameboycolor() {
		String file = "gameboycolorgames.csv";
		boolean doRollback = false;
		int failCount = 0;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String row;
			
			connection.setAutoCommit(false);
			
			while((row = reader.readLine()) != null) {
				String[] columns = row.split(";");
				
				String title = columns[0];
				String year = columns[1];
				String region = "usa";
				
				stmtInsertProduct.setString(1, title);
				stmtInsertProduct.setString(2, year);
				stmtInsertProduct.setInt(3, 77);
				stmtInsertProduct.setString(4, region);
				
				int rows = stmtInsertProduct.executeUpdate();
				
				if(rows < 1) {
					doRollback = true;
					failCount++;
					System.out.println("failed: " + title + " -- " + year);
				}
			}
			
			stmtInsertProduct.close();
			
			if(doRollback) {
				System.out.println("" + failCount + " failures");
				System.out.println("Rolling back database changes...");
				connection.rollback();
			} else {
				System.out.println("Committing database changes");
				connection.commit();
				connection.setAutoCommit(true);
			}
			
			connection.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void n64() {
		String file = "nintendo64games.csv";
		boolean doRollback = false;
		int failCount = 0;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String row;
			
			connection.setAutoCommit(false);
			
			while((row = reader.readLine()) != null) {
				String[] columns = row.split(";");
				
				String title = columns[0];
				String year = columns[1];
				String region = "usa";
				
				stmtInsertProduct.setString(1, title);
				stmtInsertProduct.setString(2, year);
				stmtInsertProduct.setInt(3, 47);
				stmtInsertProduct.setString(4, region);
				
				int rows = stmtInsertProduct.executeUpdate();
				
				if(rows < 1) {
					doRollback = true;
					failCount++;
					System.out.println("failed: " + title + " -- " + year);
				}
			}
			
			stmtInsertProduct.close();
			
			if(doRollback) {
				System.out.println("" + failCount + " failures");
				System.out.println("Rolling back database changes...");
				connection.rollback();
			} else {
				System.out.println("Committing database changes");
				connection.commit();
				connection.setAutoCommit(true);
			}
			
			connection.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public static void sfc() {
		//String url = "https://en.wikipedia.org/wiki/List_of_Super_Famicom_games_(I–R)";
		String url = "https://en.wikipedia.org/wiki/List_of_Super_Famicom_games_(S–Z)";
		boolean doRollback = false;
		int failCount = 0;
		
		try {
			Document document = Jsoup.connect(url).timeout(5000).get();
			
			Elements elements = document.select(".wikitable tr:not(:has(th))");
			//Elements elements = document.select(".wikitable td:nth-child(6), .wikitable td:nth-child(1)");
			
			connection.setAutoCommit(false);
			
			for(Element e : elements) {
				
				String title = e.child(0).text();
				
				String year = "";
				
				try {
					year = e.child(5).text();
				} catch(IndexOutOfBoundsException ioob) {
					// ha ha!
				}
				
				
				if(year.length() >= 4) {
					year = year.substring(year.length() - 4, year.length());
				} else {
					year = null;
				}
				String region = "jpn";
				
				//System.out.println(year + '\t' + title);
				
				
				stmtInsertProduct.setString(1, title);
				stmtInsertProduct.setString(2, year);
				stmtInsertProduct.setInt(3, 86);
				stmtInsertProduct.setString(4, region);
				
				
				int rows = stmtInsertProduct.executeUpdate();
				
				if(rows < 1) {
					doRollback = true;
					failCount++;
					System.out.println("failed: " + title + " -- " + year);
				}
				
			}
			
			
			stmtInsertProduct.close();
			
			if(doRollback) {
				System.out.println("" + failCount + " failures");
				System.out.println("Rolling back database changes...");
				connection.rollback();
			} else {
				System.out.println("Committing database changes");
				connection.commit();
				connection.setAutoCommit(true);
			}
			
			
			connection.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void snes() {
		File input = new File("snesclean.html");
		boolean doRollback = false;
		int failCount = 0;
		try {
			
			Document doc = Jsoup.parse(input, "UTF-8", "");
			Elements elements = doc.select("tbody tr");
			
			connection.setAutoCommit(false);
			
			for(Element e : elements) {
				String title = e.child(0).text();
				String year = e.child(1).text();
				year = year.substring(year.length() - 4, year.length());
				String developer = e.child(2).text();
				String publisher = e.child(3).text();
				String region = e.child(4).text();
				region = region.split(", ")[0];
				if(region.equals("NA")) region = "usa";
				if(region.equals("PAL")) region = "eur";
				
				stmtInsertProduct.setString(1, title);
				stmtInsertProduct.setString(2, year);
				stmtInsertProduct.setInt(3, 46);
				stmtInsertProduct.setString(4, region);
				
				int rows = stmtInsertProduct.executeUpdate();
				
				if(rows < 1) {
					doRollback = true;
					failCount++;
					System.out.println("failed: " + title + " -- " + year + " -- " + developer + " -- " + publisher + " -- " + region);
				}
			}
			
			stmtInsertProduct.close();
			
			
			
			if(doRollback) {
				System.out.println("" + failCount + " failures");
				System.out.println("Rolling back database changes...");
				connection.rollback();
			} else {
				System.out.println("Committing database changes");
				connection.commit();
				connection.setAutoCommit(true);
			}
			
			connection.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	public void scrape() {
		String url = "http://videogames.pricecharting.com/console/virtual-boy";
		
		try {
			Document document = Jsoup.connect(url).timeout(5000).get();
			
			Elements elements = document.select("table#games_table tbody tr td.title a");
			
			for(Element e : elements) {
				String title = e.text();
				String link = e.attr("abs:href");
				System.out.println(title);
			}
			
			Element elemNextPage = document.getElementById("next-page");
			if(elemNextPage != null) {
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void bootgodload() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try {
			
			Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			final PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO product (name, year, system_id, developer_id, publisher_id) " +
				"SELECT ?, ?, ?, dev.id, pub.id " +
				"FROM company AS dev " +
				"JOIN company AS pub " +
				"WHERE dev.name = ? && pub.name = ?;"
			);
			
			
			SAXParser saxParser = factory.newSAXParser();
			
			DefaultHandler handler = new DefaultHandler() {
				int count = 0;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					if(qName.equals("game")) {
						String region = attributes.getValue("region");
						if(region != null && region.equalsIgnoreCase("Japan")) {
							
							String publisher = attributes.getValue("publisher");
							String developer = attributes.getValue("developer");
							String name = attributes.getValue("name");
							String year = attributes.getValue("date").substring(0, 4);
							
							try {
								statement.setString(1, name);
								statement.setString(2, year);
								statement.setInt(3, 45);
								statement.setString(4, developer);
								statement.setString(5, publisher);
								
								statement.addBatch();
								
								/*
								statement.addBatch(String.format(
									"INSERT INTO product (name, year, system_id, developer_id, publisher_id) " +
									"SELECT \"%s\", %s, %d, dev.id, pub.id " +
									"FROM company AS dev " +
									"JOIN company AS pub " +
									"WHERE dev.name = \"%s\" && pub.name = \"%s\";",
									name, year, 44, developer, publisher
								));
								*/
								
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					
				}
				
				private String getValueIgnoreCase(Attributes attributes, String qName){
				    for(int i = 0; i < attributes.getLength(); i++){
				        String qn = attributes.getQName(i);
				        if(qn.equalsIgnoreCase(qName)){
				            return attributes.getValue(i);
				        }
				    }
				    return null;
				}
			};
			
			saxParser.parse("NesCarts (2012-10-22).xml", handler);
			
			
			System.out.println("executing...");
			
			statement.executeBatch();
			
			statement.close();
			connection.close();
			
			System.out.println("complete");
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
/*
	INSERT INTO product (name, year, system_id, developer_id, publisher_id)
	SELECT "game", 1994, 1, dev.id, pub.id
	FROM company AS dev
	JOIN company AS pub
	WHERE dev.name = "SNK" && pub.name = "Square"
	
	
	INSERT INTO product (name, year, system_id, developer_id, publisher_id)
	SELECT "game", 1994, 1, (SELECT id FROM company WHERE name = "SNK"), (SELECT id FROM company WHERE name = "Square");

*/
	}

}
