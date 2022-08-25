package edu.rice.cs.hpcdata.experiment.metric.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import edu.rice.cs.hpcdata.experiment.metric.MetricValue;


/****************************************************************
 * 
 * A simple implementation of {@code IMetricValueFormat} with format:
 * <pre>0.00E00 0.0% </pre>
 * The format allocates spaces:
 * <ul>
 * <li>8 spaces for metric value: x.xxe+xx
 * <li>6 spaces for annotation  : xxx.x%
 * </ul>
 * <b>Warning</b>:
 * This class only assumes the annotation to be percent. It doesn't work 
 * with other types of annotation.
 * 
 ****************************************************************/
public class SimpleMetricValueFormat implements IMetricValueFormat 
{
	private static final String Exponent = "e";
	private static final String Exponent_Plus  = Exponent + "+";
	private static final String Exponent_Minus = Exponent + "-";
	
	private final DecimalFormat formatValue;
	private final DecimalFormat formatPercent;
	
	private static SimpleMetricValueFormat Instance;
	
	public SimpleMetricValueFormat() {
		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setExponentSeparator(Exponent);
		formatValue = new DecimalFormat("0.00E00");
		formatValue.setDecimalFormatSymbols(dfs);
		
		formatPercent = new DecimalFormat("##0.0%");
	}
	
	public static SimpleMetricValueFormat getInstance() {
		if (Instance == null) {
			Instance = new SimpleMetricValueFormat();
		}
		return Instance;
	}

	@Override
	public String format(MetricValue value, MetricValue rootValue) {
		
		String txtValue  = formatValue.format(value.getValue());
		if (!txtValue.contains(Exponent_Minus)) {
			txtValue = txtValue.replace(Exponent, Exponent_Plus);
		}
		String paddAnn = "";
		if (rootValue != MetricValue.NONE && rootValue != null) {
			String txtAnn  = formatPercent.format(value.getValue()/rootValue.getValue());
			paddAnn = String.format(" %6s", txtAnn); 
		}
		
		return txtValue +  paddAnn;
	}

}
