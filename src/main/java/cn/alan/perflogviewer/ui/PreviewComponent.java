package cn.alan.perflogviewer.ui;

import javax.swing.*;

import org.eclipse.swt.SWT;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import cn.alan.perflogviewer.datamodel.Counter;
import cn.alan.perflogviewer.datamodel.CounterValue;
import cn.alan.perflogviewer.util.FilterManager;

public class PreviewComponent extends JComponent {

	private static final long serialVersionUID = 1L;

	private static final Color COLOR_SHALLOW_GREEN = new Color(160, 255, 160);
	private static final Color COLOR_BACKGROUND = new Color(250, 250, 250);
	private static final int PREVIEW_SAMPLES = 200;

	private Counter counter = null;

	//private long globalStartTime;
	//private long globalEndTime;
	//private long filterStartTime;
	//private long filterEndTime;

	private FilterManager filterManager;
	private PLV plv;

	private double filterLeftPos;
	private double filterRightPos;

	private int border_offset;

	public PreviewComponent(FilterManager fm, PLV plv) {
		super();
		this.filterManager = fm;
		this.plv = plv;
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.BLACK)));
		border_offset = 5;
		updateFilterPosition();

		MouseAdapter filterResizer = new MouseAdapter() {

			private final int PROX_DIST = 3;

			private boolean  resizing = false;

			public void mousePressed(MouseEvent e) {
				if(getCursor() != Cursor.getDefaultCursor()) {
					resizing = true; 
				}
			}  

			public void mouseReleased(MouseEvent e) {        
				resizing = false;
				updateFilterInManager();
			}

			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();

				double leftIntPos = (filterLeftPos * (getWidth() - border_offset * 2)) + border_offset;
				double rightIntPos = (filterRightPos * (getWidth() - border_offset * 2)) + border_offset;

				//double pos = ((double)(p.getX() - border_offset)) / (getWidth() - border_offset * 2);
				if (p.getX() >= leftIntPos - PROX_DIST && p.getX() <= leftIntPos + PROX_DIST) {
					setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
				} else if (p.getX() >= rightIntPos - PROX_DIST && p.getX() <= rightIntPos + PROX_DIST) {
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else if(getCursor() != Cursor.getDefaultCursor()) {						  
					setCursor(Cursor.getDefaultCursor());
				}
			}

			public void mouseDragged(MouseEvent e) {     
				if(resizing) {           
					Point p = e.getPoint(); 
					int type = getCursor().getType();					
					double pos = (p.getX() - border_offset) / (getWidth() - 2 * border_offset);
					double margin = (double)(PROX_DIST) / (getWidth() - 2 * border_offset);					
					switch(type) {
					case Cursor.W_RESIZE_CURSOR:
						if (pos != filterLeftPos) {
							if (pos < 0) {
								filterLeftPos = 0;
							} else if (pos > filterRightPos - margin) {
								filterLeftPos = filterRightPos - margin;
							} else {
								filterLeftPos = pos;
							}
							repaint();
						}
						break;
					case Cursor.E_RESIZE_CURSOR:
						if (pos!= filterRightPos) {
							if (pos > 1) {
								filterRightPos = 1;
							} else if (pos < filterLeftPos + margin) {
								filterRightPos = filterLeftPos + margin;
							} else {
								filterRightPos = pos;
							}
							repaint();
						}
						break;	
					}
				}
			}

		};

		addMouseListener(filterResizer);
		addMouseMotionListener(filterResizer);
	}

	public void setCounter(Counter counter) {
		setCounter(counter, true);		
	}

	public void setCounter(Counter counter, boolean redraw) {
		this.counter = counter;
		repaint();
	}

	public void updateFilterPosition() {
		long globalStartTime = filterManager.getGlobalStartTime().getTime();
		long globalEndTime = filterManager.getGlobalEndTime().getTime();
		long filterStartTime = filterManager.getFilterStartTime().getTime();
		long filterEndTime = filterManager.getFilterEndTime().getTime();

		if (filterManager.isValid()) {
			filterLeftPos = ((double)(filterStartTime - globalStartTime)) / (globalEndTime - globalStartTime);
			filterRightPos = ((double)(filterEndTime - globalStartTime)) / (globalEndTime - globalStartTime);			
		} else {
			filterLeftPos = 0;
			filterRightPos = 1;
		}
	}

	public void updateFilterInManager() {
		long globalStartTime = filterManager.getGlobalStartTime().getTime();
		long globalEndTime = filterManager.getGlobalEndTime().getTime();		
		filterManager.setFilterStartTime((long)(filterLeftPos * (globalEndTime - globalStartTime) + globalStartTime));
		filterManager.setFilterEndTime((long)(filterRightPos * (globalEndTime - globalStartTime) + globalStartTime));	
		plv.filterChanged();
	}

	public void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D)g.create();
		//g2d.setColor(Color.WHITE);
		g2d.setColor(COLOR_BACKGROUND);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, getWidth(), getHeight());		

		int chartWidth = getWidth() - border_offset * 2;
		int chartHeight =  getHeight() - border_offset * 2;

		if (counter != null) {


			g2d.setColor(COLOR_SHALLOW_GREEN);			
			g2d.fillRect((int)(filterLeftPos * chartWidth + border_offset), 
					border_offset, 
					(int)((filterRightPos - filterLeftPos) * chartWidth), 
					chartHeight);

			double maxValue = Double.NEGATIVE_INFINITY;
			double minValue = Double.POSITIVE_INFINITY;			

			double avg[] = new double [PREVIEW_SAMPLES];						

			long st = filterManager.getGlobalStartTime().getTime();
			long et = filterManager.getGlobalEndTime().getTime();

			for (int i = 0; i < PREVIEW_SAMPLES; ++i) {
				long t1 = (i * et  + (PREVIEW_SAMPLES - i) * st) / PREVIEW_SAMPLES;

				long t2 = ((i + 1) * et + (PREVIEW_SAMPLES - 1 - i) * st) / PREVIEW_SAMPLES;
				avg[i] = CounterValue.getAverage(counter.getData(new Date(t1), new Date(t2)));

				if (maxValue < avg[i]) {
					maxValue = avg[i];
				}

				if (minValue > avg[i]) {
					minValue = avg[i];
				}
			}

			if (Double.isInfinite(maxValue) || Double.isInfinite(minValue))
				return;

			double ys = (maxValue - minValue) / chartHeight / 0.8;

			int x[] = new int[PREVIEW_SAMPLES];
			int y[] = new int[PREVIEW_SAMPLES];

			for (int i = 0; i < PREVIEW_SAMPLES; ++i) {
				x[i] = (int)((i + 0.5) * chartWidth / PREVIEW_SAMPLES) + border_offset;
				y[i] = (int)(chartHeight * 0.95 - ((avg[i] - minValue) / ys)) + border_offset;				 
			}

			g2d.setColor(Color.BLUE);
			g2d.drawPolyline(x, y, PREVIEW_SAMPLES);
		}

		g2d.dispose();
	}	


}
