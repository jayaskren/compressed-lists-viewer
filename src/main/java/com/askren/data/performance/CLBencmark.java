package com.askren.data.performance;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.compressedlists.CompressionType;
import com.compressedlists.DataTable;

public class CLBencmark {
	
	public static void main(String[] args) {
		String inputCsvFile = args[0];
		String outputFolder = args[1];
		try {
			writeData(new File(inputCsvFile), outputFolder);
			readDat(outputFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeData(File inFile, String outFolder) throws IOException {
		long begin = System.currentTimeMillis();
		DataTable table = 
				com.compressedlists.FileUtils.readFile(inFile, new Properties());
		table.writeData(new File(outFolder), CompressionType.DEFAULT);
		System.out.println("Wrote Data in " + (System.currentTimeMillis() - begin)/1000.0 + " s");
	}
	
	public static void readDat(String folder) {
		long hash = 0l;
		long begin = System.currentTimeMillis();
		DataTable table;
		try {
			table = DataTable.readData(new File(folder), null);
			System.out.println("Loaded table with " +  
					table.getNumRows() + " rows and " + table.getNumColumns() + " columns in "  + 
					(System.currentTimeMillis() - begin)/1000.0 + " s");
			for (int i=0; i < table.getNumRows(); i++) {
				for (int j=0; j < table.getNumColumns(); j++) {
					hash += table.getColumns()[j].getValueDisplay(i).hashCode();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Loaded and read table in "  + 
				(System.currentTimeMillis() - begin)/1000.0 + " s");
		System.out.println("Hash = " + hash);
	}
}
