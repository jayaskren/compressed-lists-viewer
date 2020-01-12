package com.askren.data.editor;

import java.awt.Component;

import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class EfficientColumnFactory extends ColumnFactory{

	/** 
     * @param table the context the column will live in.
     * @param columnExt the column to configure.
     * @param margin the extra spacing to add twice, if -1 uses this factories
     *        default
     * @param max an upper limit to preferredWidth, -1 is interpreted as no
     *        limit
     * @throws IllegalStateException if column is not visible
     * 
     * @see #setDefaultPackMargin(int)
     * @see org.jdesktop.swingx.JXTable#packTable(int)
     * @see org.jdesktop.swingx.JXTable#packColumn(int, int)
     * 
     */
    public void packColumn(JXTable table, TableColumnExt columnExt, int margin,
            int max) {
        if (!columnExt.isVisible()) 
            throw new IllegalStateException("column must be visible to pack");
        
        int column = table.convertColumnIndexToView(columnExt.getModelIndex());
        int width = 0;
        TableCellRenderer headerRenderer = getHeaderRenderer(table, columnExt);
        if (headerRenderer != null) {
            Component comp = headerRenderer.getTableCellRendererComponent(table,
                    columnExt.getHeaderValue(), false, false, 0, column);
            width = comp.getPreferredSize().width;
        }      
        TableCellRenderer renderer = getCellRenderer(table, columnExt);
        int totalRows = getRowCount(table); 
        int numRows = Math.min(50, totalRows);
        for (int r = 0; r < numRows; r++) {
            Component comp = renderer.getTableCellRendererComponent(table, table
                    .getValueAt(r, column), false, false, r, column);
            width = Math.max(width, comp.getPreferredSize().width);
        }
        
        for (int r = totalRows - numRows; r < totalRows; r++) {
            Component comp = renderer.getTableCellRendererComponent(table, table
                    .getValueAt(r, column), false, false, r, column);
            width = Math.max(width, comp.getPreferredSize().width);
        }
        
        if (margin < 0) {
            margin = getDefaultPackMargin();
        }
        width += 2 * margin;

        /* Check if the width exceeds the max */
        if (max != -1 && width > max)
            width = max;

        columnExt.setPreferredWidth(width);

    }
    
}
