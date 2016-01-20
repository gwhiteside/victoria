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
		this.fireTableDataChanged();
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
	
	public double getPriceTotal() {
		double total = 0;
		for(PriceRow row : rowData) {
			total += row.getPrice();
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
		double price;
		boolean needsQuery = false;
		
		public PriceRow(VideoGame vg) {
			videoGame = vg;
			update();
		}
		
		public void firePriceUpdate() {
			firePriceUpdated(this);
		}
		
		public VideoGame getVideoGame() { return videoGame; }
		
		public void setPriceColumnString(String s) { priceColumnString = s; }
		
		public String getPriceColumnString() {
			return priceColumnString;
		}
		
		public double getPrice() {
			return price;
		}
		
		public void setPrice(double p) {
			price = p;
		}
		
		public void update() {
			new PriceRowUpdater(this, database, ebay).execute();
		}
		
		public boolean needsQuery() { return needsQuery; }
		
		public void setNeedsQuery(boolean nq) {
			needsQuery = nq;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireRowUpdated(PriceRow.this);
				}
			});
		}
	}
}