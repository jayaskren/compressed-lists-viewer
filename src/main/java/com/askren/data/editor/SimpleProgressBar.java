package com.askren.data.editor;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class SimpleProgressBar extends JPanel implements com.compressedlists.IUpdatable {

	double max;
	long currentValue;
	String currentMessage;
	boolean running;
	
	long lastUpdate;
	
	public SimpleProgressBar() {
		lastUpdate = System.currentTimeMillis();
		setOpaque(true);
	}
	
	public void setMax(double max) {
		this.max = max;
	}
	public void updateProgress(long currentValue, String currentMessage) {
		if ((System.currentTimeMillis() - lastUpdate) > 30) {
			this.currentMessage = currentMessage;
			this.currentValue=currentValue;
			running = true;
//			System.out.println(System.currentTimeMillis());
			lastUpdate = System.currentTimeMillis();
			repaint();
		}
		
	}

	public void finish() {
		currentValue = 0l;
		running = false;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Color oldColor = g.getColor();
		
		if (running) {
			g.setColor(Color.blue);
			g.clearRect(0, 1, getWidth(), 4);
			int width =  (int)((getWidth()-10) * currentValue/max);
			int widthSmall =  (int)((getWidth()-12) * currentValue/max);
			
			g.drawLine(6, 1, widthSmall, 1);
			g.drawLine(5, 2, width, 2);
			g.drawLine(5, 3, width, 3);
			g.drawLine(6, 4, widthSmall, 4);
			
			g.setColor(oldColor);
		}
		
	}
	
	
}
