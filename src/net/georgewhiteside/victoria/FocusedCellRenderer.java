package net.georgewhiteside.victoria;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class FocusedCellRenderer extends DefaultTableCellRenderer {
	
	private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 4, 1, 4);
	
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	if(column == 1) {					// game system column
    		setForeground(Color.GRAY);
    	} else if(column == 2) {			// percentage column
    		setForeground(Color.GREEN);
    	} else {							// set no color; relies on the super call picking up the default
    		setForeground(null); 
    	}
    	
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        setBorder(getNoFocusBorder());
        
        return this;
    }
    
    private Border getNoFocusBorder() {
    	Border border = UIManager.getBorder("Table.cellNoFocusBorder");
    	
    	if(border == null) {
    		return DEFAULT_NO_FOCUS_BORDER;
    	}
    	
    	return border;
    }
}