package com.askren.data.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class DataEditorFrame extends JFrame {
	DataTablePanel dataTable;
	int fontSize = 18;
	static final String FONT_NAME = "Consolas";
	java.net.URL url = ClassLoader.getSystemResource("com/askren/data/editor/DataEditor.png");
	
	public DataEditorFrame(String title) {
		super(title);
		init();
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		setIconImage(img);
	}
	
	public void init() {
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenu viewMenu = new JMenu("View");
		JMenu help = new JMenu("Help");

		JMenuItem zoomInMenuItem = new JMenuItem("Zoom In" /*zoomInAction*/);
		zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		viewMenu.add(zoomInMenuItem);
		JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out" /*zoomOutAction*/);
		zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		viewMenu.add(zoomInMenuItem);
		zoomInMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		    	fontSize += 2;
		    	refreshTable();
			}
			
		});
		
		zoomOutMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		    	fontSize -= 2;
		    	refreshTable();
			}
			
		});
		
		viewMenu.add(zoomOutMenuItem);
		mb.add(menu);
		mb.add(viewMenu);
		mb.add(help);
		JMenuItem openMenuItem = new JMenuItem("Open");
		menu.add(openMenuItem);
		
		JMenuItem importMenuItem = new JMenuItem("Import");
		menu.add(importMenuItem);
		
		dataTable = new DataTablePanel(this);
		dataTable.setFont(new Font(FONT_NAME, Font.PLAIN, fontSize));
		dataTable.invalidate();
		openMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dataTable.openFile();
				refreshTable();
			}

		});
		
		importMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dataTable.importFile();
			}

		});
		
//		JMenuItem openAsMenuItem = new JMenuItem("Open As");
//		menu.add(openAsMenuItem);
//		openAsMenuItem.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				dataTable.openAsFile();
//			}
//
//		});
		
		JMenuItem showMemoryMenuItem = new JMenuItem("Memory");
		showMemoryMenuItem.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				dataTable.showMemoryUsage(fontSize);
			}
			
		});
		help.add(showMemoryMenuItem);
		
		JMenuItem showLatencyMenuItem = new JMenuItem("Latency");
		showLatencyMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dataTable.showLatency();
			}
		});
		
		help.add(showLatencyMenuItem);
		dataTable.setAutoscrolls(false);
		setJMenuBar(mb);
		refreshTable();
	}
	
	void refreshTable() {
		setDefaultSize(fontSize);
    	updateComponent(DataEditorFrame.this, getFontUIResource(fontSize));
    	DataEditorFrame.this.revalidate();
    	getDataTable().packTable();
	}
	
	public DataTablePanel getDataTable() {
		return dataTable;
	}

	
	public static void setDefaultSize(int size) {

        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        Object[] keys = keySet.toArray(new Object[keySet.size()]);

        for (Object key : keys) {

            if (key != null && key.toString().toLowerCase().contains("font")) {

//                System.out.println(key);
                Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    font = font.deriveFont((float)size);
                    UIManager.put(key, font);
                }
            }
        }
        
	}
                    
	
	public void updateComponent(Component c, FontUIResource resource) {
	    if (c == null) {
	        return;
	    }
	    if (c instanceof JComponent) {
	        JComponent jc = (JComponent) c;
	        jc.updateUI();
	        JPopupMenu jpm = jc.getComponentPopupMenu();
	        if (jpm != null) {
	            updateComponent(jpm, resource);
	        }
	    }
	    Component[] children = null;
	    if (c instanceof JMenu) {
	        children = ((JMenu) c).getMenuComponents();
	    }
	    else if (c instanceof Container) {
	        children = ((Container) c).getComponents();
	    }
	    if (children != null) {
	        for (Component child : children) {
	            if (child instanceof Component) {
	                updateComponent(child, resource);
	            }
	        }
	    }
	    Font f = c.getFont();
	    if (f == null) {
	        f = getFontUIResource(fontSize); // default
	    }
	    if (c.getFont() == null) {
	    	c.setFont(new Font(FONT_NAME, Font.PLAIN, fontSize));
	    } else {
	    	c.setFont(resource.deriveFont(c.getFont().getStyle()));
	    }
	    
	}  
	
	private FontUIResource getFontUIResource(int size) {
        return new FontUIResource(new Font(FONT_NAME, Font.PLAIN, size));
    }
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				if (args.length > 0) {
					setDefaultSize(Integer.parseInt(args[0]));
				}
				DataEditorFrame frame = new DataEditorFrame("Data Editor");
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 
		        JLabel emptyLabel = new JLabel("");
		        emptyLabel.setMinimumSize(new Dimension(200, 200));
		        emptyLabel.setPreferredSize(new Dimension(800, 800));
		        frame.getContentPane().add(frame.getDataTable(), BorderLayout.CENTER);
		 
		        //Display the window.
		        frame.pack();
		        frame.setVisible(true);
			}
		});
	}
}
