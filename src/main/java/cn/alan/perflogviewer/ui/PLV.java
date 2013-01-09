package cn.alan.perflogviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.SwingConstants;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.RectangleInsets;

import cn.alan.perflogviewer.datamodel.Counter;
import cn.alan.perflogviewer.datamodel.CounterValue;
import cn.alan.perflogviewer.datamodel.PerfChartDataSet;
import cn.alan.perflogviewer.datamodel.PerfLogData;
import cn.alan.perflogviewer.datamodel.PerfTableModel;
import cn.alan.perflogviewer.util.FilterManager;

public class PLV extends JPanel implements ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private static int DIVIDER_SIZE = 4;

	private static JFrame frame;	
	private static PLV myPLV = null;

	private JFileChooser fc;

	private JSplitPane splitPane;
	private JSplitPane bottomSplitPane;

	private PerfTableModel tableModel;
	private JTable tableView;

	private PerfChartDataSet perfChartDataSet;
	private ChartPanel chartView;
	private DateAxis domain;
	
	private PreviewComponent previewComponent;

	private StatusBar statusBar;

	private PerfLogData perfLogData;
	private FilterManager filterManager;

	private int lastSelectedRow = -1;

	public PLV() {
		super(new BorderLayout());
		perfLogData = new PerfLogData();
		filterManager = new FilterManager();
		setupLookAndFeel();
	}

	public static PLV get() {
		if (null == myPLV) {
			myPLV = new PLV();
		}	

		return myPLV;
	}

	private static void createAndShowGUI() {
		//Create and set up the window.
		frame = new JFrame(" PerfLog Viewer");

		Image image = PLV.createImageIcon("PLV.png").getImage();
		frame.setIconImage(image);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getRootPane().setPreferredSize(PrefManager.get().getPreferredSize());
		frame.setLocation(PrefManager.get().getWindowPos());
		PLV.get().init();
		PLV.get().setOpaque(true);
		frame.setContentPane(PLV.get());

		/**
		 * add window listener for persisting state of main frame
		 */
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				PLV.get().saveState();
			}

			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});	

		//Display the window.
		frame.pack();
		frame.setExtendedState(PrefManager.get().getWindowState());
		frame.setVisible(true);		
	}

	private void init() {	
		perfChartDataSet = new PerfChartDataSet();

		domain = new DateAxis("Time");		
		NumberAxis range = new NumberAxis("");
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.red);		
		XYPlot plot = new XYPlot(perfChartDataSet, domain, range, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		domain.setAutoRange(true);
		domain.setLowerMargin(0.0);
		domain.setUpperMargin(0.0);
		domain.setTickLabelsVisible(true);

		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		JFreeChart chart = new JFreeChart("", new Font("SansSerif", Font.BOLD, 24), plot, true);
		chart.setBackgroundPaint(Color.white);
		chartView = new ChartPanel(chart);
		chartView.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.BLACK)));
		//add(chartPanel);

		/*
		StandardChartTheme standardChartTheme=new StandardChartTheme("PERF");  
		standardChartTheme.setExtraLargeFont(new Font("SansSerif", Font.PLAIN, 13));  
		standardChartTheme.setRegularFont(new Font("SansSerif", Font.PLAIN, 11));  
		standardChartTheme.setLargeFont(new Font("SansSerif", Font.PLAIN, 12));  
		ChartFactory.setChartTheme(standardChartTheme);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"", // title
				"Time", // x-axis label
				"", // y-axis label
				perfChartDataSet, // data
				true, // create legend?
				false, // generate tooltips?
				false // generate URLs?
				);

		chartView = new ChartPanel(chart);
		chartView.setPopupMenu(null);
		chartView.setDomainZoomable(false);
		chartView.setRangeZoomable(false);
		 */
		tableModel = new PerfTableModel(perfLogData);
		tableView = new PerfTable();

		TableColumnModel columnModel = new DefaultTableColumnModel();
		//for (int i = 0; i < PerfTableModel.HEADER_NAME.length; ++i) {
		//||Color||Group||Measurement||Samples||Minimum||Average||Maximum||Std. D||	


		TableColumn column = new TableColumn(0);
		column.setHeaderValue("");
		column.setResizable(false);
		column.setMinWidth(20);
		column.setMaxWidth(20);
		columnModel.addColumn(column);		

		column = new TableColumn(1);
		column.setHeaderValue("Group");
		column.setPreferredWidth(100);
		columnModel.addColumn(column);

		column = new TableColumn(2);
		column.setHeaderValue("Measurement");
		column.setPreferredWidth(200);
		columnModel.addColumn(column);		


		column = new TableColumn(3);
		column.setHeaderValue("Samples");
		column.setPreferredWidth(50);
		columnModel.addColumn(column);		

		column = new TableColumn(4);
		column.setHeaderValue("Minimum");
		columnModel.addColumn(column);

		column = new TableColumn(5);
		column.setHeaderValue("Average");
		columnModel.addColumn(column);

		column = new TableColumn(6);
		column.setHeaderValue("Maximum");
		columnModel.addColumn(column);

		column = new TableColumn(7);
		column.setHeaderValue("Std. Deviation");
		columnModel.addColumn(column);


		tableView.setModel(tableModel);
		tableView.setColumnModel(columnModel);

		Comparator<CounterValue> counterValueComparator = new Comparator<CounterValue>() {

			@Override
			public int compare(CounterValue arg0, CounterValue arg1) {
				if (arg0.getDoubleValue() > arg1.getDoubleValue()) {
					return 1;
				}
				if (arg0.getDoubleValue() < arg1.getDoubleValue()) {
					return -1;
				}
				return 0;
			}

		};

		TableRowSorter<PerfTableModel> sorter = new TableRowSorter<PerfTableModel>(tableModel);
		sorter.setComparator(3, counterValueComparator);
		sorter.setComparator(4, counterValueComparator);
		sorter.setComparator(5, counterValueComparator);
		sorter.setComparator(6, counterValueComparator);
		sorter.setComparator(7, counterValueComparator);

		tableView.setRowSorter(sorter);		
		tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		tableView.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollPane = new JScrollPane(tableView);
		
		previewComponent = new PreviewComponent(filterManager, this);

		//Add the scroll panes to a split pane.
		bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		bottomSplitPane.setLeftComponent(scrollPane);
		bottomSplitPane.setRightComponent(previewComponent);
		bottomSplitPane.setDividerSize(DIVIDER_SIZE);
		bottomSplitPane.setContinuousLayout(true);
		bottomSplitPane.setDividerLocation(PrefManager.get().getBottomDividerPos());

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBottomComponent(bottomSplitPane);
		splitPane.setTopComponent(chartView);
		splitPane.setDividerSize(DIVIDER_SIZE);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(PrefManager.get().getDividerPos());

		add(splitPane, BorderLayout.CENTER);

		MainMenu mainMenu = new MainMenu(this);
		//frame.setJMenuBar(mainMenu);
		add(mainMenu.getToolBar(), BorderLayout.NORTH);

		statusBar = new StatusBar(true);
		add(statusBar, BorderLayout.SOUTH);

		fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);

	}

	/**
	 * save the application state to preferences.
	 */
	private void saveState() {
		PrefManager pm = PrefManager.get();
		pm.setWindowState(frame.getExtendedState());		
		pm.setPreferredSize(frame.getRootPane().getSize());
		pm.setWindowPos(frame.getX(), frame.getY());
		pm.setDividerPos(splitPane.getDividerLocation());
		pm.setBottomDividerPos(bottomSplitPane.getDividerLocation());
		pm.flush();
	}

	private void setupLookAndFeel() {
		try {
			//--- set the desired preconfigured plaf ---
			UIManager.LookAndFeelInfo currentLAFI = null;

			// retrieve plaf param.
			String plaf = "Mac,Windows,Metal";           

			// this line needs to be implemented in order to make L&F work properly
			UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());

			// query list of L&Fs
			UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();

			if ((plaf != null) && (!"".equals(plaf))) {

				String[] instPlafs = plaf.split(",");
				search:
					for(int i = 0; i < instPlafs.length; i++) {
						for(int j = 0; j < plafs.length; j++) {
							currentLAFI = plafs[j];
							//System.out.println(currentLAFI.getName());
							if(currentLAFI.getName().startsWith(instPlafs[i])) {
								UIManager.setLookAndFeel(currentLAFI.getClassName());

								// setup font
								setUIFont(new FontUIResource("SansSerif", Font.PLAIN, 12));
								break search;
							}
						}
					}
			}


		} catch (Exception except) {
			// setup font
			setUIFont(new FontUIResource("SansSerif", Font.PLAIN, 12));
		}
	}

	/**
	 * set the ui font for all tda stuff (needs to be done for create of objects)
	 * @param f the font to user
	 */
	private void setUIFont(javax.swing.plaf.FontUIResource f){
		//
		// sets the default font for all Swing components.
		// ex.
		//  setUIFont (new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,12));
		//
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}


	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = PLV.class.getResource("icons/" + path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton source = (JButton) e.getSource();
			if("Add files".equals(source.getToolTipText())) {
				addFiles();
			} else if("Clear all".equals(source.getToolTipText())) {
				perfLogData.removeAllCounter();
				tableModel.fireTableDataChanged();
			}
			source.setSelected(false);
		}

	}

	private void addFiles() {
		/*
		if(firstFile && (PrefManager.get().getPreferredSizeFileChooser().height > 0)) {
            fc.setPreferredSize(PrefManager.get().getPreferredSizeFileChooser());
        }
		 */
		int returnVal = fc.showOpenDialog(this.getRootPane());
		//fc.setPreferredSize(fc.getSize());
		//PrefManager.get().setPreferredSizeFileChooser(fc.getSize());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();
			for (int i = 0; i < files.length; ++i) {
				try {
					perfLogData.loadPerfLogFile(files[i].getAbsolutePath());
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
			
			updateFilterManager();
			
		}

	}
	
	private void updateFilterManager() {
		filterManager.setGlobalStartTime(perfLogData.getCounterStartTime());
		filterManager.setGlobalEndTime(perfLogData.getCounterEndTime());
		//filterManager.setFilterStartTime((filterManager.getGlobalStartTime().getTime() / 2 + filterManager.getGlobalEndTime().getTime() / 2));
		
		filterChanged();
	}
	
	public void filterChanged() {
		previewComponent.updateFilterPosition();
		perfLogData.updateCounterStats(filterManager.getFilterStartTime(), filterManager.getFilterEndTime());
		tableModel.fireTableDataChanged();
		domain.setRange(filterManager.getFilterStartTime(), filterManager.getFilterEndTime());
	}

	/**
	 * main startup method for PLV
	 */
	public static void main(String[] args) {        
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = tableView.getSelectedRow();
		//tableView.getValueAt(row, -1);

		if (row != -1) {
			row = tableView.getRowSorter().convertRowIndexToModel(row);
			if (row != lastSelectedRow) {
				lastSelectedRow = row;
				perfChartDataSet.removeAllCounter();
				Counter counter = perfLogData.getCounterList()[row];
				perfChartDataSet.addCounter(counter);
				previewComponent.setCounter(counter);								
		

			}
		}
	}


}
