package net.georgewhiteside.victoria;

import java.util.Comparator;

public class VideoGameSale {
	private long saleId;
	private int productId;
	private long timestamp;
	private int price; 
	private String title;
	
	public VideoGameSale(long saleId, int videoGameId, String title, int price, long timestamp) {
		this.saleId = saleId;
		this.productId = videoGameId;
		this.title = title;
		this.price = price;
		this.timestamp = timestamp;
	}
	
	public long getSaleId() { return saleId; }

	public int getProductId() { return productId; }

	public long getTimestamp() { return timestamp; }

	public int getPrice() { return price; }

	public String getTitle() { return title; }
	
	private static class SortTimestamp implements Comparator<VideoGameSale> {
		@Override
		public int compare(VideoGameSale o1, VideoGameSale o2) {
			long a = o1.getTimestamp();
			long b = o2.getTimestamp();
			return a < b ? -1 : a == b ? 0 : 1;
		}
	}
	
	private static class SortPrice implements Comparator<VideoGameSale> {
		@Override
		public int compare(VideoGameSale o1, VideoGameSale o2) {
			int a = o1.getPrice();
			int b = o2.getPrice();
			return a < b ? -1 : a == b ? 0 : 1;
		}
	}
	
	public static Comparator<VideoGameSale> timestampComparator() {
		return new SortTimestamp();
	}
	
	public static Comparator<VideoGameSale> priceComparator() {
		return new SortPrice();
	}
}
