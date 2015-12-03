package net.georgewhiteside.victoria.tables;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.georgewhiteside.victoria.VideoGame;

@SuppressWarnings("serial")
public class SearchTableModel extends AbstractTableModel {
	
	final String TITLE = "Title";
	final String SYSTEM = "System";
	final String SCORE = "Match";
	
	List<SearchRow> rowData;
	String[] columnLabels = {TITLE, SYSTEM, SCORE};
	
	NumberFormat percentage = NumberFormat.getPercentInstance();
	
	public SearchTableModel() {
		rowData = new ArrayList<SearchRow>();
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
		SearchRow searchRow = rowData.get(rowIndex);
		VideoGame vg = searchRow.getVideoGame();
		switch(columnLabels[columnIndex]) {
			case TITLE: return vg.getTitle();
			case SYSTEM: return vg.getSystemName();
			case SCORE: return percentage.format(searchRow.getScore());
		}
		return null;
	}
	
	public SearchRow getRow(int index) {
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
	
	public void addRow(SearchRow searchRow) {
		insertRow(getRowCount(), searchRow);
	}

	public void insertRow(int index, SearchRow searchRow) {
		// something something asynchronously load data
		rowData.add(index, searchRow);
		fireTableRowsInserted(index, index);
	}
	
	public void removeRow(int index) {
		rowData.remove(index);
		fireTableRowsDeleted(index, index);
	}
}
