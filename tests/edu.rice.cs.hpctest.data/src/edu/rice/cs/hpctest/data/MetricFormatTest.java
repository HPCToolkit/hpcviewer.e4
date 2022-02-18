package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.metric.Metric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.metric.format.IMetricValueFormat;

public class MetricFormatTest {



	@Test
	public void testFormat() {
		
		Metric baseMetric = new Metric("sn", "nn", "dn",
				VisibilityType.SHOW, null, AnnotationType.PERCENT, "", 0, MetricType.INCLUSIVE, 1);
		
		double []values = {9.999, 0.999, 9.55,     9.5,       0.9999, 0.945,
				           0.95,  0.955, 0.9992,   100000001, 123456789.123456789 };
		double []annots = {.9999, 0.009, 0.000009, -0.000004, 9.9945,  0.9991234,
				           0.001, 0.009, 0.000001, 0.9999456,  0.0001};
		
		for(int i=0; i<values.length; i++) {
			MetricValue mv = new MetricValue(values[i], annots[i]);
			
			IMetricValueFormat format = baseMetric.getDisplayFormat();
			String displayFormat = format.format(mv);
			
			// System.out.println(i + ": " + values[i] + " (" + annots[i] + ") \t= '" + displayFormat +"' ");
			// output:
			/*
0: 9.999 (0.9999) 	= '1.00e+01 100.0%' 
1: 0.999 (0.009) 	= '9.99e-01   0.9%' 
2: 9.55 (9.0E-6) 	= '9.55e+00   0.0%' 
3: 9.5 (-4.0E-6) 	= '9.50e+00  -0.0%' 
4: 0.9999 (9.9945) 	= '1.00e+00 999.5%' 
5: 0.945 (0.9991234) 	= '9.45e-01  99.9%' 
6: 0.95 (0.001) 	= '9.50e-01   0.1%' 
7: 0.955 (0.009) 	= '9.55e-01   0.9%' 
8: 0.9992 (1.0E-6) 	= '9.99e-01   0.0%' 
9: 1.00000001E8 (0.9999456) 	= '1.00e+08 100.0%' 
10: 1.2345678912345679E8 (1.0E-4) 	= '1.23e+08   0.0%' 
			 */
			assertTrue(displayFormat.length()==15);
		}
		assertTrue(true);
	}

}
