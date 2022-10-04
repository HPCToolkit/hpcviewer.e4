package edu.rice.cs.hpcdata.experiment.metric.format;

import java.util.Formatter;

import edu.rice.cs.hpcdata.experiment.metric.MetricValue;


/**********************************************************
 * 
 * Metric value visualization format for user predefined format
 * 
 **********************************************************/
public class MetricValuePredefinedFormat implements IMetricValueFormat {
	private String format;
	
	public MetricValuePredefinedFormat(String sFormat) {
		this.format = sFormat;
	}
	
	
	public String format(MetricValue value, MetricValue rootValue) {
		Formatter format_str = null;
		String fmt = "";
		
		try {
			format_str = new Formatter();
			format_str.format(format, MetricValue.getValue(value));

			fmt = format_str.toString();
			format_str.close();

		} catch (java.util.IllegalFormatConversionException e) {
			System.err.println("Illegal format conversion: " + format.toString() + "\tFrom value: " + MetricValue.getValue(value));
			e.printStackTrace();
		} finally {
			format_str.close();
		}		
		return fmt;
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
