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

import net.georgewhiteside.victoria.VideoGame;
import net.georgewhiteside.victoria.VideoGameDatabase;

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
	VideoGameDatabase database;
	
	public PriceTableModel(VideoGameDatabase vgDatabase) {
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
	
	public PriceRow getRow(int index) {
		return rowData.get(index);
	}
	
	public void clear() {
		if(getRowCount() > 0) {
			int old = getRowCount();
	        rowData.clear();
	        fireTableRowsDeleted(0, old - 1);
		}
	}
	
	public void addRow(PriceRow priceRow) {
		insertRow(getRowCount(), priceRow);
	}
	
	public void addRow(VideoGame vg) {
		PriceRow priceRow = new PriceRow(vg);
		
		addRow(priceRow);
		
		//new PriceRowUpdater(priceRow).execute();
	}
	
	public void insertRow(int index, PriceRow priceRow) {
		updateRowAsync(priceRow); // something something asynchronously load data
		rowData.add(index, priceRow);
		fireTableRowsInserted(index, index);
	}
	
	public void removeRow(int index) {
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
		
		int i = 0;
		int matchingRowId = matchingRow.getVideoGame().getId();
    	for(PriceRow x : rowData) {
    		if(x.getVideoGame().getId() == matchingRowId) {
    			fireTableCellUpdated(i, 2);
    		}
    		i++;
    	}
	}

	private void updateRowAsync(PriceRow pr) {
		new PriceRowUpdater(pr).execute();
	}
	
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
	    	
	    	int delay = random.nextInt(5000 - 3000) + 2000;
	    	Thread.sleep(delay);
	    	
			return daysSinceUpdate;
		}
		
		@Override
		protected void done() {
			try {
				priceRow.setPriceColumnString(get() + " days");
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
	}
}