package cn.alan.perflogviewer.datamodel;

import java.text.NumberFormat;

public class CounterValue {
	
	public final static int DATATYPE_INTEGER	= 0;
	public final static int DATATYPE_BYTE		= 1;
	public final static int DATATYPE_PERCENT	= 2;
	public final static int DATATYPE_DEFAULT	= 99;
	
	public CounterValue(String value) {
		this.value = Double.parseDouble(value);
	}
	
	private CounterValue(double value) {
		this.value = value;
	}
	
	public double getDoubleValue() {
		return value;
	}
	
	public String getStringValue(int dataType) {
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			return "N/A";
		}
		String valueString;
		NumberFormat format = NumberFormat.getInstance();
		switch (dataType) {
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
			valueString = format.format(value);
			break;
		default:
			format.setMinimumFractionDigits(3);
			format.setMaximumFractionDigits(3);
			valueString = format.format(value);					
		}
		return valueString;
	}
	
	public static CounterValue getAverage(Object[] list) {
		double total = 0;
		for (int i = 0; i < list.length; ++i)
			total += ((CounterValue)list[i]).value;
		return new CounterValue(total / list.length);
	}
	
	public static CounterValue getMax(Object[] list) {
		Double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < list.length; ++i)
			if (max.compareTo(((CounterValue)list[i]).value) < 0)
				max = ((CounterValue)list[i]).value;
		return new CounterValue(max);
	}
	
	public static CounterValue getMin(Object[] list) {
		Double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < list.length; ++i)
			if (min.compareTo(((CounterValue)list[i]).value) > 0)
				min = ((CounterValue)list[i]).value;
		return new CounterValue(min);
	}
	
	private double value;
	

}
