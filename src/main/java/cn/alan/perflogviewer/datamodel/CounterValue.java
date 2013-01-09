package cn.alan.perflogviewer.datamodel;

import java.text.NumberFormat;

public class CounterValue {

	public final static int DATATYPE_INTEGER	= 0;
	public final static int DATATYPE_BYTE		= 1;
	public final static int DATATYPE_PERCENT	= 2;
	public final static int DATATYPE_DEFAULT	= 99;

	private double value;
	private int type;

	public CounterValue(String value, int type) {
		this.value = Double.parseDouble(value);
		this.type = type;
	}

	private CounterValue(double value, int type) {
		this.value = value;
		this.type = type;
	}

	public double getDoubleValue() {
		return value;
	}

	public String getStringValue() {
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			return "N/A";
		}
		String valueString;
		NumberFormat format = NumberFormat.getInstance();
		switch (type) {
		case DATATYPE_INTEGER:
			format.setMaximumFractionDigits(0);
			valueString = format.format(value);
			break;
		case DATATYPE_BYTE:
			double v = value;
			format.setMaximumFractionDigits(0);
			if (v > 1048576) {
				v /= 1048576;
				valueString = format.format(v) + " MB";				
			} else if (v > 1024) {
				v /= 1024;
				valueString = format.format(v) + " KB";	
			} else {
				valueString = format.format(v);	
			}
			break;
		case DATATYPE_PERCENT:
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
			valueString = format.format(value) + "%";
			break;
		default:
			format.setMinimumFractionDigits(3);
			format.setMaximumFractionDigits(3);
			valueString = format.format(value);					
		}
		return valueString;
	}

	@Override
	public String toString() {		
		return getStringValue();
	}

	public static class CounterStats {
		public CounterValue samples;
		public CounterValue minimum;
		public CounterValue average;
		public CounterValue maximum;
		public CounterValue stdDeviation;		
	}

	public static CounterStats calculateStats(Object[] list) {
		int resType = list.length > 0 ? ((CounterValue)list[0]).type : DATATYPE_DEFAULT;
		
		CounterStats stats = new CounterStats();

		int count = list.length;	

		double avg = 0;
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		double std = 0;

		for (int i = 0; i < list.length; ++i) {
			double val = ((CounterValue)list[i]).value;
			avg += val;
			if (min > val)
				min = val;
			if (max < val)
				max = val;
		}

		if (count > 0) {
			avg /= count;
			for (int i = 0; i < list.length; ++i) {
				double val = ((CounterValue)list[i]).value;
				std += (val - avg) * (val - avg);
			}
			std = Math.sqrt(std / count);
		}
		
		stats.samples = new CounterValue(count, DATATYPE_INTEGER);
		stats.minimum = new CounterValue(min, resType);
		stats.average = new CounterValue(avg, resType);
		stats.maximum = new CounterValue(max, resType);
		stats.stdDeviation = new CounterValue(std, resType);
		
		return stats;
	}
	
	public static double getAverage(Object[] list) {
		double total = 0;		
		for (int i = 0; i < list.length; ++i)
			total += ((CounterValue)list[i]).value;
		return total / list.length;
	}

	public static double getMax(Object[] list) {
		Double max = Double.NEGATIVE_INFINITY;		
		for (int i = 0; i < list.length; ++i)
			if (max.compareTo(((CounterValue)list[i]).value) < 0)
				max = ((CounterValue)list[i]).value;
		return max;
	}

	public static double getMin(Object[] list) {
		Double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < list.length; ++i)
			if (min.compareTo(((CounterValue)list[i]).value) > 0)
				min = ((CounterValue)list[i]).value;
		return min;
	}
	 
}
