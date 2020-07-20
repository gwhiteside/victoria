package net.georgewhiteside.victoria;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.georgewhiteside.victoria.tables.PriceTableModel;

// https://tips4java.wordpress.com/2010/01/24/table-row-rendering/

@SuppressWarnings("serial")
public class JTableCustom extends JTable
{	
	private static final Insets INSETS = new Insets(1, 1, 1, 1);
	private static final Border BORDER = new EmptyBorder(INSETS);
	
	public JTableCustom() {
		super();
		setFillsViewportHeight(true);
		//setRowHeight(20);
		setIntercellSpacing(new Dimension(0, getIntercellSpacing().height));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setShowGrid(false);
		//setTableHeader(null);
		
		setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    	
		    	setForeground(null);
		    	
		    	/*
		    	if(column == 1) {					// game system column
		    		setForeground(Color.GRAY);
		    	} else if(column == 2) {			// percentage column
		    		setForeground(Color.GREEN);
		    		//value = "trololol";
		    	} else {							// set no color; let the super call pick up the default
		    		setForeground(null); 
		    	}
		    	*/
		    	
		        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		        
		    	//setBorder(BORDER);
		        
		        TableModel model = table.getModel();
		        if(model instanceof PriceTableModel) {
		    		PriceTableModel ptm = (PriceTableModel) model;
		    		if(ptm.needsQuery(row)) {
		    			if(table.isRowSelected(row)) {
		    				setForeground(Color.RED);
		    			} else {
		    				setForeground(Color.RED);
		    			}
		    		} else {
		    			
		    		}
		    	}
		        
		        return this;
		    }
		});
		
		addMouseListener(new MouseAdapter() {
		    @Override
		    public void mousePressed(MouseEvent e) {
		        int r = rowAtPoint(e.getPoint());
		        if (r >= 0 && r < getRowCount()) {
		            setRowSelectionInterval(r, r);
		        } else {
		            clearSelection();
		        }
		    }
		});
	}
	
	// row-level rendering
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        JComponent jc = (JComponent) c;
        jc.setBorder(BORDER);
        return c;
    }
	
	public Insets getCellInsets() { return INSETS; }
	
	public void setSelectedRow(int index) {
		setRowSelectionInterval(index, index);
	}
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