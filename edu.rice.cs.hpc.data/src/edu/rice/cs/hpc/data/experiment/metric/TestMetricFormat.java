/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.VisibilityType;

/**
 * @author laksonoadhianto
 *
 */
public class TestMetricFormat {
	
	static String getCustomFormat(float value, float annotation) {
		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setExponentSeparator("e");
		DecimalFormat df = new DecimalFormat("0.00E00");
		df.setDecimalFormatSymbols(dfs);
		
		DecimalFormat pf = new DecimalFormat("0.0%");
		
		String txtValue  = df.format(value);
		if (!txtValue.contains("e-")) {
			txtValue = txtValue.replace("e", "e+");
		}
		String txtAnn  = pf.format(annotation);
		String paddAnn = String.format(" %6s", txtAnn); 
		
		return txtValue +  paddAnn;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Metric baseMetric = new Metric("sn", "nn", "dn",
				VisibilityType.SHOW, null, AnnotationType.PERCENT, "", 0, MetricType.INCLUSIVE, 1);
		
		// test 1: 9.999 should be displayed as 1.0e+01 
		MetricValue mv = new MetricValue(9.999, .999);
		// test 2: 0.999 should be displayed as 9.99e-1
		MetricValue mv2 = new MetricValue(0.999, .009);
		MetricValue mv3 = new MetricValue(9.55, 0.0009);
		MetricValue mv4 = new MetricValue(9.5, -0.000005);
		MetricValue mv5 = new MetricValue(0.9999, 0.9945);
		MetricValue mv6 = new MetricValue(0.945, 0.99555);
		MetricValue mv7 = new MetricValue(0.95, 0.99995);
		MetricValue mv8 = new MetricValue(0.955, -0.095005);
		MetricValue mv9 = new MetricValue(0.9992, 0.900);
		
		System.out.println("test 1: " + mv.value  + "\t(" + mv.annotation  + ") \t= '" + baseMetric.getDisplayFormat().format(mv) +"'" + "\t" + getCustomFormat(mv.value, mv.annotation));
		System.out.println("test 2: " + mv2.value + "\t(" + mv2.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv2)+"'" + "\t" + getCustomFormat(mv2.value, mv2.annotation));
		System.out.println("test 3: " + mv3.value + "\t(" + mv3.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv3)+"'" + "\t" + getCustomFormat(mv3.value, mv3.annotation));
		System.out.println("test 4: " + mv4.value + "\t(" + mv4.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv4)+"'" + "\t" + getCustomFormat(mv4.value, mv4.annotation));
		System.out.println("test 5: " + mv5.value + "\t(" + mv5.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv5)+"'" + "\t" + getCustomFormat(mv5.value, mv5.annotation));
		System.out.println("test 6: " + mv6.value + "\t(" + mv6.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv6)+"'" + "\t" + getCustomFormat(mv6.value, mv6.annotation));
		System.out.println("test 7: " + mv7.value + "\t(" + mv7.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv7)+"'" + "\t" + getCustomFormat(mv7.value, mv7.annotation));
		System.out.println("test 8: " + mv8.value + "\t(" + mv8.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv8)+"'" + "\t" + getCustomFormat(mv8.value, mv8.annotation));
		System.out.println("test 9: " + mv9.value + "\t(" + mv9.annotation + ") \t= '"+baseMetric.getDisplayFormat().format(mv9)+"'" + "\t" + getCustomFormat(mv9.value, mv9.annotation));
	}

}
