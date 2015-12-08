package net.georgewhiteside.victoria.tables;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
			case PRICE: return priceRow.getPrice();
		}
		return null;
	}
	
	public PriceRow getRow(int index) {
		return rowData.get(index);
	}
	
	public VideoGame getVideoGameByRow(int index) {
		return getRow(index).getVideoGame();
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
		// do some background loading here
		
		final PriceRow priceRow = new PriceRow(vg, "");
		
		addRow(priceRow);
		
		int delay = random.nextInt(5000 - 3000) + 2000;
		
		final Timer timer = new Timer(delay, new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
		    	long period = database.getSecondsSinceUpdate(priceRow.getVideoGame());
		    	long daysSinceUpdate = TimeUnit.SECONDS.toDays(period);
		    	if(daysSinceUpdate > 7) {
		    		// do an update
		    	}
		    	priceRow.setPrice("checking... " + daysSinceUpdate);
		    	firePriceUpdated(priceRow);
		    }    
		});
		timer.setRepeats(false);
		timer.start();
	}
	
	private void firePriceUpdated(PriceRow matchingRow) {
		
		int i = 0;
		int matchingRowId = matchingRow.getVideoGame().getId();
    	for(PriceRow x : rowData) {
    		if(x.getVideoGame().getId() == matchingRowId) {
    			fireTableCellUpdated(i, 2);
    		}
    		i++;
    	}
	}

	public void insertRow(int index, PriceRow priceRow) {
		// something something asynchronously load data
		rowData.add(index, priceRow);
		fireTableRowsInserted(index, index);
	}
	
	public void removeRow(int index) {
		rowData.remove(index);
		fireTableRowsDeleted(index, index);
	}
	
	class Test extends SwingWorker<Object, Object> {
		
		// could get sale price data in a date range or the complete history
		// need to check

		public Test() {
			
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			
			return null;
		}
		
	}
}