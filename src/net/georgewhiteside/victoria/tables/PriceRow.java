package net.georgewhiteside.victoria.tables;

import net.georgewhiteside.victoria.VideoGame;

public class PriceRow {
	VideoGame videoGame;
	Object price;
	
	public PriceRow(VideoGame vg, Object p) {
		videoGame = vg;
		price = p;
	}
	
	VideoGame getVideoGame() { return videoGame; }
}