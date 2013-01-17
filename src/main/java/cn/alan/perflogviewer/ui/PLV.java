package cn.alan.perflogviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
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
import cn.alan.perflogviewer.util.FileUtils;
import cn.alan.perflogviewer.util.FilterManager;

public class PLV extends JPanel implements ListSelectionListener, ActionListener, ClipboardOwner {

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
	
	private Clipboard clipboard;

	private int lastSelectedRow = -1;

	public PLV() {
		super(new BorderLayout());
		perfLogData = new PerfLogData();
		filterManager = new FilterManager();
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();		
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

		tableModel = new PerfTableModel(perfLogData);
		tableView = new PerfTable();

		TableColumnModel columnModel = new DefaultTableColumnModel();

		//||Color||Group||Measurement||Samples||Minimum||Average||Maximum||Std. D||	

		TableColumn column = new TableColumn(0);
		column.setHeaderValue("");
		column.setResizable(false);
		column.setMinWidth(20);
		column.setMaxWidth(20);
		columnModel.addColumn(column);		

		column = new TableColumn(1);
		column.setHeaderValue("Group");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(1));
		columnModel.addColumn(column);

		column = new TableColumn(2);
		column.setHeaderValue("Measurement");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(2));
		columnModel.addColumn(column);

		column = new TableColumn(3);
		column.setHeaderValue("Samples");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(3));
		columnModel.addColumn(column);		

		column = new TableColumn(4);
		column.setHeaderValue("Minimum");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(4));
		columnModel.addColumn(column);

		column = new TableColumn(5);
		column.setHeaderValue("Average");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(5));
		columnModel.addColumn(column);

		column = new TableColumn(6);
		column.setHeaderValue("Maximum");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(6));
		columnModel.addColumn(column);

		column = new TableColumn(7);
		column.setHeaderValue("Std. Deviation");
		column.setPreferredWidth(PrefManager.get().getTableViewColumnSize(7));
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
		fc.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {   
					return true;
				}

				String extension = FileUtils.getExtension(f); 
				if (extension != null) {
					if (extension.equals(FileUtils.CSV_TYPE)) {
						return true;            
					} 					
				}
				return false;
			}
			@Override
			public String getDescription() {				
				return "Comma Separated Value Files (*.csv)";
			}

		});

		setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 1L;

			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);

					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

					addFiles(droppedFiles.toArray(new File[0]));
					//for (File file : droppedFiles) {
					//	statusBar.setInfoText(file.getName());
					//}
				} catch (Exception ex) {
					String msg = ex.getMessage();
					if (msg != null && msg.length() > 40) {
						msg = msg.substring(0, 40);
					}
					if (msg != null) {
						JOptionPane.showMessageDialog(frame,
								msg,
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
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

		TableColumnModel columnModel = tableView.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); ++i) {
			pm.setTableViewColumnSize(i, columnModel.getColumn(i).getWidth());
		}

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
				int returnVal = fc.showOpenDialog(this.getRootPane());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fc.getSelectedFiles();
					addFiles(files);
				}
			} else if("Clear all".equals(source.getToolTipText())) {
				/*reset all*/
				perfLogData.removeAllCounter();
				lastSelectedRow = -1;
				tableModel.fireTableDataChanged();
				perfChartDataSet.removeAllCounter();
				previewComponent.setCounter(null);
				filterManager.reset();
			} else if ("Remove selected row".equals(source.getToolTipText())) {
				removeCounterFromTable();				
			} else if ("Export to clipboard".equals(source.getToolTipText())) {
				StringBuilder sb = new StringBuilder();
				sb.append("Group\tMeasurement\tSamples\tMinimum\tAverage\tMaximum\tStd. Deviation\n");
				int col = tableView.getColumnCount();
				int row = tableView.getRowCount();
				
				for (int i = 0; i < row; ++i) {					
					for (int j = 1; j < col; ++j) {						
						sb.append(tableView.getValueAt(i, j));
						if (j == col - 1)
							sb.append("\n");
						else
							sb.append("\t");
						}					
				}
				
				clipboard.setContents(new StringSelection(sb.toString()), this);				
			} else if ("Export to image".equals(source.getToolTipText())) {
				long timestamp = System.currentTimeMillis();
				File file;
				file = new File("plv-" + timestamp + ".png");
				/*
				do {
					//timestamp = 1;
					file = new File("plv-" + timestamp + ".png");
					timestamp++;
				} while (!file.getAbsoluteFile().exists());
				 */
				try {
					ChartUtilities.saveChartAsPNG(file, chartView.getChart(), chartView.getSize().width, chartView.getSize().height);
					JOptionPane.showMessageDialog(frame,
							"PNG file is generated.\n" + file.getAbsolutePath());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame,
							"Failed in generatng PNG file.\n" + e1.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);					
				}				
			} else if ("Chart configuration".equals(source.getToolTipText())) {
				String title = chartView.getChart().getTitle().getText();
				title = (String)JOptionPane.showInputDialog(frame,						
						"Enter chart title:",
						"Chart configuration - 1/3",
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						title);
				if (title != null) {
					chartView.getChart().setTitle(title.trim());
					String xLabel = chartView.getChart().getXYPlot().getDomainAxis().getLabel();
					xLabel = (String)JOptionPane.showInputDialog(frame,
							"Enter X-axis label:",
							"Chart configuration - 2/3",
							JOptionPane.QUESTION_MESSAGE,
							null,
							null,
							xLabel);
					if (xLabel != null) {
						chartView.getChart().getXYPlot().getDomainAxis().setLabel(xLabel.trim());
						String yLabel = chartView.getChart().getXYPlot().getRangeAxis().getLabel();
						yLabel = (String)JOptionPane.showInputDialog(frame,
								"Enter Y-axis label:",
								"Chart configuration - 3/3",
								JOptionPane.QUESTION_MESSAGE,
								null,
								null,
								yLabel);
						if (yLabel != null) {
							chartView.getChart().getXYPlot().getRangeAxis().setLabel(yLabel.trim());
						}
					}
				}
			} else if ("Help".equals(source.getToolTipText())) {
				JOptionPane.showMessageDialog(frame,
						"The author is too lazy to provide a help text.\nHAHAHAHAHA------",
						"PLV 2.0",
						JOptionPane.INFORMATION_MESSAGE,
						createImageIcon("rabbit.gif"));

			} 
			source.setSelected(false);
		}

	}

	private void addFiles(File[] files) {

		String datePattern = perfLogData.DEFAULT_DATE_PATTERN;
		for (int i = 0; i < files.length; ) {				

			try {
				perfLogData.loadPerfLogFile(files[i].getAbsolutePath(), datePattern);
				datePattern = perfLogData.DEFAULT_DATE_PATTERN;
				i = i + 1;
			} catch (ParseException e) {	
				String dateString = e.getMessage();
				dateString = dateString.substring(dateString.indexOf("\""));
				if (dateString == null) {
					//statusBar.setInfoText("Could not parse the file: " + files[i].getName());
					i = i + 1;
					continue;
				}

				String s = (String)JOptionPane.showInputDialog(
						frame,
						"Could not parse the input date. \nPlease enter a new format:\n"
								+ dateString,
								"Set Date Format",
								JOptionPane.QUESTION_MESSAGE,
								null,
								null,
								datePattern);

				if (s != null) {
					datePattern = s.trim();
				} else {
					i = i + 1;
					continue;
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame,
						"Could not process the file: " + files[i].getName(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				i = i + 1;
			} 

		}
		updateFilterManager();			
	}

	private void updateFilterManager() {
		long oldStartTime = filterManager.getGlobalStartTime().getTime();
		long oldEndTime = filterManager.getGlobalEndTime().getTime();
		long newStartTime = perfLogData.getCounterStartTime().getTime();
		long newEndTime = perfLogData.getCounterEndTime().getTime();

		if (oldStartTime != newStartTime || oldEndTime != newEndTime) {
			filterManager.setGlobalStartTime(newStartTime);
			filterManager.setGlobalEndTime(newEndTime);
			filterChanged(true);
		} else {
			filterChanged(false);
		}
	}

	public void filterChanged(boolean isGlobalChanged) {
		if (!filterManager.isValid())
			return ;

		if (isGlobalChanged)
			previewComponent.updateFilterPosition();

		perfLogData.updateCounterStats(filterManager.getFilterStartTime(), filterManager.getFilterEndTime());	
		if (filterManager.getFilterStartTime().getTime() < filterManager.getFilterEndTime().getTime())
			domain.setRange(filterManager.getFilterStartTime(), filterManager.getFilterEndTime());	
		tableModel.fireTableDataChanged();

		if (lastSelectedRow != -1) {
			int row = tableView.getRowSorter().convertRowIndexToView(lastSelectedRow);
			tableView.requestFocusInWindow();
			tableView.setRowSelectionInterval(row, row);
		} else if (tableView.getRowCount() > 0) {			
			tableView.requestFocusInWindow();
			tableView.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = tableView.getSelectedRow();

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

	private void removeCounterFromTable() {
		int row = tableView.getSelectedRow();		

		if (row != -1) {
			row = tableView.getRowSorter().convertRowIndexToModel(row);			
			lastSelectedRow = -1;
			perfChartDataSet.removeAllCounter();
			previewComponent.setCounter(null);

			Counter counter = perfLogData.getCounterList()[row];				
			perfLogData.removeCounter(counter);

			tableModel.fireTableDataChanged();
			updateFilterManager();
		}
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
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}




}
