package net.georgewhiteside.victoria.tables;

import java.awt.EventQueue;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import net.georgewhiteside.victoria.Config;
import net.georgewhiteside.victoria.Database;
import net.georgewhiteside.victoria.EbayMiner;
import net.georgewhiteside.victoria.EbayUtils;
import net.georgewhiteside.victoria.VideoGame;
import net.georgewhiteside.victoria.VideoGameSale;
import net.georgewhiteside.victoria.tables.PriceTableModel.PriceRow;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.services.finding.SearchItem;

public class PriceRowUpdater extends SwingWorker<String, String> {
	PriceRow priceRow;
	Config config;
	Database database;
	EbayMiner ebay;
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	public PriceRowUpdater(PriceRow pr, Database db, EbayMiner em) {
		priceRow = pr;
		database = db;
		ebay = em;
		config = Config.getInstance();
	}
	
	@Override
	protected String doInBackground() throws Exception {
		publish("Updating...");
		
		final VideoGame vg = priceRow.getVideoGame();
		
		long lastUpdateUnixTime = database.getSearchTimestamp(vg);
		long currentUnixTime = System.currentTimeMillis() / 1000 - 2; // HACK subtracting two from current time to compensate for apparent eBay server time drift or api bug
		long secondsSinceUpdate = currentUnixTime - lastUpdateUnixTime;
    	int daysSinceUpdate = (int) TimeUnit.SECONDS.toDays(secondsSinceUpdate);
    	
    	List<VideoGameSale> videoGameSales;
    	
    	int interval = Integer.valueOf(config.getProperty(Config.PRICE_ROW_UPDATE_INTERVAL));
    	if(daysSinceUpdate >= interval) {
    		
    		String searchString = database.getSearchQuery(vg);
    		
    		if(searchString == null || searchString.length() == 0) {
    			log.warn("Item id {} search string is null or length 0", vg.getId());
    			return null; //publish("No data");
    		} else {
    			// get updated search results
    			List<SearchItem> searchItems = ebay.getSales(searchString, lastUpdateUnixTime, currentUnixTime);
    			
    			// convert data to friendlier container format
    			videoGameSales = EbayUtils.toVideoGameSales(searchItems, vg.getId());
    			
    			// commit sales data to database
    			if(database.insertSales(videoGameSales)) {
    				database.updateSearchTimestamp(vg.getId(), currentUnixTime);
    			} else {
    				log.error("There was an error inserting database records; not updating search timestamp");
    			}
    		}
    	}
    	
    	// calculate and return median price
    	List<VideoGameSale> sales = database.getPriceHistory(vg);
    	
    	if(sales.isEmpty()) {
    		return null; // TODO currently interpreted as "no search string"; should be distinct "no data available" condition
    	}
    	
    	// TODO this probably needs to be redone more robustly
    	Collections.sort(sales, VideoGameSale.timestampComparator());
    	VideoGameSale max = sales.get(sales.size() - 1);
    	long lastTimestamp = max.getTimestamp();
    	long firstTimestamp = lastTimestamp - TimeUnit.DAYS.toSeconds(28); // TODO store this value in settings
    	
    	// grab all the elements between firstTimestamp and lastTimestamp
    	
    	List<VideoGameSale> monthSales = new ArrayList<VideoGameSale>();
    	
    	// TODO simplify this garbage
    	for(int i = sales.size() - 1; i >= 0; i--) {
    		VideoGameSale vgs = sales.get(i);
    		if(vgs.getTimestamp() >= firstTimestamp) {
    			monthSales.add(vgs);
    		} else {
    			break;
    		}
    	}
    	
    	double[] prices = new double[monthSales.size()];
    	
    	for(int i = 0; i < prices.length; i++) {
    		prices[i] = monthSales.get(i).getPrice();
    	}
    	
    	// calculate median
    	
    	Median median = new Median();
    	double value = median.evaluate(prices);
    	priceRow.setPrice(value / 100.0); // TODO is it a poor idea to update this here?
    	String result = NumberFormat.getCurrencyInstance().format(value / 100.0);
    	
		return result;
	}
	
	@Override
	protected void done() {
		try {
			Object value = get();
			if(value == null) {
				priceRow.setNeedsQuery(true);
    			log.debug("No search string for {}", priceRow.getVideoGame().getTitle());
    			priceRow.setPrice(0);
				priceRow.setPriceColumnString("No data");
			} else {
				priceRow.setNeedsQuery(false);
				priceRow.setPriceColumnString(get());
			}
			priceRow.firePriceUpdate();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void process(List<String> chunks) {
         for (String string : chunks) {
        	 priceRow.setPriceColumnString(string);
        	 priceRow.firePriceUpdate();
         }
     }
	

}
