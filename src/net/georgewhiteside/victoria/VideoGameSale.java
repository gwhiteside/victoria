package net.georgewhiteside.victoria;

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
}
