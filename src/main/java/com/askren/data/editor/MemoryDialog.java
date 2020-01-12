package com.askren.data.editor;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import com.compressedlists.DataTable;
import com.compressedlists.IUpdatable;

public class MemoryDialog extends JDialog {
	
	public MemoryDialog(JFrame parentFrame, File[] file, DataTable dataTable, int fontSize, IUpdatable progress) {
		super(parentFrame, "Memory Usage - " + file[0].getName() + " ...", false);
		MemoryTableModel memoryData = new MemoryTableModel(this, dataTable, progress);
		JXTable table = new JXTable(memoryData);
		table.setColumnFactory(new EfficientColumnFactory());
		table.setFont(new Font(DataEditorFrame.FONT_NAME, Font.PLAIN, fontSize));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(1600, 1200));
		getContentPane().add(scrollPane);
		System.out.println("Available processors (cores): " + 
			        Runtime.getRuntime().availableProcessors());

	    /* Total amount of free memory available to the JVM */
	    System.out.println("Free memory (MB): " + 
	        Runtime.getRuntime().freeMemory()/(double)MemoryTableModel.BYTE_TO_MB + " MB");

	    /* This will return Long.MAX_VALUE if there is no preset limit */
	    long maxMemory = Runtime.getRuntime().maxMemory();
	    /* Maximum amount of memory the JVM will attempt to use */
	    System.out.println("Maximum memory (bytes): " + 
	        (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory/(double)MemoryTableModel.BYTE_TO_MB) + " MB");

	    /* Total memory currently available to the JVM */
	    System.out.println("Total memory available to JVM (bytes): " + 
	        Runtime.getRuntime().totalMemory()/(double)MemoryTableModel.BYTE_TO_MB + " MB");
	    invalidate();
		validate();
		revalidate();
	    table.packAll();
		pack();
	}
}
