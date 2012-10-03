package cn.alan.perflogviewer.viewer;


import java.util.Date;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import cn.alan.perflogviewer.datamodel.Counter;
import cn.alan.perflogviewer.datamodel.CounterValue;
import cn.alan.perflogviewer.datamodel.PerfLogData;


public class CounterStatsTableViewer extends Composite {

	public static final int COLUMN_COMPUTER	= 1;
	public static final int COLUMN_OBJECT		= 2;
	public static final int COLUMN_COUNTER	= 3;
	public static final int COLUMN_INSTANCE	= 4;
	public static final int COLUMN_SAMPLES	= 5;
	public static final int COLUMN_MINIMUM	= 6;
	public static final int COLUMN_AVERAGE	= 7;
	public static final int COLUMN_MAXIMUM	= 8;

	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;

	private int sortColumn = COLUMN_COMPUTER;
	private int sortDirection = ASCENDING;	

	private CheckboxTableViewer viewer;
	private Date startTime;
	private Date endTime;
	private Date filterStartTime;
	private Date filterEndTime;

	private GraphViewer graphViewer;

	private PerfLogData perfLogData = new PerfLogData();

	private class CounterSelecter implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection =
					(IStructuredSelection) event.getSelection();

			Counter selected_counter = (Counter)selection.getFirstElement();
			graphViewer.setCounter(selected_counter);			        
		}

	}

	private class CounterSorter extends ViewerSorter {	
		public void doSort(int column) {
			if (column == sortColumn) {
				// Same column as last sort; toggle the direction
				sortDirection = 1 - sortDirection;
			} else {
				// New column; do an ascending sort
				sortColumn = column;
				sortDirection = ASCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			int rc = 0;
			Counter c1 = (Counter)e1;
			Counter c2 = (Counter)e2;
			switch (sortColumn) {
			case COLUMN_COMPUTER:
				rc = c1.getComputer().compareTo(c2.getComputer());
				break;
			case COLUMN_OBJECT:
				rc = c1.getObject().compareTo(c2.getObject());
				break;
			case COLUMN_COUNTER:
				rc = c1.getCounter().compareTo(c2.getCounter());
				break;
			case COLUMN_INSTANCE:
				rc = c1.getInstance().compareTo(c2.getInstance());
				break;
			case COLUMN_SAMPLES:				
				rc = c1.getData(filterStartTime, filterEndTime).length - c2.getData(filterStartTime, filterEndTime).length;
				break;
			case COLUMN_MINIMUM:
				double v1 = CounterValue.getMin(c1.getData(filterStartTime, filterEndTime)).getDoubleValue();
				double v2 = CounterValue.getMin(c2.getData(filterStartTime, filterEndTime)).getDoubleValue();
				rc = Double.compare(v1, v2);			
				break;
			case COLUMN_AVERAGE:
				v1 = CounterValue.getAverage(c1.getData(filterStartTime, filterEndTime)).getDoubleValue();
				v2 = CounterValue.getAverage(c2.getData(filterStartTime, filterEndTime)).getDoubleValue();				
				rc = Double.compare(v1, v2);			
				break;								
			case COLUMN_MAXIMUM:
				v1 = CounterValue.getMax(c1.getData(filterStartTime, filterEndTime)).getDoubleValue();
				v2 = CounterValue.getMax(c2.getData(filterStartTime, filterEndTime)).getDoubleValue();
				rc = Double.compare(v1, v2);			
				break;							

			}

			// If descending order, flip the direction
			if (sortDirection == DESCENDING)
				rc = -rc;

			return rc;
		}

	}

	private class ColumnSelectionAdapter extends SelectionAdapter {

		private int column;

		public ColumnSelectionAdapter(int column) {
			this.column = column;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			TableColumn tc = (TableColumn)e.getSource();
			((CounterSorter) viewer.getSorter()).doSort(column);
			tc.getParent().setSortColumn(tc);

			switch (sortDirection) {
			case ASCENDING:
				tc.getParent().setSortDirection(SWT.UP);
				break;
			case DESCENDING:
				tc.getParent().setSortDirection(SWT.DOWN);
				break;							
			}

			viewer.refresh();
		}

	}

	public CounterStatsTableViewer(Composite parent) {
		super(parent, SWT.NULL);
		setupTableViewer();
	}	

	private void setupTableViewer() {	

		FillLayout compositeLayout = new FillLayout(SWT.VERTICAL);
		setLayout(compositeLayout);
		//Set Table Layout
		Table table = new Table(this, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);			

		TableColumn column;

		column = new TableColumn(table, SWT.CENTER);		
		column.setResizable(false);		
		layout.addColumnData(new ColumnWeightData(4, 28, false));		

		column = new TableColumn(table, SWT.LEFT);		
		column.setText("Computer");
		layout.addColumnData(new ColumnWeightData(15, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_COMPUTER));

		column = new TableColumn(table, SWT.LEFT);
		column.setText("Object");
		layout.addColumnData(new ColumnWeightData(20, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_OBJECT));

		column = new TableColumn(table, SWT.LEFT);
		column.setText("Counter");
		layout.addColumnData(new ColumnWeightData(30, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_COUNTER));

		column = new TableColumn(table, SWT.LEFT);
		column.setText("Instance");
		layout.addColumnData(new ColumnWeightData(20, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_INSTANCE));

		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Samples #");
		layout.addColumnData(new ColumnWeightData(15, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_SAMPLES));

		column = new TableColumn(table, SWT.RIGHT);	
		column.setText("Minimum");
		layout.addColumnData(new ColumnWeightData(30, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_MINIMUM));

		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Average");
		layout.addColumnData(new ColumnWeightData(30, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_AVERAGE));

		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Maximum");
		layout.addColumnData(new ColumnWeightData(30, true));
		column.addSelectionListener(new ColumnSelectionAdapter(COLUMN_MAXIMUM));

		column = new TableColumn(table, SWT.RIGHT);
		layout.addColumnData(new ColumnWeightData(5, true));	

		table.setHeaderVisible(true);		

		viewer = new CheckboxTableViewer(table);	
		graphViewer = new GraphViewer(this);	

		//Create Label Provider
		viewer.setLabelProvider(new ITableLabelProvider() {

			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				Counter ct = (Counter)element;
				switch (columnIndex) {
				case COLUMN_COMPUTER:
					return ct.getComputer();
				case COLUMN_OBJECT:
					return ct.getObject();
				case COLUMN_COUNTER:
					return ct.getCounter();
				case COLUMN_INSTANCE:					
					return ct.getInstance();
				case COLUMN_SAMPLES:
					return Integer.toString(ct.getData(filterStartTime, filterEndTime).length);
				case COLUMN_MINIMUM:					
					return CounterValue.getMin(ct.getData(filterStartTime, filterEndTime)).getStringValue(ct.getDataType());
				case COLUMN_AVERAGE:					
					return CounterValue.getAverage(ct.getData(filterStartTime, filterEndTime)).getStringValue(ct.getDataType());
				case COLUMN_MAXIMUM:				
					return CounterValue.getMax(ct.getData(filterStartTime, filterEndTime)).getStringValue(ct.getDataType());
				default:
					return "";					
				}				
			}

		});

		//Set Content Provider
		viewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// TODO Auto-generated method stub

			}

			@Override
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}

		});

		//Set Sorter
		viewer.setSorter(new CounterSorter());

		viewer.addSelectionChangedListener(new CounterSelecter());

		perfLogData = new PerfLogData();		

		updateCounter();
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = (Date)startTime.clone();
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = (Date)endTime.clone();
	}

	public void setFilterStartTime(Date startTime) {
		this.filterStartTime = (Date)startTime.clone();
		graphViewer.setStartTime(filterStartTime);		
	}

	public Date getFilterStartTime() {
		return filterStartTime;
	}

	public void setFilterEndTime(Date endTime) {
		this.filterEndTime = (Date)endTime.clone();
		graphViewer.setEndTime(filterEndTime);
	}

	public Date getFilterEndTime() {
		return filterEndTime;
	}

	public void loadPerfLogFile(String filePath) throws Exception {
		perfLogData.loadPerfLogFile(filePath);

		updateCounter();
	}

	public void setTimeFilter(Date start, Date end) {
		filterStartTime = (Date)start.clone();
		filterEndTime = (Date)end.clone();

		graphViewer.setStartTime(filterStartTime);
		graphViewer.setEndTime(filterEndTime);

		updateCounter();
	}

	public void clearTimeFilter() {
		filterStartTime = (Date)startTime.clone();
		filterEndTime = (Date)endTime.clone();

		graphViewer.setStartTime(filterStartTime);
		graphViewer.setEndTime(filterEndTime);

		updateCounter();
	}

	public void removeSelectedCounter() {
		Object[] selectedItems = viewer.getCheckedElements();		
		for (int i = 0; i < selectedItems.length; ++i) {
			perfLogData.removeCounter(selectedItems[i]);
		}
		if (selectedItems.length > 0)
			updateCounter();
	}	

	private void updateCounter() {		
		startTime = perfLogData.getCounterStartTime();
		endTime = perfLogData.getCounterEndTime();

		if (startTime == null || endTime == null) {
			startTime = new Date();
			endTime = (Date)startTime.clone();		
		}

		if (filterStartTime == null || filterStartTime.compareTo(startTime) < 0 || filterStartTime.compareTo(endTime) > 0) {
			filterStartTime = (Date)startTime.clone();
			graphViewer.setStartTime(filterStartTime);
		}

		if (filterEndTime == null || filterEndTime.compareTo(startTime) < 0 || filterEndTime.compareTo(endTime) > 0) {
			filterEndTime = (Date)endTime.clone();
			graphViewer.setEndTime(filterEndTime);
		}

		viewer.setInput(perfLogData.getCounterList());	
		viewer.refresh();
		graphViewer.redraw();
	}

	public void removeAllCounter() {
		perfLogData.removeAllCounter();
		updateCounter();		
	}

	public void CopyToClipboard(Clipboard cb) {
		StringBuilder sb = new StringBuilder();
		sb.append("Counter\tSamples\tMinimum\tAverage\tMaximum" + System.getProperty("line.separator"));
		TableItem[] items = viewer.getTable().getItems();
		for (int i = 0; i < items.length; ++i) {
			Counter ct = (Counter)items[i].getData();
			sb.append(ct.getFullName() + "\t" 
					+ Integer.toString(ct.getData(filterStartTime, filterEndTime).length) + "\t"
					+ CounterValue.getMin(ct.getData(filterStartTime, filterEndTime)).getStringValue(ct.getDataType()) + "\t"
					+ CounterValue.getAverage(ct.getData(filterStartTime, filterEndTime)).getStringValue(ct.getDataType()) + "\t"
					+ CounterValue.getMax(ct.getData(filterStartTime, filterEndTime)).getStringValue(ct.getDataType())
					+ System.getProperty("line.separator"));
		}

		TextTransfer textTransfer = TextTransfer.getInstance();
		cb.setContents(new Object[] { sb.toString() },
				new Transfer[] { textTransfer });		

	}

	public void updateFilterLine() {
		setTimeFilter(graphViewer.getStartLineDate(), graphViewer.getEndLineDate());
		graphViewer.resetFilterLine();
	}



}
