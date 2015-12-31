package net.georgewhiteside.victoria.tables;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.services.finding.SearchItem;

import net.georgewhiteside.victoria.Config;
import net.georgewhiteside.victoria.EbayMiner;
import net.georgewhiteside.victoria.EbayUtils;
import net.georgewhiteside.victoria.VideoGame;
import net.georgewhiteside.victoria.Database;
import net.georgewhiteside.victoria.VideoGameSale;

// what I should be doing is (in parallel to the rowData list) tracking these pricerows in a set (or videogame -> price in a map)
// 

@SuppressWarnings("serial")
public class PriceTableModel extends AbstractTableModel {
	
	final String TITLE = "Title";
	final String SYSTEM = "System";
	final String PRICE = "Price";
	
	List<PriceRow> rowData;
	String[] columnLabels = {TITLE, SYSTEM, PRICE};
	
	Random random = new Random();
	Database database;
	EbayMiner ebay;
	
	
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	public PriceTableModel(Database vgDatabase, EbayMiner ebayMiner) {
		rowData = new ArrayList<PriceRow>();
		database = vgDatabase;
		ebay = ebayMiner;
	}

	@Override
	public int getRowCount() {
		return rowData.size();
	}

	@Override
	public int getColumnCount() {
		return columnLabels.length;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
    }
	
	@Override
	public String getColumnName(int column) {
		return columnLabels[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		PriceRow priceRow = rowData.get(rowIndex);
		VideoGame vg = priceRow.getVideoGame();
		switch(columnLabels[columnIndex]) {
			case TITLE: return vg.getTitle();
			case SYSTEM: return vg.getSystemName();
			case PRICE: return priceRow.getPriceColumnString();
		}
		return null;
	}
	
	public PriceRow getRow(int index) {
		return rowData.get(index);
	}
	
	public VideoGame getVideoGame(int row) {
		return getRow(row).getVideoGame();
	}
	
	public void clear() {
		if(getRowCount() > 0) {
			int old = getRowCount();
	        rowData.clear();
	        fireTableRowsDeleted(0, old - 1);
		}
	}
	
	private void addRow(PriceRow priceRow) {
		insertRow(getRowCount(), priceRow);
	}
	
	public void addRow(VideoGame vg) {
		PriceRow priceRow = new PriceRow(vg);
		
		addRow(priceRow);
		
		//new PriceRowUpdater(priceRow).execute();
	}
	
	private void insertRow(int index, PriceRow priceRow) {
		//updateRowAsync(priceRow); // something something asynchronously load data
		rowData.add(index, priceRow);
		fireTableRowsInserted(index, index);
	}
	
	private void removeRow(int index) {
		rowData.remove(index);
		fireTableRowsDeleted(index, index);
	}
	
	public int getPriceTotal() {
		int total = 0;
		for(PriceRow row : rowData) {
			// add totals
		}
		return total;
	}
	
	protected void firePriceUpdated(PriceRow matchingRow) {
		/*
		int i = 0;
		int matchingRowId = matchingRow.getVideoGame().getId();
    	for(PriceRow x : rowData) {
    		if(x.getVideoGame().getId() == matchingRowId) {
    			fireTableCellUpdated(i, 2);
    		}
    		i++;
    	}
    	*/
		
		int i = rowData.indexOf(matchingRow);
		fireTableCellUpdated(i, 2);
	}
	
	protected void fireRowUpdated(PriceRow matchingRow) {
		int i = rowData.indexOf(matchingRow);
		fireTableRowsUpdated(i, i);
	}
	
	public boolean needsQuery(int index) {
		return rowData.get(index).needsQuery();
	}
	
	public class PriceRow {
		VideoGame videoGame;
		String priceColumnString;
		boolean needsQuery = false;
		
		public PriceRow(VideoGame vg) {
			videoGame = vg;
			update();
		}
		
		//public String getPrice() { return "0"; }
		
		public VideoGame getVideoGame() { return videoGame; }
		
		public void setPriceColumnString(String s) { priceColumnString = s; }
		
		public String getPriceColumnString() {
			return priceColumnString;
		}
		
		public void update() {
			new PriceRowUpdater(this).execute();
		}
		
		public boolean needsQuery() { return needsQuery; }
		
		private class PriceRowUpdater extends SwingWorker<String, String> {
			PriceRow priceRow;
			Config config;

			public PriceRowUpdater(PriceRow pr) {
				priceRow = pr;
				config = Config.getInstance();
			}
			
			@Override
			protected String doInBackground() throws Exception {
				publish("Updating...");
				
				final VideoGame vg = priceRow.getVideoGame();
				
				long lastUpdateUnixTime = database.getSearchTimestamp(videoGame);
				long currentUnixTime = System.currentTimeMillis() / 1000;
				long secondsSinceUpdate = currentUnixTime - lastUpdateUnixTime;
		    	int daysSinceUpdate = (int) TimeUnit.SECONDS.toDays(secondsSinceUpdate);
		    	
		    	List<VideoGameSale> videoGameSales;
		    	
		    	int interval = Integer.valueOf(config.getProperty(Config.UPDATE_INTERVAL_DAYS));
		    	if(daysSinceUpdate >= 1) {
		    		
		    		String searchString = database.getSearchQuery(vg);
		    		
		    		if(searchString == null || searchString.length() == 0) {
		    			
		    			return null; //publish("No data");
		    			
		    		} else {
		    			// get updated search results
		    			List<SearchItem> searchItems = ebay.getSales(searchString, lastUpdateUnixTime, currentUnixTime);
		    			
		    			// convert data to friendlier container format
		    			videoGameSales = EbayUtils.toVideoGameSales(searchItems, vg.getId());
		    			
		    			// commit sales data to database
		    			database.insertSales(videoGameSales);
		    			
		    			// update search timestamp
		    			database.updateSearchTimestamp(vg.getId(), currentUnixTime);
		    			
		    			lastUpdateUnixTime = currentUnixTime;
		    		}
		    	}
		    	
		    	// calculate and return median price
		    	List<VideoGameSale> sales = database.getPriceHistory(vg);
		    	
		    	if(sales.isEmpty()) {
		    		return null; // TODO currently interpreted as "no search string"; should be distinct "no data available" condition
		    	}
		    	
		    	Collections.sort(sales, VideoGameSale.timestampComparator());
		    	VideoGameSale max = sales.get(sales.size() - 1);
		    	long lastTimestamp = max.getTimestamp();
		    	long firstTimestamp = lastTimestamp - TimeUnit.DAYS.toSeconds(28);
		    	
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
		    	
		    	String result = NumberFormat.getCurrencyInstance().format(value / 100.0);
		    	
		    	//Collections.
		    	
				return result; // return String.valueOf(daysSinceUpdate);
			}
			
			@Override
			protected void done() {
				try {
					Object value = get();
					if(value == null) {
						setNeedsQuery(true);
		    			log.debug("No search string for {}", priceRow.getVideoGame().getTitle());
						priceRow.setPriceColumnString("No data");
					} else {
						setNeedsQuery(false);
		    			//log.debug("\"{}\" search string length: {}", vg.getTitle(), searchString.length());
						priceRow.setPriceColumnString(get() + " days");
					}
					firePriceUpdated(priceRow);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			@Override
		     protected void process(List<String> chunks) {
		         for (String string : chunks) {
		        	 priceRow.setPriceColumnString(string);
		             firePriceUpdated(priceRow);
		         }
		     }
			
			private void setNeedsQuery(boolean nq) {
				needsQuery = nq;
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						fireRowUpdated(priceRow);
					}
				});
				
			}
		}
	}
}