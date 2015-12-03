package net.georgewhiteside.victoria.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.georgewhiteside.victoria.VideoGame;

@SuppressWarnings("serial")
public class PriceTableModel extends AbstractTableModel {
	
	final String TITLE = "Title";
	final String SYSTEM = "System";
	final String PRICE = "Price";
	
	List<PriceRow> rowData;
	String[] columnLabels = {TITLE, SYSTEM, PRICE};
	
	public PriceTableModel() {
		rowData = new ArrayList<PriceRow>();
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
			case PRICE: return "PRICE";
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

	public void insertRow(int index, PriceRow priceRow) {
		// something something asynchronously load data
		rowData.add(index, priceRow);
		fireTableRowsInserted(index, index);
	}
	
	public void removeRow(int index) {
		rowData.remove(index);
		fireTableRowsDeleted(index, index);
	}
}