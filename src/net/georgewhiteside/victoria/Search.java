package net.georgewhiteside.victoria;

public class Search {
	
	private int productId;
	private long timestamp;
	private String query;
	
	public Search(int productId, long timestamp, String query) {
		this.productId = productId;
		this.timestamp = timestamp;
		this.query = query;
	}
	
	public int getProductId() { return productId; }
	
	public long getTimestamp() { return timestamp; }
	
	public String getQuery() { return query; }
}
