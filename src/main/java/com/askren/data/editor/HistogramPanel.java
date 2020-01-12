package com.askren.data.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;

import javax.swing.JPanel;

import org.HdrHistogram.AbstractHistogram.Percentiles;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;
import org.HdrHistogram.PercentileIterator;

public class HistogramPanel extends JPanel {
	
	protected Histogram[] histograms;
	protected int histogramWidth;
	protected int histogramHeight;
	protected double maxValue;
	
	public HistogramPanel(Histogram[] hist, double maxValue) {
		this.histograms = hist;
		this.histogramWidth = 570;
		this.histogramHeight = 570;
		this.maxValue = maxValue;
		setPreferredSize(new Dimension(histogramWidth, histogramHeight));
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (g instanceof Graphics2D) {
			((Graphics2D) g).setBackground(Color.white);
		}
		g.clearRect(0, 0, getWidth(), getHeight());
		// int[] percentiles = new int[histogramWidth];
		if (histograms == null) {
			return;
		}
//		double max = -1;
		
		for (Histogram hist: histograms) {
			g.setColor(Color.blue);
			int previousY = -1;
			
			Percentiles percentiles = hist.percentiles(25);
			
			Iterator<HistogramIterationValue > iter = percentiles.iterator();
			int i=0;
			while (iter.hasNext()) {
				HistogramIterationValue val = iter.next();
				double percentile = val.getPercentile();
				long value = val.getValueIteratedTo();
	//			System.out.println((int) (histogramHeight - (count / maxValue)
	//					* histogramHeight));
//				val.
				int y = (int) (histogramHeight - (value / maxValue) * histogramHeight);
				if (i > 0) {
					g.drawLine(i-1, previousY, i, y);
				}
				previousY = y;
				i++;
			}
			System.out.println("Created " + i + " values");
		}
	}
}
