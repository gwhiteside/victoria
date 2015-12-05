package net.georgewhiteside.victoria;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class JTableCustom extends JTable
{	
	private static final Insets INSETS = new Insets(1, 1, 1, 1);
	private static final Border BORDER = new EmptyBorder(INSETS);
	
	public JTableCustom() {
		setFillsViewportHeight(true);
		//setRowHeight(20);
		setIntercellSpacing(new Dimension(0, 2));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setShowGrid(false);
		setTableHeader(null);
		setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
		}); 
	}
	
	public Insets getCellInsets() { return INSETS; }
}

/* some quick (and brittle) search table sizing tweaks 

FontMetrics fontMetrics = tableSearch.getFontMetrics(tableSearch.getFont());
Insets insets = tableSearch.getInsets();

System.out.println("Insets: " + insets);

TableColumn column = tableSearch.getColumnModel().getColumn(2);
int maxWidth = fontMetrics.stringWidth("100%");
column.setMinWidth(0);
column.setMaxWidth(insets.left + maxWidth + insets.right);
column.setPreferredWidth(column.getMaxWidth());

column = tableSearch.getColumnModel().getColumn(1);
maxWidth = fontMetrics.stringWidth("Sega Master System"); // approximately the longest string in this column
column.setMinWidth(0);
column.setMaxWidth(insets.left + maxWidth + insets.right);
column.setPreferredWidth(column.getMaxWidth());
*/