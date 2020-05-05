package edu.rice.cs.hpc.data.experiment.metric.format;

public class MetricValueFormatFactory 
{
/*	final static private MetricValueFormat FormatDefault = new 
			MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, false, 0, 0, 0, null, 1);
	final static private MetricValueFormat FormatPercent = new 
			MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, true, FormatStyle.FIXED, 5, 1, "#0.0%", 1);
	final static private MetricValueFormat FormatProcess = new 
			MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, true, FormatStyle.FIXED, 5, 0, "<0>", 1);
*/
	static public MetricValueFormat createFormatDefault()
	{
		return new 
				MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, false, 0, 0, 0, null, 1);
	}

	static public MetricValueFormat createFormatPercent()
	{
		return new 
				MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, true, FormatStyle.FIXED, 5, 1, "#0.0%", 1);
	}

	static public MetricValueFormat createFormatProcess()
	{
		return new 
				MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, true, FormatStyle.FIXED, 5, 1, "<0>", 1);
	}

	static public MetricValueFormat createFormat(
			boolean annotationShow, 
			int annotationKind,
			int annotationFieldWidth,
			int annotationFractionDigits,
			String annotationFormatPattern)	{
		
		return new MetricValueFormat(true, FormatStyle.FLOAT, 8, 2,
				annotationShow, annotationKind, annotationFieldWidth,
				annotationFractionDigits, annotationFormatPattern, 1);
	}
}
