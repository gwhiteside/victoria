package net.georgewhiteside.victoria.tables;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.georgewhiteside.victoria.VideoGame;
import net.georgewhiteside.victoria.VideoGameDatabase;

public class PriceRow {
	PriceTableModel model;
	VideoGameDatabase database;
	VideoGame videoGame;
	String priceColumnString;
	
	State state = State.INITIAL;
	
	public PriceRow(VideoGameDatabase db, PriceTableModel ptm, VideoGame vg) {
		model = ptm;
		database = db;
		videoGame = vg;
	}
	
	public PriceRow(VideoGame vg) {
		videoGame = vg;
	}
	
	//public String getPrice() { return "0"; }
	
	public VideoGame getVideoGame() { return videoGame; }
	
	public void setPriceColumnString(String s) { priceColumnString = s; }
	
	public String getPriceColumnString() {
		return priceColumnString;
	}
	
	public enum State { INITIAL, CHECKING, NODATA, UPTODATE };
	
	class PriceRowUpdater extends SwingWorker<Integer, String> {
		PriceRow priceRow;

		public PriceRowUpdater(PriceRow pr) {
			priceRow = pr;
		}
		
		@Override
		protected Integer doInBackground() throws Exception {
			publish("Checking...");
			final VideoGame vg = priceRow.getVideoGame();
	    	long period = database.getSecondsSinceUpdate(vg);
	    	int daysSinceUpdate = (int) TimeUnit.SECONDS.toDays(period);
	    	if(daysSinceUpdate > 7) {
	    		publish("Updating...");
	    		String searchString = database.getSearchQuery(vg);
	    		System.out.println("search string length: " + searchString.length());
	    		if(searchString.length() == 0) {
	    			publish("No data");
	    			//EventQueue.invokeLater(new Runnable() {
					//	@Override
					//	public void run() {
							JOptionPane.showInputDialog("No search string exists for " + vg.getTitle());
					//	}
	    			//});
	    		}
	    	}
	    	
	    	//int delay = random.nextInt(5000 - 3000) + 2000;
	    	//Thread.sleep(delay);
	    	
			return daysSinceUpdate;
		}
		
		@Override
		protected void done() {
			try {
				priceRow.setPriceColumnString(get() + " days");
				model.firePriceUpdated(priceRow);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		@Override
	     protected void process(List<String> chunks) {
	         for (String string : chunks) {
	        	 priceRow.setPriceColumnString(string);
	             model.firePriceUpdated(priceRow);
	         }
	     }
	}
}