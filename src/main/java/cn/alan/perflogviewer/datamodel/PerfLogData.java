package cn.alan.perflogviewer.datamodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class PerfLogData {
	
	private ArrayList<Counter> counterList = new ArrayList<Counter>();
	
	public void loadPerfLogFile(String filePath) throws Exception {
		ArrayList<Counter> newCounterList = new ArrayList<Counter>();
		File logFile = new File(filePath);
		
		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		String line;
		int lineNum = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");		
		
		while ((line = reader.readLine()) != null) {
			String[] items = line.split(",");			
			Date time = null;
			
			for (int i = 0; i < items.length; ++i) {
				if (items[i].startsWith("\""))
					items[i] = items[i].substring(1, items[i].length());
				
				if (items[i].endsWith("\""))
						items[i] = items[i].substring(0, items[i].length() - 1);
				
				items[i] = items[i].trim();		
				     
				switch (lineNum) {
				case 0: 
					if (i > 0) {						
						newCounterList.add(new Counter(logFile.getAbsolutePath(), items[i]));						
					}					
					break;													
				default:							
					if (i == 0) {
						time = dateFormat.parse(items[0]);												
					} else {
						if (!items[i].equals(""))
							newCounterList.get(i - 1).addData(time, items[i]);							
					}					
					break;					
				}				
			}	
			if (lineNum == 0)
				++lineNum;
		}	
		
		Iterator<Counter> iter = newCounterList.iterator(); 
		while (iter.hasNext()) {
			Counter ct = iter.next();
			//ct.updateStats(ct.getStartTime(), ct.getEndTime());
			if (!counterList.contains(ct)) {				
				counterList.add(ct);
			}
		}
		
		reader.close();
	}
	
	public void removeCounter(Object ct) {
		counterList.remove(ct);
	}
	
	public void removeAllCounter() {
		counterList.clear();
	}
	
	public Counter[] getCounterList() {
		return counterList.toArray(new Counter[]{});
	}
	
	public int getCounterNum() {
		return counterList.size();
	}
	
	public Date getCounterStartTime() {		
		if (counterList.size() == 0)
			return null;
		Date date = counterList.get(0).getStartTime();
		for (int i = 1; i <counterList.size(); ++i) {
			Date date1 = counterList.get(i).getStartTime();
			if (date1 != null && date1.compareTo(date) < 0)
				date = date1;
		}
		return (Date) date.clone();	
	}
	
	public Date getCounterEndTime() {		
		if (counterList.size() == 0)
			return null;
		Date date = counterList.get(0).getEndTime();
		for (int i = 1; i <counterList.size(); ++i) {
			Date date1 = counterList.get(i).getEndTime();
			if (date1 != null && date1.compareTo(date) > 0)
				date = date1;
		}
		return (Date) date.clone();	
	}
	
	public void updateCounterStats(Date start, Date end) {
		for (int i = 0; i < counterList.size(); ++i) {
			counterList.get(i).updateStats(start, end);
		}
	}

}
