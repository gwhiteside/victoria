package net.georgewhiteside.victoria;

import java.util.Objects;

public class VideoGame {
	private int product_id;
	private String title;
	private int system_id;
	private String systemName;
	private int year;
	private String region;
	
	public VideoGame(int productId, String title, int systemId, String systemName, int year, String region) {
		this.product_id = productId;
		this.title = title;
		this.system_id = systemId;
		this.systemName = systemName;
		this.year = year;
		this.region = region;
	}
	
	public int getId() { return product_id; }
	
	public String getTitle() { return title; }
	
	public int getSystemId() { return system_id; }
	
	public String getSystemName() { return systemName; }
	
	public int getYear() { return year; }
	
	public String getRegion() { return region; }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VideoGame == false)
			return false;
		
		if(obj == this)
			return true;
		
		VideoGame vg = (VideoGame) obj;
		
		return vg.getId() == getId();
	}
	
	@Override
	public int hashCode() {
		return getTitle().hashCode() + getId();
	}
	
	@Override
	public String toString() {
		return title;
	}
	
	
}
