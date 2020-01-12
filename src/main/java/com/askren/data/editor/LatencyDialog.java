package com.askren.data.editor;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;

import com.compressedlists.CompressedList;
import com.compressedlists.DataTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;

import java.awt.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class LatencyDialog extends JDialog {

	public LatencyDialog(JFrame parentFrame, DataTable dataTable) {
		super(parentFrame, "Latency");
		CompressedList[] columns = dataTable.getColumns();
		String[] uniqueNames = dataTable.getUniqueNames();
		GridBagLayout gridBagLayout = new GridBagLayout();
		getContentPane().setLayout(new BorderLayout());

		JPanel contentPane = new JPanel(gridBagLayout);
		JScrollPane scrollPane = new JScrollPane(contentPane);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		double maxValue = -1;
		for (CompressedList col: columns) {
			Histogram hist = col.getHistogram();
//			Iterator<HistogramIterationValue> iter = hist.percentiles(25).iterator();
//			while (iter.hasNext()) {
//				HistogramIterationValue value = iter.next();
//
//				if (value.getValueIteratedTo() > maxValue) {
//					//					maxValue = hist.getValueAtPercentile(.9999);
//					maxValue = value.getValueIteratedTo();
//				}
//			}
			if (hist.getMaxValueAsDouble() > maxValue) {
				maxValue = hist.getMaxValueAsDouble();
			}
		}

		System.out.println("Max: " + maxValue);
		maxValue *= 1.2;
		for (int i=0; i<columns.length; i+=2) {
			CompressedList col = columns[i];
			JLabel colNameLabel = new JLabel(uniqueNames[i]);
			GridBagConstraints gbc_colNameLabel = new GridBagConstraints();
			gbc_colNameLabel.insets = new Insets(10, 0, 5, 10);
			gbc_colNameLabel.gridx = 1;
			gbc_colNameLabel.gridy = i;
			contentPane.add(colNameLabel, gbc_colNameLabel);

			PrintStream printStream;
			try {
				printStream = new PrintStream(new File("/home/jay/" + uniqueNames[i] + ".txt"));
				col.getHistogram().outputPercentileDistribution(printStream, .5);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			HistogramPanel histogramPanel = new HistogramPanel(new Histogram[]{col.getHistogram()}, maxValue);
			GridBagConstraints gbc_histogramPanel = new GridBagConstraints();
			gbc_histogramPanel.insets = new Insets(10, 0, 5, 0);
			gbc_histogramPanel.fill = GridBagConstraints.BOTH;
			gbc_histogramPanel.gridx = 1;
			gbc_histogramPanel.gridy = i+1;
			gbc_histogramPanel.weighty = 1;
			contentPane.add(histogramPanel, gbc_histogramPanel);

			if (i+1 < columns.length) {
				CompressedList col2 = columns[i+1];
				JLabel colNameLabel2 = new JLabel(uniqueNames[i+1]);
				GridBagConstraints gbc_colNameLabel2 = new GridBagConstraints();
				gbc_colNameLabel2.insets = new Insets(10, 0, 5, 10);
				gbc_colNameLabel2.gridx = 2;
				gbc_colNameLabel2.gridy = i;
				contentPane.add(colNameLabel2, gbc_colNameLabel2);

				HistogramPanel histogramPanel2 = new HistogramPanel(new Histogram[]{col2.getHistogram()}, maxValue);
				GridBagConstraints gbc_histogramPanel2 = new GridBagConstraints();
				gbc_histogramPanel2.insets = new Insets(10, 10, 5, 0);
				gbc_histogramPanel2.fill = GridBagConstraints.BOTH;
				gbc_histogramPanel2.gridx = 2;
				gbc_histogramPanel2.gridy = i+1;
				gbc_histogramPanel2.weighty = 1;
				contentPane.add(histogramPanel2, gbc_histogramPanel2);
			}
		}
		scrollPane.setPreferredSize(new Dimension(500, 600));
		pack();
	}
}
