package net.georgewhiteside.victoria.tables;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.georgewhiteside.victoria.VideoGame;
import net.georgewhiteside.victoria.Database;

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
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	public PriceTableModel(Database vgDatabase) {
		rowData = new ArrayList<PriceRow>();
		database = vgDatabase;
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
	
	private PriceRow getRow(int index) {
		return rowData.get(index);
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
	
	private class PriceRow {
		VideoGame videoGame;
		String priceColumnString;
		boolean needsQuery = true;
		
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

			public PriceRowUpdater(PriceRow pr) {
				priceRow = pr;
			}
			
			@Override
			protected String doInBackground() throws Exception {
				publish("Updating...");
				
				final VideoGame vg = priceRow.getVideoGame();
		    	long period = database.getSecondsSinceUpdate(vg);
		    	int daysSinceUpdate = (int) TimeUnit.SECONDS.toDays(period);
		    	
		    	if(daysSinceUpdate > 7) {
		    		
		    		String searchString = database.getSearchQuery(vg);
		    		
		    		if(searchString.length() == 0) {
		    			setNeedsQuery(true);
		    			log.debug("No search string for {}", vg.getTitle());
		    			return null; //publish("No data");
		    			
		    			//EventQueue.invokeLater(new Runnable() {
						//	@Override
						//	public void run() {
								//JOptionPane.showInputDialog("No search string exists for " + vg.getTitle());
						//	}
		    			//});
		    		} else {
		    			setNeedsQuery(false);
		    			log.debug("\"{}\" search string length: {}", vg.getTitle(), searchString.length());
		    		}
		    	} else {
		    		
		    	}
		    	
				return String.valueOf(daysSinceUpdate);
			}
			
			@Override
			protected void done() {
				try {
					Object value = get();
					if(value == null) {
						priceRow.setPriceColumnString("No data");
					} else {
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