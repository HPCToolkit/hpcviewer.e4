package edu.rice.cs.hpc.data.experiment.metric.format;

import java.util.Formatter;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;


/**********************************************************
 * 
 * @author laksonoadhianto
 *
 **********************************************************/
public class MetricValuePredefinedFormat implements IMetricValueFormat {
	private String format;
	
	public MetricValuePredefinedFormat(String sFormat) {
		this.format = sFormat;
	}
	
	
	public String format(MetricValue value) {
		Formatter format_str = new Formatter();
		try {
			// --------------------------------------------------------------------------------
			// temporary bug fix: if the format is hexadecimal (%x), the input value
			//  has to be either an integer or a long number. Since we store by default
			//	the value to be "double", we should convert it to long (integer is too small)
			// 
			// for next release, we need to have a universal metric value type that can 
			//	represent any possible value such as double, int, long, hex, ...
			// --------------------------------------------------------------------------------
/*			if (format.indexOf("%x")>=0) {
				long valong = (long) value.getValue();
				format_str = format_str.format(format, valong);
				
			} else*/ 
			{
				format_str = format_str.format(format, MetricValue.getValue(value));
			}

			if (format_str != null) {
				return format_str.toString();
			} else {
				return "";
			}
		} catch (java.util.IllegalFormatConversionException e) {
			System.err.println("Illegal format conversion: " + format.toString() + "\tFrom value: " + MetricValue.getValue(value));
			e.printStackTrace();
		}
		return "";
	}

	/****
	 * get the custom format
	 * 
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}
}
