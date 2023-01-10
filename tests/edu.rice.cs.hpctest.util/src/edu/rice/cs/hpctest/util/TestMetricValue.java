package edu.rice.cs.hpctest.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class TestMetricValue 
{
	private TestMetricValue() { /* not used */ }
	
	public static boolean testMetricValueCorrectness(Experiment exp, Scope parent, Scope context) {
		var mvc = context.getMetricValues();
		if (mvc == null)
			return true;
		
		var mvcParent = parent.getMetricValues();
		var metrics = exp.getVisibleMetrics();
		
		for(var metric: metrics) {
			if (metric.getPartner() < 0)
				continue;
			
			var partner = exp.getMetric(metric.getPartner());
			var mv1 = mvc.getValue(context, metric);
			var mv2 = mvc.getValue(context, partner);
			
			// test the exclusive vs inclusive
			if (metric.getMetricType() == MetricType.INCLUSIVE) {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				assertTrue(c>=0);
			} else if (metric.getMetricType() == MetricType.EXCLUSIVE){
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

	
	public static void testTreMetriceCorrectnes(List<BaseMetric> metrics, Scope root) {
		
		var children = root.getChildren();
		if (children.isEmpty())
			return;
		
		Map<Integer, Float> totalValues = new HashMap<>();
		
		for (var child: children) {
			testMetricCorrectness(metrics, child, totalValues);
		}
	}
	
	private static void testMetricCorrectness(List<BaseMetric> metrics, Scope scope, Map<Integer, Float> values) {
		
		for(var metric: metrics) {
			var mv = metric.getValue(scope);
			
			var root = scope.getRootScope();
			var rootMV = metric.getValue(root);
			var rootValue = rootMV == MetricValue.NONE ? 0.0f : rootMV.getValue();

			if (metric.getMetricType() == MetricType.EXCLUSIVE) {
				var id = metric.getIndex();
				float value = mv == MetricValue.NONE ? 0 : mv.getValue();
				if (!scope.hasChildren() && 
					!(scope instanceof CallSiteScope)   && 
					!(scope instanceof InstructionScope) &&
					!(scope instanceof ProcedureScope)) {
					values.compute(id, (k, v) -> v == null ? value : value + v);
				}
				var currentValue = values.computeIfAbsent(id, v -> 0.0f);
				assertTrue(rootValue >= currentValue);
			} else if (rootValue > 0) {
				// inclusive
				var delta = floatCompare(rootValue, mv.getValue());
				assertTrue(delta >= 0);
			}
		}
		
		if (!scope.hasChildren())
			return;
		
		var children = scope.getChildren();
		if (children.isEmpty())
			return;
		
		for(var child: children) {
			testMetricCorrectness(metrics, child, values);
		}
	}
}
