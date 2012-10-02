package cn.alan.perflogviewer.viewer;

import java.util.Date;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import cn.alan.perflogviewer.datamodel.Counter;
import cn.alan.perflogviewer.datamodel.CounterValue;

public class GraphViewer extends Canvas {
	
	private Menu popUpMenu;
	private MenuItem markStartMenuItem;
	private MenuItem markEndMenuItem;
	private MenuItem clearMenuItem;
	
	private int rightClickX;
	private int rightClickY;

	public GraphViewer(Composite parent) {
		super(parent, SWT.NONE);
		
		setupMenu();	
		
		addListener(SWT.MouseDown, new Listener() {	

			@Override
			public void handleEvent(Event event) {
				if (event.button == 3) {
					rightClickX = event.x;
					rightClickY = event.y;
				}		
			}
			
		});
	    
	    
		
		addPaintListener(new CanvasPaintListener());
		
	}
	
	class CanvasPaintListener implements PaintListener {
	
		@Override
		public void paintControl(PaintEvent e) {
			
			Canvas canvas = (Canvas)e.widget;
			Rectangle rec = canvas.getClientArea();
			
			chartLeftTopX = rec.width / 10;
			chartLeftTopY = rec.height / 10;
			chartWidth = rec.width * 8 / 10;
			chartHeight = rec.height * 8 / 10;
			
			GC gc = e.gc;
			//gc.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
			gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(0, 0, rec.width, rec.height);			
			
			drawAxis(e);
			
			if (counter != null) {
				double maxValue = CounterValue.getMax(counter.getData(startTime, endTime)).getDoubleValue();
				double minValue = CounterValue.getMin(counter.getData(startTime, endTime)).getDoubleValue();
				
				if (Double.isInfinite(maxValue) || Double.isInfinite(minValue))
					return;
				
				double x[] = new double[200];
				double y[] = new double[200];
				
				double ys = 1;
				
				while (maxValue / ys < 1)
					ys /= 10;
				
				while (maxValue / ys > 10)
					ys *= 10;
							
				long st = startTime.getTime();
				long et = endTime.getTime();
				
				for (int i = 0; i < 200; ++i) {
					long t1 = (i * et  + (200 - i) * st) / 200;
					
					long t2 = ((i + 1) * et + (199 - i) * st) / 200;
					double averageValue = CounterValue.getAverage(counter.getData(new Date(t1), new Date(t2))).getDoubleValue();
					x[i] = i + 0.5;
					y[i] = averageValue;
				}
							
				drawDataLine(e, e.display.getSystemColor(SWT.COLOR_BLUE),			
					x, y, chartWidth / 200.0, chartHeight / 10.0);
			}
			
			
		}
		
		private void drawAxis(PaintEvent e) {
			GC gc = e.gc;									
			
			gc.setLineWidth(1);
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
			gc.setLineStyle(SWT.LINE_DASH);
			
			if (startLineX > 0) {
				int x = (int)(startLineX * chartWidth) + chartLeftTopX;
				gc.drawLine(x, chartLeftTopY, x, chartLeftTopY + chartHeight);				
			}
			
			if (endLineX > 0) {
				int x = (int)(endLineX * chartWidth) + chartLeftTopX;
				gc.drawLine(x, chartLeftTopY, x, chartLeftTopY + chartHeight);
			}
			
			gc.setLineWidth(1);
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
			gc.setLineStyle(SWT.LINE_SOLID);
			
			for (int i = 1; i < 10; ++i) {
				gc.drawLine(chartLeftTopX, chartLeftTopY + chartHeight * i / 10, chartLeftTopX + chartWidth, chartLeftTopY + chartHeight * i / 10);
			}
			
			gc.setLineWidth(2);
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			gc.drawRectangle(chartLeftTopX, chartLeftTopY, chartWidth, chartHeight);
			
			
			
		}
		
