package edu.rice.cs.hpctest.data.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class TestMetricValue 
{
	public static boolean testMetricValueCorrectness(Experiment exp, Scope parent, Scope context) {
		var mvc = context.getMetricValues();
		if (mvc == null)
			return true;
		
		var mvcParent = parent.getMetricValues();
		var metrics = exp.getMetricList();
		
		for(var metric: metrics) {
			var partner = exp.getMetric(metric.getPartner());
			var mv1 = mvc.getValue(context, metric);
			var mv2 = mvc.getValue(context, partner);
			
			// test the exclusive vs inclusive
			if (metric.getMetricType() == MetricType.INCLUSIVE) {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				assertTrue(c>=0);
			} else {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				assertTrue(c<=0);
			}
			
			// test comparison with the parent
			var pv1 = mvcParent.getValue(parent, metric);
			
			assertTrue(checkChildValue(metric, pv1, mv1));
		}
		
		// traverse all the children
		if (context.hasChildren()) {
			assertNotNull(context.getChildren());
			for(var child: context.getChildren()) {
				var result = testMetricValueCorrectness(exp, context, child);
				if (!result)
					return false;
			}
		}
		return true;
	}
	
	public static int floatCompare(float f1, float f2) {
		final float EPSILON = 0.001f;
		final float delta = f1 - f2;
		final float diffEps = Math.abs(delta) / f1;
		if (diffEps < EPSILON)
			return 0;
		else return (int) delta;
	}
	
	
	public static boolean checkChildValue(BaseMetric metric, MetricValue mv1, MetricValue mv2) {
		if (!(metric instanceof DerivedMetric) && 
			 (metric.getMetricType() == MetricType.INCLUSIVE)) { 
			final int  c = floatCompare(mv1.getValue(), mv2.getValue());
			return c>=0;
		}
		// exclusive metric: anything can happen
		return true;
	}

}
