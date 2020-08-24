package edu.rice.cs.hpc.data.experiment.metric.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;

public class SimpleMetricValueFormat implements IMetricValueFormat 
{
	private final String Exponent = "e";
	private final String Exponent_Plus  = Exponent + "+";
	private final String Exponent_Minus = Exponent + "-";
	
	private DecimalFormat formatValue;
	private DecimalFormat formatPercent;
	
	public SimpleMetricValueFormat() {

		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setExponentSeparator(Exponent);
		formatValue = new DecimalFormat("0.00E00");
		formatValue.setDecimalFormatSymbols(dfs);
		
		formatPercent = new DecimalFormat("0.0%");
	}

	@Override
	public String format(MetricValue value) {
		
		String txtValue  = formatValue.format(value.getValue());
		if (!txtValue.contains(Exponent_Minus)) {
			txtValue = txtValue.replace(Exponent, Exponent_Plus);
		}
		String txtAnn  = formatPercent.format(value.getAnnotationValue());
		String paddAnn = String.format(" %6s", txtAnn); 
		
		return txtValue +  paddAnn;
	}

}
