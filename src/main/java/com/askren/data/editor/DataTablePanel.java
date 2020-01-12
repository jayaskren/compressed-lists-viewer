package com.askren.data.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;

import com.compressedlists.DataTable;

/**
 * https://graphicdesign.stackexchange.com/questions/9246/what-are-great-fonts-for-information-heavy-data-tables
 * */
public class DataTablePanel extends JPanel {
	private JXTable table;
	private JFrame parentFrame;
	private SimpleProgressBar simmpleProgressBar = new SimpleProgressBar();
	private File[] currentFile;
	
	private DataTable data;
	
	public DataTablePanel(JFrame parentFrame) {
		this.parentFrame = parentFrame;
//        setMinimumSize(new Dimension(200, 200));
//        setPreferredSize(new Dimension(800, 800));
		setLayout(new BorderLayout(0, 0));
		setMinimumSize(new Dimension(200, 200));
		setPreferredSize(new Dimension(800, 800));
//		DefaultTableModel testModel = new DefaultTableModel();
//		testModel.addColumn("Col 1");
//		testModel.addColumn("Col 2");
//		Vector vec = new Vector();
//		vec.add("Test 1");
//		vec.add("test 2");
//		testModel.addRow(vec);
		table = new JXTable();
		table.setColumnFactory(new EfficientColumnFactory());
		table.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
		table.setEnabled(true);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setMinimumSize(new Dimension(200, 200));
		scrollPane.setPreferredSize(new Dimension(800, 800));
		scrollPane.setEnabled(true);
		add(scrollPane, BorderLayout.CENTER);
		
		
		add(simmpleProgressBar, BorderLayout.SOUTH);
	}
	
//	public void setModel(File file, AbstractDictionaryStringList[] columns) {
//		table.setModel(new DataTableModel(file, columns));
//	}

	public void importFile() {
		//Handle open button action.
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
        int returnVal = fc.showOpenDialog(DataTablePanel.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	currentFile = fc.getSelectedFiles();
            importFile(currentFile);
        } 
	}
	
	public void openFile() {
		JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(DataTablePanel.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	currentFile = new File[] {fc.getSelectedFile()};
        	openFile(currentFile[0]);
        } 
	}
	public void packTable() {
		invalidate();
		validate();
		revalidate();
		table.packAll();
	}
	
	private void openFile(final File currentFile) {
		SwingWorker<DataTable, Void> swingWorker = new SwingWorker<DataTable, Void>(){

			@Override
			protected DataTable doInBackground() throws Exception {
				return DataTable.readData(currentFile.getParentFile(), simmpleProgressBar);
			}

			@Override
			protected void done() {
				try {
					data = this.get();
					DataTableModel model = new DataTableModel(new File[] {currentFile}, data);
					
					
					table.setModel(model);
					
					invalidate();
					validate();
					revalidate();
//						table.packAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
			}
			
		};
		swingWorker.execute();
	}

	private void importFile(final File[] currentFile) {
		final Properties props = loadCorrespondingProperties(currentFile[0]);
		
		SwingWorker<DataTable, Void> swingWorker = new SwingWorker<DataTable, Void>(){

			

			@Override
			protected DataTable doInBackground() throws Exception {
				return FileUtils.readFile(currentFile, props, simmpleProgressBar);
			}

			@Override
			protected void done() {
				try {
					data = this.get();
					DataTableModel model = new DataTableModel(currentFile, data);
					
					
					table.setModel(model);
					
					invalidate();
					validate();
					revalidate();
//						table.packAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
			}
			
		};
		swingWorker.execute();
	}

	private Properties loadCorrespondingProperties(File file) {
 		Properties props = new Properties();
		try {
			String name = file.getName();
			String propFileName = name.substring(0, name.lastIndexOf('.')) + ".properties";
			File propFile = new File(file.getParentFile(), propFileName);
			if (propFile.exists()) {
				props.load(new FileInputStream(propFile));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}

	public void showMemoryUsage(int fontSize) {
		MemoryDialog dialog = new MemoryDialog(parentFrame, currentFile, data, fontSize, simmpleProgressBar);
		dialog.setVisible(true);
	}

	public void showLatency() {
		LatencyDialog dialog = new LatencyDialog(parentFrame, data);
		dialog.setVisible(true);
	}

	
}
