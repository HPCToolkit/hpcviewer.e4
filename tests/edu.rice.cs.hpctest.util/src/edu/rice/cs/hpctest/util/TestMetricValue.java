package edu.rice.cs.hpctest.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Precision;

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
	static final float EPSILON = 1f;

	private TestMetricValue() { /* not used */ }
	
	public static boolean testMetricValueCorrectness(Experiment exp, Scope parent, Scope context) {
		var mvc = context.getMetricValues();
		if (mvc == null)
			return true;
		
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
				
				if (!(metric instanceof DerivedMetric)) {
					
					// test comparison with the parent
					var parentVal = getValue(metric, parent);
					var childVal = getValue(metric, context);
					
					var delta = floatCompare(parentVal, childVal);
					
					assertTrue(delta >= 0);
				}

			} else if (metric.getMetricType() == MetricType.EXCLUSIVE){
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				assertTrue(c<=0);
			}
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
		return Precision.compareTo(f1, f2, EPSILON * Math.abs(f1-f2));
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

	
	private  static float getValue(BaseMetric metric, Scope scope) {
		var mv = metric.getValue(scope);
		if (mv == MetricValue.NONE)
			return 0.0f;
		
		return mv.getValue();
	}
	
	public static void testSortedMetricCorrectness(BaseMetric metric, Scope scope1, Scope scope2) {
		if (scope1 == null || scope2 == null)
			return;
		
		var val1 = getValue(metric, scope1);
		var val2 = getValue(metric, scope2);
		
		var delta = floatCompare(val1, val2);
		assertTrue(delta >= 0);
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
			var value = getValue(metric, scope);
			
			var root = scope.getRootScope();
			var rootMV = metric.getValue(root);
			var rootValue = rootMV == MetricValue.NONE ? 0.0f : rootMV.getValue();

			if (metric.getMetricType() == MetricType.EXCLUSIVE) {
				var id = metric.getIndex();
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
				var delta = floatCompare(rootValue, value);
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
