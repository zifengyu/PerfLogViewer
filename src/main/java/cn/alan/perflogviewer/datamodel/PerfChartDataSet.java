package cn.alan.perflogviewer.datamodel;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class PerfChartDataSet extends TimeSeriesCollection {

	private static final long serialVersionUID = 1L;
	
	public void addCounter(Counter counter) {
		TimeSeries s1 = new TimeSeries(counter.getFullName());
		Map<Date, CounterValue> counterData = counter.getData();
		for (Iterator<Date> iter = counterData.keySet().iterator(); iter.hasNext(); ) {
			Date date = iter.next();
			s1.add(new FixedMillisecond(date), counterData.get(date).getDoubleValue());
		}
		addSeries(s1);		
	}
	
	public void removeAllCounter() {
		removeAllSeries();		
	}
	

}
