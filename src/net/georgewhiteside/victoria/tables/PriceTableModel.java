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
	final String QUANTITY = "Quantity";
	
	List<PriceRow> rowData;
	String[] columnLabels = {TITLE, SYSTEM, PRICE, QUANTITY};
	
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
			case QUANTITY: return priceRow.getQuantity();
		}
		return null;
	}
	
	private int getColumnIndex(String label) {
		int column = -1;
		for(int i = 0; i < columnLabels.length; i++) {
			if(columnLabels[i].equals(label)) {
				column = i;
				break;
			}
		}
		return column;
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
	
	/**
	 * Finds the first row containing VideoGame vg
	 * @param vg
	 */
	private int indexOf(VideoGame vg) {
		int index = -1;

		for(int i = 0; i < rowData.size(); i++) {
			PriceRow priceRow = rowData.get(i);
			if(priceRow.getVideoGame().equals(vg)) {
				index = i;
				break;
			}
		}
			
		return index;
	}
	
	private void addRow(PriceRow priceRow) {
		insertRow(getRowCount(), priceRow);
	}
	
	public void addRow(VideoGame vg) {
		int index = indexOf(vg);
		if(index == -1) {
			// row doesn't exist; create a new one
			PriceRow priceRow = new PriceRow(vg);
			addRow(priceRow);
		} else {
			// increment the existing row
			PriceRow priceRow = getRow(index);
			priceRow.incrementQuantity();
			fireCellUpdated(priceRow, QUANTITY);
		}
	}
	
	private void insertRow(int index, PriceRow priceRow) {
		rowData.add(index, priceRow);
		fireTableRowsInserted(index, index);
	}
	
	public void removeRow(int index) {
		rowData.remove(index);
		fireTableRowsDeleted(index, index);
	}
	
	public double getPriceTotal() {
		double total = 0;
		for(PriceRow row : rowData) {
			total += row.getPrice() * row.getQuantity();
		}
		return total;
	}
	
	protected void fireCellUpdated(PriceRow matchingRow, String column) {
		int iRow = rowData.indexOf(matchingRow);
		int iCol = getColumnIndex(column);
		fireTableCellUpdated(iRow, iCol);
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
		int quantity;
		boolean needsQuery = false;
		
		public PriceRow(VideoGame vg, int quantity) {
			videoGame = vg;
			setQuantity(quantity);
			update();
		}
		
		public PriceRow(VideoGame vg) {
			this(vg, 1);
		}
		
		public void firePriceUpdate() {
			fireCellUpdated(this, PRICE);
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
		
		public int getQuantity() {
			return quantity;
		}
		
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
		
		public void incrementQuantity() {
			quantity++;
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