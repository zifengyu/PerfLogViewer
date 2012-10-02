package cn.alan.perflogviewer.viewer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SetFilterDialog extends Dialog {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");


	protected SetFilterDialog(Shell parentShell) {
		super(parentShell);		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        Composite container2 = new Composite(container, SWT.CENTER);
        container2.setLayout(new GridLayout(2, true));
        
        Label label = new Label(container2, SWT.NONE);
        label.setText("Start Time: ");
        label = new Label(container2, SWT.NONE);
        label.setText("End Time: ");
        
        startTimeText = new Text(container2, SWT.BORDER);
        startTimeText.setText(dateFormat.format(filterStartTime));        
        
        endTimeText = new Text(container2, SWT.BORDER);
        endTimeText.setText(dateFormat.format(filterEndTime));       
        
        scale1 = new Scale(container2, SWT.HORIZONTAL);
        scale1.setMinimum(0);
    	scale1.setMaximum(100);     
        
        scale2 = new Scale(container2, SWT.HORIZONTAL);
        scale2.setMinimum(0);
    	scale2.setMaximum(100);
   
        
        long st = startTime.getTime();
        long et = endTime.getTime();
        
        if (st == et) {
        	scale1.setSelection(50);
        	scale2.setSelection(50);
        	scale1.setEnabled(false);        	
        	scale2.setEnabled(false);        	
        } else {
        	scale1.setSelection((int)Math.ceil(100 * ((double)filterStartTime.getTime() - st) / (et - st)));
        	scale2.setSelection((int)Math.ceil(100 * ((double)filterEndTime.getTime() - st) / (et - st)));
        }
        
        scale1.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int select = scale1.getSelection();
				int select2 = scale2.getSelection();
				if (select > select2) {
					select = select2;
					scale1.setSelection(select);
				}
				long time = endTime.getTime() * select / 100 + (100 - select) * startTime.getTime() / 100;				
				filterStartTime.setTime(time);				
				startTimeText.setText(dateFormat.format(filterStartTime));				
			}        	
        });
        
        scale2.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int select = scale2.getSelection();
				int select2 = scale1.getSelection();
				
				if (select < select2) {
					select = select2;
					scale2.setSelection(select);
				}			
				
				long time = endTime.getTime() * select / 100 + (100 - select) * startTime.getTime() / 100;
				filterEndTime.setTime(time); 
				endTimeText.setText(dateFormat.format(filterEndTime));				
			}        	
        });
        
        return container;

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
	
	
	
	public Date getFilterStartTime() {
		return filterStartTime;
	}

	public void setFilterStartTime(Date filterStartTime) {
		this.filterStartTime = (Date)filterStartTime.clone();
	}

	public Date getFilterEndTime() {
		return filterEndTime;
	}

	public void setFilterEndTime(Date filterEndTime) {
		this.filterEndTime = (Date)filterEndTime.clone();
	}



	private Date startTime;
	private Date endTime;
	private Date filterStartTime;
	private Date filterEndTime;
	private Text startTimeText;
	private Text endTimeText;
	private Scale scale1, scale2;	

}
