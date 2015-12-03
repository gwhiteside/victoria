package net.georgewhiteside.victoria.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.georgewhiteside.victoria.VideoGame;

public class SearchTableModel extends AbstractTableModel {
	
	final String TITLE = "Title";
	final String SYSTEM = "System";
	
	List<VideoGame> rowData;
	String[] columnLabels = {TITLE, SYSTEM};
	
	public SearchTableModel() {
		rowData = new ArrayList<VideoGame>();
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
		switch(columnLabels[columnIndex]) {
			case TITLE: return String.class;
			case SYSTEM: return String.class;
		}
        return Object.class;
    }
	
	@Override
	public String getColumnName(int column) {
		return columnLabels[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		VideoGame vg = rowData.get(rowIndex);
		switch(columnLabels[columnIndex]) {
			case TITLE: return vg.getTitle();
			case SYSTEM: return vg.getSystemName();
		}
		return null;
	}
	
	public VideoGame getVideoGameAt(int rowIndex) {
		return rowData.get(rowIndex);
	}
	
	public void clear() {
		if(getRowCount() > 0) {
			int old = getRowCount();
	        rowData.clear();
	        fireTableRowsDeleted(0, old - 1);
		}
	}
	
	public void addRow(VideoGame vg) {
		insertRow(getRowCount(), vg);
	}

	public void insertRow(int row, VideoGame vg) {
		// something something asynchronously load data
		rowData.add(row, vg);
		fireTableRowsInserted(row, row);
	}
	
	public void removeRow(int row) {
		rowData.remove(row);
		fireTableRowsDeleted(row, row);
	}
}
