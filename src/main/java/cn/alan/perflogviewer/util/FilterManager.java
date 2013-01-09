package cn.alan.perflogviewer.util;

import java.util.Date;

public class FilterManager {
	
	private long globalStartTime;
	private long globalEndTime;
	private long filterStartTime;
	private long filterEndTime;
	
	public FilterManager() {
		reset();
	}
	
	public Date getGlobalStartTime() {
		return new Date(globalStartTime);
	}
	
	public Date getGlobalEndTime() {
		return new Date(globalEndTime);
	}
	
	public Date getFilterStartTime() {
		return new Date(filterStartTime);
	}
	
	public Date getFilterEndTime() {
		return new Date(filterEndTime);
	}
	
	public void setGlobalStartTime(Date time) {
		setGlobalStartTime(time.getTime());		
	}
	
	public void setGlobalStartTime(long time) {		
		globalStartTime = time;
		if (time > globalEndTime && globalEndTime != -1)
			globalEndTime = time;
		checkAndUpdatefilterTime();
	}
	
	public void setGlobalEndTime(Date time) {
		setGlobalEndTime(time.getTime());		
	}
	
	public void setGlobalEndTime(long time) {		
		globalEndTime = time;
		if (time < globalStartTime && globalStartTime != -1)
			globalStartTime = time;
		checkAndUpdatefilterTime();
	}
	
	public void setFilterStartTime(Date time) {
		setFilterStartTime(time.getTime());		
	}
	
	public void setFilterStartTime(long time) {		
		if (time < globalStartTime || time > globalEndTime)  
			throw new IllegalArgumentException("Error: filter start time is out of the global time range");
		
		filterStartTime = time;		
		
		if (filterEndTime < filterStartTime)
			filterEndTime = filterStartTime;		
	}
	
	public void setFilterEndTime(Date time) {
		setFilterEndTime(time.getTime());		
	}
	
	public void setFilterEndTime(long time) {		
		if (time < globalStartTime || time > globalEndTime)  
			throw new IllegalArgumentException("Error: filter start time is out of the global time range");
		
		filterEndTime = time;
		
		if (filterEndTime < filterStartTime)
			filterStartTime = filterEndTime;		
	}
	
	/**
	 * Update filter time range according to the global time range 
	 * @throws IllegalStateException if globalStartTime > globalEndTime 
	 */

	private void checkAndUpdatefilterTime() {
		
		if (globalStartTime == -1 || globalEndTime == -1)
			return;
		
		if (globalStartTime > globalEndTime)
			throw new IllegalStateException("Error: global start time > global end time");
		
		if (filterStartTime < globalStartTime || filterStartTime > globalEndTime) {
			filterStartTime = globalStartTime;
		}
				
		if (filterEndTime < globalStartTime || filterEndTime > globalEndTime) {
			filterEndTime = globalEndTime;
		}
		
	}
	
	private void reset() {
		globalStartTime = -1;
		globalEndTime = -1;
		filterStartTime = -1;
		filterEndTime = -1;
	}

	public boolean isValid() {		
		return globalStartTime != -1 && globalStartTime <= filterStartTime && filterStartTime<= filterEndTime && filterEndTime <= globalEndTime;		
	}

}
