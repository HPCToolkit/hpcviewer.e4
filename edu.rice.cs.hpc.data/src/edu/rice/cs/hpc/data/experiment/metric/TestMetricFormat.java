/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpc.data.experiment.metric.format.IMetricValueFormat;

/**
 * Manual unit test for metric format
 */
public class TestMetricFormat {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Metric baseMetric = new Metric("sn", "nn", "dn",
				VisibilityType.SHOW, null, AnnotationType.PERCENT, "", 0, MetricType.INCLUSIVE, 1);
		
		double []values = {9.999, 0.999, 9.55,     9.5,       0.9999, 0.945,
				           0.95,  0.955, 0.9992,   100000001, 123456789.123456789 };
		double []annots = {.9999, 0.009, 0.000009, -0.000004, 9.9945,  0.9991234,
				           0.001, 0.009, 0.000001, 0.9999456,  0.0001};
		
		System.out.println("No:  Value   (Percent)  =  display_format");
		for(int i=0; i<values.length; i++) {
			MetricValue mv = new MetricValue(values[i], annots[i]);
			
			IMetricValueFormat format = baseMetric.getDisplayFormat();
			System.out.println(i + ": " + values[i] + " (" + annots[i] + ") \t= '" + format.format(mv) +"' ");
		}
	}
}
