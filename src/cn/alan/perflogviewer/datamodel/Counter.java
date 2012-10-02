package cn.alan.perflogviewer.datamodel;

import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Counter {	
	
	@Override
	public boolean equals(Object obj) {
		Counter ct = (Counter)obj;
		
		return filePath.equals(ct.filePath) 
				&& fullName.equals(ct.fullName);
		
	
	}

	public Counter(String filePath, String counterName) throws Exception {
		this.filePath = filePath;
		
		Pattern p = Pattern.compile("\\\\\\\\(.+)\\\\([^\\(]+)(\\((.*)\\))?\\\\(.+)");
		Matcher m = p.matcher(counterName);
		
		if (!m.find()) {
			throw new Exception("Parse Counter Name Fails.");			
		}
		
		fullName = counterName;
		computer = m.group(1);
		object = m.group(2);
		instance = m.group(4) == null ? "": m.group(4);
		counter = m.group(5);
		
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
		
		data = new TreeMap<Date, CounterValue>();		
	}
	
	public int getDataType() {
		return dataType;
	}

	public void addData(Date time, String value) {
		data.put(time, new CounterValue(value));		
		
	}
	
	public Object[] getData(Date startTime, Date endTime) {			
		return data.subMap(startTime, true, endTime, true).values().toArray();		
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
	
	private String filePath;
	private String fullName;
	private String computer;
	private String object;
	private String instance;
	private String counter;
	private int dataType;
	
	private TreeMap<Date, CounterValue> data;	
	
}
