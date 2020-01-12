package com.askren.data.performance;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Hello world!
 *
 */
public class OrcBenchmark {

    public static void main( String[] args ) {
    	File csvFile = new File(args[0]);
    	File outFile = new File(args[1]);
    	String[] header = null;
    	try {
			header = writeOrcFile(csvFile, outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	try {
			System.out.println("Hash = " + readOrcFile(header.length, outFile.getAbsolutePath()));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    public static String[] writeOrcFile(File inFile, File outFile) throws IOException {
    	long begin = System.currentTimeMillis();
    	
    	CSVReader reader = new CSVReader(new FileReader(inFile));
    	String[] headerRow = reader.readNext();
    	
    	Configuration conf = new Configuration();
    	
    	
		TypeDescription schema = TypeDescription.createStruct();
		for(int i=0; i < headerRow.length; i++) {
			schema = schema.addField(headerRow[i], TypeDescription.createString());
		}
		
		outFile.delete();
		Writer writer = OrcFile.createWriter(new Path(outFile.getAbsolutePath()),
				OrcFile.writerOptions(conf)
						.setSchema(schema).stripeSize(65536).bufferSize(1024*32).compress(CompressionKind.NONE));


		VectorizedRowBatch batch = schema.createRowBatch();
		BytesColumnVector[] columnVectors = new BytesColumnVector[headerRow.length];
		for (int i=0; i < headerRow.length ; i++) {
			columnVectors[i] = (BytesColumnVector) batch.cols[i];
		}

		String[] row;
		while ((row = reader.readNext()) != null) {
			int rowNum = batch.size++;
			for (int i=0; i<row.length; i ++) {
				columnVectors[i].setVal(rowNum, row[i].getBytes());
			}
			if (batch.size == batch.getMaxSize()) {
				writer.addRowBatch(batch);
				batch.reset();
			}
		}
		if (batch.size != 0) {
			writer.addRowBatch(batch);
			batch.reset();
		}
		writer.close();
		System.out.println("Wrote Orc file in " + (System.currentTimeMillis()-begin)/1000.0 + " ms");
		return headerRow;
    }
   
    static long readOrcFile(int numColumns, String orcInput) throws IllegalArgumentException, IOException {
    	long begin = System.currentTimeMillis();
		Configuration conf = new Configuration();
		org.apache.orc.Reader reader = OrcFile.createReader(new Path(orcInput),
				OrcFile.readerOptions(conf));
		int numBatches = 0;
		int numRows = 0;
		RecordReader rows = reader.rows();
		VectorizedRowBatch batch = reader.getSchema().createRowBatch();
		BytesColumnVector[] columnVectors = new BytesColumnVector[numColumns];
		for (int i=0; i < numColumns ; i++) {
			columnVectors[i] = (BytesColumnVector) batch.cols[i];
		}
		
		long total = 0l;
		while (rows.nextBatch(batch)) {
			numBatches++;
			for(int r=0; r < batch.size; r++) {
				numRows++;
				for (int i=0; i<batch.numCols; i ++) {
					String stringValue = new String(columnVectors[i].toString(r));
					if ( stringValue != null) {
						total+=stringValue.hashCode();
					}
				}
				
			}
		}
		rows.close();
		System.out.println("Read Orc file with " + numBatches + " batches and " + numRows + " rows in " + (System.currentTimeMillis()-begin)/1000.0 + " s");
		return total;
	}
    
}
