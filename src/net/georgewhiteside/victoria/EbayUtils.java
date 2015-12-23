package net.georgewhiteside.victoria;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
	
	public static String prepareQuery(String input) {
		String[] terms = input.split("\\s+");
		
		boolean termsOkay = true;
		for(String term : terms) {
			if(term.length() > 99) {
				termsOkay = false;
				break;
			}
		}
		
		if(termsOkay && input.length() <= 350) {
			return input;
		}
		
		return "";
	}
	
	private static int toCents(double floatingAmount) {
		return (int) (floatingAmount * 100 + 0.5);
	}
	
	public static int toCents(Amount amount) {
		if(amount.getCurrencyId().equals("USD") == false) {
			throw new IllegalArgumentException("Uh oh, convertedCurrentPrice's currencyId was not USD");
		}
		
		return toCents(amount.getValue());
	}
	
	public static VideoGameSale toVideoGameSale(SearchItem sale, int videoGameId) {
			long saleId = Long.parseLong(sale.getItemId());
			long timestamp = EbayUtils.getEndTimeUnix(sale);
			int price = EbayUtils.getPriceWithShipping(sale);
			String title = sale.getTitle();
			return new VideoGameSale(saleId, videoGameId, title, price, timestamp);
	}
	
	public static List<VideoGameSale> toVideoGameSales(List<SearchItem> searchItems, int videoGameId) {
		// convert data to friendlier container format
		List<VideoGameSale> videoGameSales = new ArrayList<VideoGameSale>();
		for(SearchItem item : searchItems) {
			videoGameSales.add(toVideoGameSale(item, videoGameId));
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