		private void drawDataLine(PaintEvent e, Color color, double x[], double y[], double scaleX, double scaleY) {
			GC gc = e.gc;
			
			gc.setLineWidth(2);
			gc.setForeground(color);
			gc.setBackground(color);
			
			double ys = 1;
			double maxValue = 0;
			
			for (int k = 0; k < y.length; ++k) {
				if (!(Double.isNaN(y[k]) || Double.isInfinite(y[k])))
					if (maxValue < y[k])
							maxValue = y[k];
			}
			
			if (maxValue != 0) {
			
			while (maxValue / ys < 1)
				ys /= 10;
			
			while (maxValue / ys > 10)
				ys *= 10;
			}
			
			int i = 0;
			
			
			
			while (i < x.length && (Double.isInfinite(y[i]) || Double.isNaN(y[i])))
				++i;
			
			int x1 = 0, y1 = 0, x2, y2;
			
			if (i < x.length) {
				x1 = (int)(x[i] * scaleX + chartLeftTopX);
				y1 = (int)(-y[i] / ys * scaleY + chartLeftTopY + chartHeight);
				gc.fillRectangle(x1 - 2, y1 - 2, 4, 4);
			}
			
			for (int j = i + 1; j < x.length; ++j) {
				if (!(Double.isInfinite(y[j]) || Double.isNaN(y[j]))) {					
					x2 = (int)(x[j] * scaleX + chartLeftTopX);
					y2 = (int)(-y[j] / ys * scaleY + chartLeftTopY + chartHeight);				
					gc.drawLine(x1, y1, x2, y2);					
					gc.fillRectangle(x2 - 2, y2 - 2, 4, 4);
					x1 = x2;
					y1 = y2;
				}				
			}			
		}
	}	
	
	private void setupMenu() {
		popUpMenu = new Menu(getShell(), SWT.POP_UP);
		markStartMenuItem = new MenuItem(popUpMenu, SWT.PUSH);
		markStartMenuItem.setText("Set Start Time");
			
		markEndMenuItem = new MenuItem(popUpMenu, SWT.PUSH);
		markEndMenuItem.setText("Set End Time");
	
		setMenu(popUpMenu);
	
		popUpMenu.addMenuListener  (new MenuListener() {		

			@Override
			public void menuHidden(MenuEvent e) {
				// TODO Auto-generated method stub				
			}
	
			@Override
			public void menuShown(MenuEvent e) {
				if (rightClickX < chartLeftTopX || rightClickX > chartLeftTopX + chartWidth || rightClickY < chartLeftTopY || rightClickY > chartLeftTopY + chartHeight)
					markStartMenuItem.setEnabled(false);
				else
					markStartMenuItem.setEnabled(true);
				
				if (startLineX < 0 || rightClickX < startLineX || rightClickX > chartLeftTopX + chartWidth || rightClickY < chartLeftTopY || rightClickY > chartLeftTopY + chartHeight)
					markEndMenuItem.setEnabled(false);
				else
					markEndMenuItem.setEnabled(true);					
			}
			
		});
		
			
		markStartMenuItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {				
				startLineX = (rightClickX - chartLeftTopX) / (double)chartWidth;								
				redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		markEndMenuItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				endLineX = (rightClickX - chartLeftTopX) / (double)chartWidth;
				
				redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		clearMenuItem = new MenuItem(popUpMenu, SWT.PUSH);
		clearMenuItem.setText("Clear");
		clearMenuItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				resetFilterLine();
				redraw();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	public void resetFilterLine() {
		startLineX = 0;
		endLineX = 1;
		redraw();
	}
	
	public void setCounter(Counter counter) {
		this.counter = counter;
		this.redraw();
	}

	private int chartLeftTopX;
	private int chartLeftTopY;
	private int chartWidth;
	private int chartHeight;
	
	private double startLineX = 0;
	private double endLineX = 1;
	
	private Counter counter;
	private Date startTime;
	private Date endTime;
	
	public void setStartTime(Date startTime) {
		this.startTime = (Date)startTime.clone();
		resetFilterLine();
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = (Date)endTime.clone();
		resetFilterLine();
	}
	
	public Date getStartLineDate() {		
		if (startLineX < 0 || startLineX > 1)
			return null;
		else {
			long st = startTime.getTime();
			long et = endTime.getTime();
			long t1 = (long)(startLineX * et + (1 - startLineX) * st);
			return new Date(t1);
		}			
	}
	
	public Date getEndLineDate() {
		if (endLineX < 0 || endLineX > 1)
			return null;
		else {
			long st = startTime.getTime();
			long et = endTime.getTime();
			long t1 = (long)(endLineX * et + (1 - endLineX) * st);
			return new Date(t1);
		}			
	}
	
	
	
	
}