package com.askren.data.editor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.compressedlists.CompressedList;
import com.compressedlists.DataTable;
import com.compressedlists.DataType;
import com.compressedlists.IUpdatable;
import com.compressedlists.StringList;
import com.compressedlists.TextList;
import com.compressedlists.impl.IntListImpl;
import com.compressedlists.impl.StringListImpl;
import com.compressedlists.impl.TextListImpl;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FileUtils {

	//	java.lang.*, javax.*, java.net.*
	//	sun.*, sunw.*, com.sun.*


	public static DataTable readFile(File[] files, Properties props, IUpdatable updateable) throws IOException {
		DataTable dataTable = null;
		List<CompressedList> columns = new ArrayList<>();
		List<DataType> columntypes = new ArrayList<>();

		long start = System.currentTimeMillis();
		int count = 0;
		long timeParsing = 0;
		List<String> header = new ArrayList<>();
		Object2IntOpenHashMap<String> headerMap = new Object2IntOpenHashMap<>();
		long totalFileSize = 0l;
		long totalBytesRead = 0l;
		if (updateable != null) {
			for (File file: files) {
				totalFileSize += file.length();
			}
			updateable.setMax(totalFileSize);
			updateable.updateProgress(0, "");
		}
		
		for (File file: files) {
			long beginRead = System.currentTimeMillis();
			char delimiter = ',';
			if (file.getName().endsWith(".tsv") || file.getName().endsWith(".txt")) {
				delimiter = '\t';
			} else if (file.getName().endsWith(".csv")) {
				delimiter = ',';
			} else {
				delimiter = '|';
			}
			Object2IntOpenHashMap<String> currentFileHeader = new Object2IntOpenHashMap<>();
			//		else if (file.getName().endsWith("")){
			//			delimiter = (char)31;
			//		}

			//		try (CsvNioReader csvReader = new CsvNioReader(file, "UTF-8", delimiter)) {
			try (FileReader reader = new FileReader(file);
					SizeableCsvReader csvReader = new SizeableCsvReader(reader, delimiter, '"')) {
				String[] tmpHeader = csvReader.readNext();
				while (tmpHeader[0].startsWith("#")) {
					tmpHeader = csvReader.readNext();
				}
				for (int i=0; i <tmpHeader.length; i++) {
					if (!headerMap.containsKey(tmpHeader[i])) {
						headerMap.put(tmpHeader[i], header.size());
						header.add(tmpHeader[i]);
						currentFileHeader.put(tmpHeader[i], currentFileHeader.size());
					} else {
						currentFileHeader.put(tmpHeader[i], currentFileHeader.size());
					}
				}

//				columntypes = new DataType[header.getsize()];
//				System.out.println(Arrays.toString(header));
				
				if (tmpHeader != null) {
					
					for (int i=0; i< header.size(); i++) {
						String key = tmpHeader[i] + "." + "type"; 
						String type = props.getProperty(key);
						int columnIndex = headerMap.getInt(header.get(i));
						// TODO Am I okay adding the column, or do I need to set the column at the position
						if ( columnIndex >= columns.size()) {  
							if ( type == null || type.length() == 0) {
								columns.add(new StringListImpl());
							} else {
								DataType columnType = DataType.valueOf(type);
								switch (columnType) {
								case INT:
									columns.add(new IntListImpl());
									break;
	
								default:
									columns.add(new StringListImpl());
									break;
								}
							}
						}
					}

					String[] nextLine = null;
					
					if (updateable != null) {
						updateable.updateProgress(totalBytesRead + csvReader.getBytesRead()*2, "");
					}
					
					while ((nextLine = csvReader.readNext()) != null) {
						if (updateable != null) {
							updateable.updateProgress(totalBytesRead + csvReader.getBytesRead()*2, "");
						}
						//					if (/*csvReader.getBytesRead()/file.length() > .75 && */count%10000 == 0) { 
						//						System.out.println(count + ") " + Arrays.toString(nextLine));
						//					}
						long before = System.nanoTime();
						
						for (int i=0; i< tmpHeader.length; i++) {
							int columnIndex = -1;
							if (currentFileHeader.containsKey(tmpHeader[i])) {
								columnIndex = headerMap.getInt(tmpHeader[i]);
							} else {
								// This should never happen
								System.out.println("This should never happen");
							}
							
							if (nextLine.length > i/*columnIndex>=0*/) {
								columns.get(columnIndex).addValue(nextLine[i]);
							} else {
								// TODO what to do for integer columns?
								// This row has fewer columns than the header
								columns.get(columnIndex).addValue("");
							}

							if (columns.get(columnIndex).hasMaxUniqueValues()) {
								switch (columns.get(columnIndex).getDataType()) {
								case STRING:
									TextList col = (TextList) columns.get(columnIndex);
									// Convert to Compressed column
									columns.set(columnIndex, new TextListImpl(col));
									break;

								default:
									break;
								}

							}
						}
						timeParsing += (System.nanoTime() - before);
						count++;
						//					System.out.println(count);
					}
					totalBytesRead += csvReader.getBytesRead()*2;
				}
			}
			System.out.println("Read " + file + " in " +(System.currentTimeMillis() - beginRead)/60000.0 + " min");
		}
		dataTable = new DataTable(columns.toArray(new CompressedList[0]), header.toArray(new String[0]), header.toArray(new String[0]));
		if (updateable != null) {
			updateable.finish();
		}
		//			System.out.println("Reading data: "  + csvReader.getTimeReading()/1000000.0/1000.0 + " seconds");
		//			System.out.println("Parsing data: "  + csvReader.getTimeParsing()/1000000.0/1000.0 + " seconds");
		//			System.out.println("Copy data between Arrays: "  + csvReader.getArrayCopyTime()/1000000.0/1000.0 + " seconds");
		//			csvReader.printTimes();
		System.out.println("Adding To Data Structures: "  + timeParsing/1000000/1000.0 + " seconds");
		for (int i=0; i < columns.size(); i++) {
			CompressedList col = columns.get(i);
			if (col instanceof StringList) {
				System.out.println(header.get(i) + ": " + col.getDataType() + ": " +
						((StringList)col).getUniqueValuesSize() + ": "+ 
						col.getTimeProcessed()/1000.0 + " seconds");
			} else {
				System.out.println(header.get(i) + ": " + col.getDataType() + ": -- : "+ 
						col.getTimeProcessed()/1000.0 + " seconds");
			}
		}
		
		System.gc();
		System.out.println("Loaded " + count + " rows in "+ (System.currentTimeMillis()-start)/1000.0 + " s");

		return dataTable;
	}
}
