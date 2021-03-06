package cn.alan.perflogviewer.datamodel;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.alan.perflogviewer.datamodel.CounterValue.CounterStats;

public class Counter {	

	@Override
	public boolean equals(Object obj) {
		Counter ct = (Counter)obj;

		return filePath.equals(ct.filePath) 
				&& fullName.equals(ct.fullName);		

	}

	public Counter(String filePath, String counterName) throws IllegalArgumentException {
		this.filePath = filePath;

		Pattern p = Pattern.compile("\\\\\\\\([^\\\\]+)\\\\(.+)");
		Matcher m = p.matcher(counterName);

		if (!m.find()) {
			throw new IllegalArgumentException("Incorrect counter name : " + counterName);			
		}
		
		fullName = counterName;
		group = m.group(1);
		measurement = m.group(2);

		p = Pattern.compile("\\\\\\\\(.+)\\\\([^\\(]+)(\\((.*)\\))?\\\\(.+)");
		m = p.matcher(counterName);

		if (m.find()) {			
			computer = m.group(1);
			object = m.group(2);
			instance = m.group(4) == null ? "": m.group(4);
			counter = m.group(5);
			//measurement = counterName.substring(counterName.indexOf(object));

			if (counter.equals("Private Bytes"))
				dataType = CounterValue.DATATYPE_BYTE;
			else if (counter.equals("Virtual Bytes"))
				dataType = CounterValue.DATATYPE_BYTE;
			else if (counter.equals("Working Set"))
				dataType = CounterValue.DATATYPE_BYTE;
			else if (counter.equals("Thread Count"))
				dataType = CounterValue.DATATYPE_INTEGER;
			else if (counter.equals("Handle Count"))
				dataType = CounterValue.DATATYPE_INTEGER;
			else if (counter.equals("ID Process"))
				dataType = CounterValue.DATATYPE_INTEGER;
			else if (counter.equals("% Processor Time"))
				dataType = CounterValue.DATATYPE_PERCENT;
			else if (counter.equals("% Disk Time"))
				dataType = CounterValue.DATATYPE_PERCENT;
			else if (counter.equals("Available MBytes"))
				dataType = CounterValue.DATATYPE_INTEGER;
			else if (counter.equals("% Committed Bytes In Use"))
				dataType = CounterValue.DATATYPE_PERCENT;
			else if (counter.equals("Page Faults/sec"))
				dataType = CounterValue.DATATYPE_INTEGER;
			else
				dataType = CounterValue.DATATYPE_DEFAULT;
		} else {
			dataType = CounterValue.DATATYPE_DEFAULT;
		}
		
		data = new TreeMap<Date, CounterValue>();		
	}

	public int getDataType() {
		return dataType;
	}

	public void addData(Date time, String value) {
		Date time2 = new Date(time.getTime());
		data.put(time2, new CounterValue(value, dataType));		
	}

	public Map<Date, CounterValue> getData() {
		return data;
	}

	public Object[] getData(Date startTime, Date endTime) {		
		if (startTime == null)
			startTime = getStartTime();
		if (endTime == null)
			endTime = getEndTime();
		if (startTime.getTime() <= endTime.getTime())
			return data.subMap(startTime, true, endTime, true).values().toArray();
		else
			return new Object[0];
	}

	public String getFilePath() {
		return filePath;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getComputer() {
		return computer;
	}

	public String getObject() {
		return object;
	}

	public String getInstance() {
		return instance;
	}

	public String getCounter() {
		return counter;
	}

	public Date getStartTime() {
		if (data.isEmpty())
			return null;
		return data.firstKey();
	}

	public Date getEndTime() {
		if (data.isEmpty())
			return null;
		return data.lastKey();
	}

	public CounterStats getCounterStats() {
		if (stats == null)
			throw new IllegalStateException();
		return stats;
	}

	public void updateStats(Date startTime, Date endTime) {
		stats = CounterValue.calculateStats(getData(startTime, endTime));
	}
	
	public String getGroup() {
		return group;
	}

	public String getMeasurement() {
		return measurement;
	}

	private String filePath;
	private String fullName;
	private String computer;
	private String object;
	private String instance;
	private String counter;
	private String group;
	private String measurement;
	private int dataType;

	private TreeMap<Date, CounterValue> data;	
	private CounterStats stats = null;
}
