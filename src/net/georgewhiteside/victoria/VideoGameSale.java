package net.georgewhiteside.victoria;

import com.ebay.services.finding.SearchItem;

public class VideoGameSale {
	private long saleId;
	private int productId;
	private long timestamp;
	private int price; 
	private String title;
	
	public VideoGameSale(int id, int productId, String title, int price, long timestamp) {
		this.saleId = id;
		this.productId = productId;
		this.title = title;
		this.price = price;
		this.timestamp = timestamp;
	}
	
	public VideoGameSale(SearchItem sale, int id) {
		saleId = Long.parseLong(sale.getItemId());
		productId = id;
		timestamp = EbayUtils.getEndTimeUnix(sale);
		price = EbayUtils.getPriceWithShipping(sale);
		title = sale.getTitle();
	}
	
	public long getSaleId() { return saleId; }

	public int getProductId() { return productId; }

	public long getTimestamp() { return timestamp; }

	public int getPrice() { return price; }

	public String getTitle() { return title; }
}
