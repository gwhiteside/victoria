package net.georgewhiteside.victoria;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class FocusedCellRenderer extends DefaultTableCellRenderer {
	
	public static final Insets INSETS = new Insets(1, 1, 1, 1);
	private static final Border BORDER = new EmptyBorder(INSETS);
	
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	if(column == 1) {					// game system column
    		setForeground(Color.GRAY);
    	} else if(column == 2) {			// percentage column
    		setForeground(Color.GREEN);
    		//value = "trololol";
    	} else {							// set no color; let the super call pick up the default
    		setForeground(null); 
    	}
    	
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
    	setBorder(BORDER);
        
        return this;
    }
}