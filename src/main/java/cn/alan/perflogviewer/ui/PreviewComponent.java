package cn.alan.perflogviewer.ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import cn.alan.perflogviewer.datamodel.Counter;
import cn.alan.perflogviewer.datamodel.CounterValue;
import cn.alan.perflogviewer.util.FilterManager;

public class PreviewComponent extends JComponent {

	private static final long serialVersionUID = 1L;

	private static final Color COLOR_SHALLOW_GREEN = new Color(180, 255, 180);
	private static final Color COLOR_BACKGROUND = new Color(250, 250, 250);
	private static final int PREVIEW_SAMPLES = 200;

	private Counter counter = null;

	private FilterManager filterManager;
	private PLV plv;

	private double filterLeftPos;
	private double filterRightPos;

	private int border_offset;

	private int samples = PREVIEW_SAMPLES;

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
				if (resizing) {
					resizing = false;				
					updateFilterInManager();
				}
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
							if (pos > filterRightPos - margin) {
								filterLeftPos = filterRightPos - margin;
							} else {
								filterLeftPos = pos;
							}
							
							if (filterLeftPos < 0) {
								filterLeftPos = 0;
							}
							repaint();
						}

						break;

					case Cursor.E_RESIZE_CURSOR:

						if (pos!= filterRightPos) {
							if (pos < filterLeftPos + margin) {
								filterRightPos = filterLeftPos + margin;
							} else {
								filterRightPos = pos;
							}
							if (filterRightPos > 1) {
								filterRightPos = 1;
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
		if (redraw)
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
			if (filterRightPos - filterLeftPos < 0.005) {
				/*reset filter if gap is too small*/
				filterLeftPos = 0;
				filterRightPos = 1;		
				updateFilterInManager();
			}
		} else {
			filterLeftPos = 0;
			filterRightPos = 1;
		}
		repaint();
	}

	public void updateFilterInManager() {
		//long globalStartTime = filterManager.getGlobalStartTime().getTime();
		//long globalEndTime = filterManager.getGlobalEndTime().getTime();		
		//filterManager.setFilterStartTime((long)(filterLeftPos * (globalEndTime - globalStartTime) + globalStartTime));
		//filterManager.setFilterEndTime((long)(filterRightPos * (globalEndTime - globalStartTime) + globalStartTime));	
		long leftTime = convertPosToTime(filterLeftPos, 0);
		if (filterLeftPos > 0.005)
			leftTime++;
		long rightTime = convertPosToTime(filterRightPos, 0);		

		filterManager.setFilterStartTime(leftTime);
		filterManager.setFilterEndTime(rightTime);
		plv.filterChanged(false);
	}

	public void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D)g.create();

		g2d.setColor(COLOR_BACKGROUND);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, getWidth(), getHeight());		

		int chartWidth = getWidth() - border_offset * 2;
		int chartHeight =  getHeight() - border_offset * 2;

		if (counter != null) {

			long st = filterManager.getGlobalStartTime().getTime();
			long et = filterManager.getGlobalEndTime().getTime();

			g2d.setColor(COLOR_SHALLOW_GREEN);
			
			g2d.fillRect((int)(filterLeftPos * chartWidth + border_offset), 
					border_offset, 
					(int)((filterRightPos - filterLeftPos) * chartWidth), 
					chartHeight);

			double maxValue = Double.NEGATIVE_INFINITY;
			double minValue = Double.POSITIVE_INFINITY;

			double avg[] = new double[PREVIEW_SAMPLES];
			long time[] = new long[PREVIEW_SAMPLES];

			samples = 0;
			long t1 = st;


			for (int i = 0; i < PREVIEW_SAMPLES; ++i) {			
				long t2 = ((i + 1) * et + (PREVIEW_SAMPLES - 1 - i) * st) / PREVIEW_SAMPLES;
				if (t1 > t2)
					t2 = t1;				
				double average = CounterValue.getAverage(counter.getData(new Date((long)t1), new Date((long)t2)));
				long averageTime = (long)((t1 + t2) / 2.0 + 0.5);

				if (!Double.isNaN(average) && (samples == 0 || time[samples - 1] != averageTime)) {					

					avg[samples] = average;
					time[samples] = averageTime;

					if (maxValue < average) {
						maxValue = average;
					}

					if (minValue > average) {
						minValue = average;
					}

					++samples;					
				}

				t1 = t2 + 1;
				if (t1 > et)
					t1 = et;
			}

			if (Double.isInfinite(maxValue) || Double.isInfinite(minValue))
				return;

			if (maxValue == minValue)
				minValue = maxValue - 1;

			double ys = (maxValue - minValue) / chartHeight / 0.8;

			int x[] = new int[PREVIEW_SAMPLES];
			int y[] = new int[PREVIEW_SAMPLES];

			for (int i = 0; i < samples; ++i) {				
				x[i] = convertTimeToPos(time[i]);
				y[i] = (int)(chartHeight * 0.95 - ((avg[i] - minValue) / ys)) + border_offset;				
			}

			g2d.setColor(Color.BLUE);
			g2d.drawPolyline(x, y, samples);
		}

		g2d.dispose();
	}

	private long convertPosToTime(double x, double margin) {		

		long st = filterManager.getGlobalStartTime().getTime();
		long et = filterManager.getGlobalEndTime().getTime();

		if (et == st)
			return st;

		return (long)((x + margin) * (et - st) + st); 

		//int n = (int)((x - border_offset) * PREVIEW_SAMPLES / (getWidth() - border_offset * 2) + 0.5);

		//return (n * et  + (PREVIEW_SAMPLES - n) * st) / PREVIEW_SAMPLES;		
	}

	private int convertTimeToPos(long time) {
		long st = filterManager.getGlobalStartTime().getTime();
		long et = filterManager.getGlobalEndTime().getTime();

		if (st == et) {
			return getWidth() / 2;
		}

		return (int)((getWidth() - border_offset * 2) * (time - st) / (et - st) + border_offset);
		//return (int)((time - st) * grid + border_offset);
	}




}
