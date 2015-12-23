package net.georgewhiteside.victoria;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Config {
	private static final String FILENAME = "victoria.properties";
	//private static final String FILENAME = "configuration.xml";
	private Properties properties = null;
	private static Config instance = null;
	
	public static final String DB_URL = "databaseUrl";
	public static final String DB_USER = "databaseUser";
	public static final String DB_PASS = "databasePass";
	public static final String EBAY_APP_ID = "ebayAppID";
	public static final String POSTAL_CODE = "postalCode";
	public static final String UPDATE_INTERVAL_DAYS = "updateIntervalDays";
	
	private Config() {
		properties = new Properties();
		try {
			FileInputStream is = new FileInputStream(FILENAME);
			//properties.loadFromXML(is);
			properties.load(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Config getInstance() {
		if(instance == null) {
			instance = new Config();
		}
		return instance;
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
}
