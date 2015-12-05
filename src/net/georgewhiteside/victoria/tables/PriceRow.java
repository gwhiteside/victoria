package net.georgewhiteside.victoria.tables;

import net.georgewhiteside.victoria.VideoGame;

public class PriceRow {
	VideoGame videoGame;
	String price;
	
	public PriceRow(VideoGame vg, String p) {
		videoGame = vg;
		price = p;
	}
	
	public String getPrice() { return price; }
	
	public VideoGame getVideoGame() { return videoGame; }
	
	public void setPrice(String p) { price = p; }
}