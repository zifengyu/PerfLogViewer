package cn.alan.perflogviewer.datamodel;

import java.util.Date;

import javax.swing.table.AbstractTableModel;

public class PerfTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private PerfLogData perfLogData;
		
	public static final String[] HEADER_NAME = {"", "Group", "Name", "Samples", "Minimum", "Average", "Maximum", "Std. Devisation"};

	public PerfTableModel(PerfLogData data) {
		perfLogData = data;			
	}
	
	@Override
	public String getColumnName(int arg0) {		
		return HEADER_NAME[arg0];
	}
	
	@Override
	public int getColumnCount() {		
		return HEADER_NAME.length;
	}

	@Override
	public int getRowCount() {		
		return perfLogData.getCounterNum();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Counter counter = perfLogData.getCounterList()[row];
		Object value = "";
		switch (col) {
		case -1:
			return counter;
		case 1:
			value = counter.getGroup();
			break;
		case 2:
			value = counter.getMeasurement();
			break;
		case 3:
			value = counter.getCounterStats().samples;
			break;
		case 4:
			value = counter.getCounterStats().minimum;
			break;
		case 5:
			value = counter.getCounterStats().average;
			break;
		case 6:
			value = counter.getCounterStats().maximum;
			break;	
		case 7:
			value = counter.getCounterStats().stdDeviation;
			break;	
		}
		return value;
	}
	/*

	public Date getStartTime() {
		return (Date) startTime.clone();
	}

	public void setStartTime(Date startTime) {
		this.startTime = new Date(startTime.getTime());
	}

	public Date getEndTime() {
		return (Date) endTime.clone();
	}

	public void setEndTime(Date endTime) {
		this.endTime = new Date(endTime.getTime());;
	}
	*/
	

}
