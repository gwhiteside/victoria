package net.georgewhiteside.victoria;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.services.client.ClientConfig;
import com.ebay.services.client.FindingServiceClientFactory;
import com.ebay.services.finding.Amount;
import com.ebay.services.finding.FindCompletedItemsRequest;
import com.ebay.services.finding.FindCompletedItemsResponse;
import com.ebay.services.finding.FindingServicePortType;
import com.ebay.services.finding.PaginationInput;
import com.ebay.services.finding.SearchItem;
import com.ebay.services.finding.SortOrderType;

//350 characters per search; 99 characters per word ("word" defined as consecutive characters

public class EbayUtils {
	
	public static String EBAY_CAT_VIDEO_GAMES = "139973";
	private static final TimeZone TZ_UTC = TimeZone.getTimeZone("Etc/UTC");
	private static Logger log = LoggerFactory.getLogger(EbayUtils.class);

	private EbayUtils() {
	}
	
	/**
	 * Gets the ending time of a {@link com.ebay.services.finding.SearchItem SearchItem} in Unix time
	 * @param item eBay finding kit SearchItem object
	 * @return the ending time, in seconds since 00:00:00 UTC Jan. 1st, 1970
	 */
	public static long getEndTimeUnix(SearchItem item) {
		return item.getListingInfo().getEndTime().getTimeInMillis() / 1000;
	}
	
	public static int getPriceWithShipping(SearchItem item) {
		int itemPrice = toCents(item.getSellingStatus().getConvertedCurrentPrice());
		int shippingPrice = toCents(item.getShippingInfo().getShippingServiceCost());
		return itemPrice + shippingPrice;
	}
	
	private static int toCents(double floatingAmount) {
		return (int) (floatingAmount * 100 + 0.5);
	}
	
	public static int toCents(Amount amount) {
		String currencyId = amount.getCurrencyId();
		
		if(currencyId == null) {
			throw new NullPointerException("toCents: Amount's CurrencyId was null");
		}
		
		if(amount.getCurrencyId().equals("USD") == false) {
			throw new IllegalArgumentException("Uh oh, convertedCurrentPrice's currencyId was not USD");
		}
		
		return toCents(amount.getValue());
	}
	
	public static VideoGameSale toVideoGameSale(SearchItem sale, int videoGameId) {
			long saleId = Long.parseLong(sale.getItemId());
			long timestamp = EbayUtils.getEndTimeUnix(sale);
			
			// sometimes a sale has a glitched shipping calculation due to eBay error, user error, or both
			// if that happens, no shippingServiceCost is reported which we'll indicate by returning null
			Amount amount = sale.getShippingInfo().getShippingServiceCost();
			if(amount == null) {
				log.warn("saleId {} - glitched calculated shipping detected; discarding", saleId); 
				return null;
			}
			
			int price = EbayUtils.getPriceWithShipping(sale);
			String title = sale.getTitle();
			return new VideoGameSale(saleId, videoGameId, title, price, timestamp);
	}
	
	public static List<VideoGameSale> toVideoGameSales(List<SearchItem> searchItems, int videoGameId) {
		// convert data to friendlier container format
		List<VideoGameSale> videoGameSales = new ArrayList<VideoGameSale>();
		
		for(SearchItem item : searchItems) {
			VideoGameSale vgs = toVideoGameSale(item, videoGameId);
			if(vgs != null) {
				videoGameSales.add(vgs);
			} else {
				// glitched calculated shipping; do nothing with it
			}
		}
		
		return videoGameSales;
	}
	
	public static String toISO8601(Calendar calendar) {
		return String.format("%tFT%<tT.%<tLZ", calendar);
	}
	
	public static String unixTimeISO8601(long seconds) {
		Calendar calendar = Calendar.getInstance(TZ_UTC);
		calendar.setTimeInMillis(seconds * 1000);
		return toISO8601(calendar);
	}
	
	public static String unixTimeISO8601Exclusive(long seconds) {
		Calendar calendar = Calendar.getInstance(TZ_UTC);
		calendar.setTimeInMillis(seconds * 1000 - 1);
		return toISO8601(calendar);
	}
	
	public static String currentTimeISO8601() {
		return toISO8601(Calendar.getInstance(TZ_UTC));
	}
}
