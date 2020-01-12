package com.askren.data.editor;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.table.AbstractTableModel;

import com.compressedlists.CompressedList;
import com.compressedlists.DataTable;
import com.compressedlists.impl.AbstractDictionaryStringList;

public class DataTableModel extends AbstractTableModel {

	private CompressedList[] columns;
	private String[] columnHeaders;
	private String[] uniqueHeaderNames;
	private File[] file;
	private NumberFormat rowNumberFormatter = NumberFormat.getIntegerInstance();
	
	public DataTableModel(File[] file, DataTable data) {
		this.columns = data.getColumns();
		columnHeaders = data.getHeaderNames();
		uniqueHeaderNames = data.getUniqueNames();
		this.file = file;
	}
	
	@Override
	public int getRowCount() {
		try {
			if (columns != null && columns.length > 0) {
				return columns[0].getSize();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getColumnCount() {
		try {
			return columns.length+1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return rowNumberFormatter.format(rowIndex);
		}
		try {
			CompressedList column = columns[columnIndex-1];
			return column.getValueDisplay(rowIndex);
		} catch (Exception e) {
			System.out.println("Failed To retrieve column " + uniqueHeaderNames[columnIndex-1]);
			e.printStackTrace();
		}
		return "Test";
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Row";
		}
		try {
			return uniqueHeaderNames[column-1];
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	public File[] getFile() {
		return file;
	}

}
