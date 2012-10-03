package cn.alan.perflogviewer.viewer;


import java.io.File;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;




public class MainWindow extends ApplicationWindow {	

	private CounterStatsTableViewer viewer;
	
	private final static String ICON_FOLDER_BASE				= ".";
	private final static String ADD_FILE_ICON_PATH				= ICON_FOLDER_BASE + "/icon/add_correction.gif";
	private final static String ADD_FOLDER_ICON_PATH			= ICON_FOLDER_BASE + "/icon/expand_all.gif";
	private final static String REMOVE_COUNTER_ICON_PATH		= ICON_FOLDER_BASE + "/icon/remove_correction.gif";
	private final static String REMOVE_ALL_COUNTER_ICON_PATH	= ICON_FOLDER_BASE + "/icon/delete_edit.gif";
	private final static String FILTER_ICON_PATH				= ICON_FOLDER_BASE + "/icon/filter.gif";
	private final static String APPLY_FILTER_ICON_PATH			= ICON_FOLDER_BASE + "/icon/scope.gif";
	private final static String CLEAR_FILTER_ICON_PATH			= ICON_FOLDER_BASE + "/icon/clear.gif";
	private final static String COPY_RESULT_ICON_PATH			= ICON_FOLDER_BASE + "/icon/alltopics_co.gif";
	
	public MainWindow() {
		super(null);	
		addToolBar(SWT.NONE);		
	}	

	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
		tbm.add(new ControlContribution("Custom") {

			@Override
			protected Control createControl(Composite parent) {
				final SashForm sf = new SashForm(parent, SWT.NONE);

				Button addButton = new Button(sf, SWT.PUSH);
				Image addIcon = new Image(parent.getDisplay(),ADD_FILE_ICON_PATH);
				addButton.setImage(addIcon);				
				addButton.setToolTipText("Add CSV File...");					
				addButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {	

						FileDialog dialog = new FileDialog(sf.getShell(), SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*.csv", "*.*"});

						//DirectoryDialog dialog = new DirectoryDialog(sf.getShell());
						String filePath = dialog.open();
						if (filePath != null) {
							try {
								viewer.loadPerfLogFile(filePath);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} 
						}
					}					
				});

				Button addFolderButton = new Button(sf, SWT.PUSH);
				Image addFolderIcon = new Image(parent.getDisplay(), ADD_FOLDER_ICON_PATH);
				addFolderButton.setImage(addFolderIcon);				
				addFolderButton.setToolTipText("Add Folder...");				
				addFolderButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {						

						DirectoryDialog dialog = new DirectoryDialog(sf.getShell());
						String folderPath = dialog.open();
						if (folderPath != null) {												
							try {
								loadFolder(folderPath);								
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} 
						}
					}					

					private void loadFolder(String folderPath) throws Exception {
						File dir = new File(folderPath);
						File[] fileList = dir.listFiles();
						for (int i = 0; i < fileList.length; ++i) {
							if (fileList[i].isFile() && fileList[i].getAbsolutePath().toUpperCase().endsWith(".CSV")) {								
								viewer.loadPerfLogFile(fileList[i].getAbsolutePath());
							}
							if (fileList[i].isDirectory()) {
								loadFolder(fileList[i].getAbsolutePath());								
							}
						}						
					}
				});

				Button removeButton = new Button(sf, SWT.PUSH);
				Image removeIcon = new Image(parent.getDisplay(), REMOVE_COUNTER_ICON_PATH);
				removeButton.setImage(removeIcon);				
				removeButton.setToolTipText("Remove Selected Counters");
				removeButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {							
						viewer.removeSelectedCounter();
					}					
				});

				Button removeAllButton = new Button(sf, SWT.PUSH);
				Image removeAllIcon = new Image(parent.getDisplay(), REMOVE_ALL_COUNTER_ICON_PATH);
				removeAllButton.setImage(removeAllIcon);				
				removeAllButton.setToolTipText("Remove All Counters");
				removeAllButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {							
						viewer.removeAllCounter();
					}					
				});			

				new Label(sf, SWT.SEPARATOR);

				Button setFilterButton = new Button(sf, SWT.PUSH);
				Image setFilterIcon = new Image(parent.getDisplay(), FILTER_ICON_PATH);
				setFilterButton.setImage(setFilterIcon);				
				setFilterButton.setToolTipText("Set Filter...");
				setFilterButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						SetFilterDialog filterDialog = new SetFilterDialog(sf.getShell());

						filterDialog.setStartTime(viewer.getStartTime());
						filterDialog.setEndTime(viewer.getEndTime());
						filterDialog.setFilterStartTime(viewer.getFilterStartTime());
						filterDialog.setFilterEndTime(viewer.getFilterEndTime());
						if (filterDialog.open() == Window.OK) {							
							viewer.setTimeFilter(filterDialog.getFilterStartTime(), filterDialog.getFilterEndTime());
						}						

					}					
				});	

				Button setFilterLineButton = new Button(sf, SWT.PUSH);
				Image setFilterLineIcon = new Image(parent.getDisplay(), APPLY_FILTER_ICON_PATH);
				setFilterLineButton.setImage(setFilterLineIcon);				
				setFilterLineButton.setToolTipText("Set Filter Line");
				setFilterLineButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {							
						viewer.updateFilterLine();
					}					
				});

				Button clearFilterButton = new Button(sf, SWT.PUSH);
				Image clearFilterIcon = new Image(parent.getDisplay(), CLEAR_FILTER_ICON_PATH);
				clearFilterButton.setImage(clearFilterIcon);				
				clearFilterButton.setToolTipText("Clear Filter");
				clearFilterButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {							
						viewer.clearTimeFilter();
					}					
				});

				new Label(sf, SWT.SEPARATOR);	

				Button copyButton = new Button(sf, SWT.PUSH);
				Image copyIcon = new Image(parent.getDisplay(), COPY_RESULT_ICON_PATH);
				copyButton.setImage(copyIcon);				
				copyButton.setToolTipText("Copy");
				copyButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {		
						Clipboard cb = new Clipboard(Display.getDefault());
						viewer.CopyToClipboard(cb);	
					}					
				});


				return sf;
			}

		});

		return  tbm;

	}

	@Override
	protected Control createContents(Composite parent) {	

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));

		viewer = new CounterStatsTableViewer(container);

		getShell().setText("PerfLogs Viewer");	
		getShell().setVisible(true);		

		return parent;
	}

	public static void main(String[] args) {

		MainWindow wwin = new MainWindow();

		wwin.setBlockOnOpen(true);

		wwin.open();

		Display.getCurrent().dispose();
	}

}
