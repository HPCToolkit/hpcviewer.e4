package edu.rice.cs.hpcdata.experiment.metric.format;

public class MetricValueFormatFactory 
{
	private MetricValueFormatFactory() {
		// unused
	}
	
	public static MetricValueFormat createFormatDefault()
	{
		return new 
				MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, false, 0, 0, 0, null, 1);
	}

	public static MetricValueFormat createFormatPercent()
	{
		return new 
				MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, true, FormatStyle.FIXED, 5, 1, "#0.0%", 1);
	}

	public static MetricValueFormat createFormatProcess()
	{
		return new 
				MetricValueFormat(true, FormatStyle.FLOAT, 8, 2, true, FormatStyle.FIXED, 5, 1, "<0>", 1);
	}

	public static MetricValueFormat createFormat(
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
