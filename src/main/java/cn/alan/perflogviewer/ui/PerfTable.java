package cn.alan.perflogviewer.ui;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class PerfTable extends JTable {	
  
	private static final long serialVersionUID = 1L;
	
	private static final Color COLOR_GRAY = new Color(240, 240, 240);

	public PerfTable() {
        super();
    }

    public PerfTable(TableModel tm) {
        super(tm);
    }

    public PerfTable(Object[][] data, Object[] columns) {
        super(data, columns);
    }

    public PerfTable(int rows, int columns) {
        super(rows, columns);
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
    	
    	DefaultTableCellRenderer render = new DefaultTableCellRenderer();; 
        
    	if (column <= 2) {
    		
    	} else {    		
    		render.setHorizontalAlignment(SwingConstants.CENTER);
    	}

        if ((row % 2) == 0) {
            render.setBackground(Color.WHITE);
        } else {
        	render.setBackground(COLOR_GRAY);
        }
        
        return render;
    }
}
