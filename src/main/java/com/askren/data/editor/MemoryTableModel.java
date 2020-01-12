package com.askren.data.editor;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import com.compressedlists.CompressedList;
import com.compressedlists.TextList;
import com.compressedlists.impl.StringListImpl;
import com.compressedlists.DataTable;
import com.compressedlists.DataType;
import com.compressedlists.IUpdatable;

public class MemoryTableModel extends AbstractTableModel {

	
	String[] header = new String[]{"Name", "Type", "Size (MB)", "Index Size (MB)", "Index Waste (MB)", "Unique Values (MB)", "Original Size In MB", "# Unique Values", "Time To Process (seconds)"};
	double[] columnSizes;
	double totalSize = 0.0;
	public static final double BYTE_TO_MB = 1000*1000;
	
	CompressedList[] columns;
	String[] uniqueNames;
	
	public MemoryTableModel(final MemoryDialog parentDialog, DataTable dataTable, final IUpdatable progress) {
		this.columns = dataTable.getColumns();
		this.uniqueNames = dataTable.getUniqueNames();
		columnSizes = new double[columns.length];
		progress.setMax(columnSizes.length);
		SwingWorker<Void, Void> swingWorker = 
					new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground() throws Exception {
//				ClassIntrospector inspector = new ClassIntrospector();
				for (int i=0; i<columns.length; i++) {
					System.out.println("inspecting column " + i);
					if (columns[i].getDataType().equals(DataType.STRING)) {
						StringListImpl stringCol = (StringListImpl) columns[i];
						long uniqueValSize = 0;
						for (String unique: stringCol.getUniqueValues()) {
							uniqueValSize += unique.length() * 2; // 2 bytes per character
						}
						columnSizes[i] = (stringCol.getIndexSizeInBytes().sizeInBytes + uniqueValSize) /  BYTE_TO_MB;
						totalSize += columnSizes[i] ;
//						try {
//							ObjectInfo info = inspector.introspect(columns[i]);
//							
//							columnSizes[i] = info.getDeepSize() / BYTE_TO_MB;
//							totalSize += columnSizes[i] ;
//						} catch (IllegalAccessException e) {
//							e.printStackTrace();
//						}
					} else {
						columnSizes[i] = columns[i].getSizeInBytes() / BYTE_TO_MB;
						totalSize += columnSizes[i];
					}
					
					progress.updateProgress(i, "Analyzed column " + uniqueNames[i]);
					parentDialog.repaint();
				}
				return null;
			}

			@Override
			protected void done() {
				progress.finish();
				parentDialog.repaint();
			}
			
		};
		swingWorker.execute();
		
	}
	
	@Override
	public String getColumnName(int column) {
		return header[column];
	}

	@Override
	public int getRowCount() {
		return columns.length + 1;
	}

	@Override
	public int getColumnCount() {
		return header.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex == columns.length) {
			long size = 0l;
			switch (columnIndex) {
			case 0:
				return "All " + (getRowCount()-1) + " columns";
			case 1:
				return "";
			case 2:
				return totalSize;
			case 3:
				for (CompressedList column: columns) {
					if (column instanceof TextList) {
						size += ((TextList)column).getIndexSizeInBytes().sizeInBytes;
					}
				}
				return size/BYTE_TO_MB;
			case 4:
				for (CompressedList column: columns) {
					if (column instanceof TextList) {
						size += ((TextList)column).getIndexSizeInBytes().waistedSizeInBytes;
					}
				}
				return size/BYTE_TO_MB;
			case 5:
				for (CompressedList column: columns) {
					if (column instanceof TextList) {
						size += ((TextList)column).getUniqueValuesSizeInBytes();
					}
				}
				return size/BYTE_TO_MB;
			case 6:
				double totalMB = 0.0;
				for (CompressedList column: columns) {
					totalMB += column.getOriginalSizeInBytes()/BYTE_TO_MB;	
				}
				return totalMB;
			case 7:
				for (CompressedList column: columns) {
					if (column instanceof TextList) {
						size += ((TextList)column).getUniqueValuesSize();
					}
				}
				return (int)size;
			case 8:
				for (CompressedList column: columns) {
					if (column instanceof TextList) {
						size += column.getTimeProcessed();
					}
				}
				return size/1000.0;
			default:
				break;
			}
		} else {
			switch (columnIndex) {
			case 0:
				return uniqueNames[rowIndex];// columns[rowIndex].getDisplayName();
			case 1:
				return columns[rowIndex].getDataType().name();
			case 2:
				return columnSizes[rowIndex];
			case 3:
				if (columns[rowIndex] instanceof TextList) {
					return ((TextList)columns[rowIndex]).getIndexSizeInBytes().sizeInBytes/BYTE_TO_MB;
				}
			case 4:
				if (columns[rowIndex] instanceof TextList) {
					return ((TextList)columns[rowIndex]).getIndexSizeInBytes().waistedSizeInBytes/BYTE_TO_MB;
				}
			case 5:
				if (columns[rowIndex] instanceof TextList) {
					return ((TextList)columns[rowIndex]).getUniqueValuesSizeInBytes()/BYTE_TO_MB;
				}
			case 6: 
				if (columns[rowIndex] instanceof TextList) {
					return ((TextList)columns[rowIndex]).getOriginalSizeInBytes()/BYTE_TO_MB;
				}
			case 7:
				if (columns[rowIndex] instanceof TextList) {
					return ((TextList)columns[rowIndex]).getUniqueValuesSize();
				}
			case 8:
				return columns[rowIndex].getTimeProcessed()/1000.0;
			default:
				break;
			}
		}
		return "";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 5:
			return Integer.class;
			
		default:
			return Double.class;
		}
	}

}
