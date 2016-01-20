package net.georgewhiteside.victoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.services.finding.SearchItem;

public class BackgroundUpdater {
	
	Database database;
	EbayMiner ebay;
	Timer timer;
	TimerTask task;
	
	Config config = Config.getInstance();
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	ProgressDialog dialog;
	
	public class Updater extends SwingWorker<Void, Integer> {
		
		int max = 0;

		@Override
		protected Void doInBackground() throws Exception {
			log.info("Beginning regularly scheduled search update...");
			long startTime = System.currentTimeMillis();
			
			int interval = Integer.valueOf(config.getProperty(Config.BACKGROUND_PRICE_UPDATE_INTERVAL));
			List<Search> searches = database.getSearchesOlderThan(interval);
			
			if(searches.isEmpty()) {
				return null;
			}
			
			max = searches.size();
			publish(0); // kicks off progress dialog
			
			int count = 0;
			for(Search search : searches) {
				String searchString = search.getQuery();
				
				if(searchString == null || searchString.length() == 0) {
					log.warn("Item id {} search string is null or length 0", search.getProductId());
				} else {
					long currentUnixTime = System.currentTimeMillis() / 1000;
					long lastUpdateUnixTime = search.getTimestamp();
					List<SearchItem> searchItems = ebay.getSales(searchString, lastUpdateUnixTime, currentUnixTime);
					
					List<VideoGameSale> videoGameSales = EbayUtils.toVideoGameSales(searchItems, search.getProductId());
					
					count++;
					if(database.insertSales(videoGameSales)) {
						database.updateSearchTimestamp(search.getProductId(), currentUnixTime);
						count++;
					} else {
						log.error("There was an error inserting database records; not updating search timestamp");
					}
				}
				
				publish(count);
			}
			
			float totalTime = (System.currentTimeMillis() - startTime) / 1000f;
			log.info("Scheduled search update complete. {} product searches updated in {} seconds.", count, totalTime);
			
			return null;
		}
		
		@Override
		protected void process(List<Integer> chunks) {
			for(Integer value : chunks) {
				if(value == 0) {
					dialog = new ProgressDialog();
					dialog.setMinimum(0);
					dialog.setMaximum(max);
					dialog.setProgress(0);
					dialog.setVisible(true);
				} else {
					dialog.setProgress(value);
				}
			}
		}
		
		@Override
		protected void done() {
			if(dialog != null) {
				dialog.dispose();
				dialog = null;
			}
		}
	}

	public BackgroundUpdater(Database db, EbayMiner em) {
		
		database = db;
		ebay = em;
		
		task = new TimerTask() {
			@Override
			public void run() {
				Updater updater = new Updater();
				updater.execute();
			}
		};
		
		timer = new Timer();
		int interval = Integer.valueOf(config.getProperty(Config.BACKGROUND_PRICE_CHECK_INTERVAL));
		timer.scheduleAtFixedRate(task, 0, TimeUnit.DAYS.toMillis(interval));
	}
	
	public void shutdown() {
		log.debug("Disabling background updater");
		timer.cancel();
	}
}