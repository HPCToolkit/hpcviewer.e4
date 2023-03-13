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
	static final float EPSILON = 0.1f;

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
				int c = compareValue(mv1.getValue(), mv2.getValue());
				assertTrue(c>=0);

			} else if (metric.getMetricType() == MetricType.EXCLUSIVE){
				int c = compareValue(mv1.getValue(), mv2.getValue());
				assertTrue(c<=0);
			}

			testParentChildValue(metric, parent, context);
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

	public static int compareValue(double f1, double f2) {
		// the epsilon here is very relaxed:
		// Instead of using Math.ulp, we define our own relative epsilon
		final double epsilon = EPSILON * (Math.max(f2, f1));
		return Precision.compareTo(f1, f2, epsilon);
	}


	/***
	 * Check if the child value is less or equal to the parent.
	 * Only inclusive metrics are tested.
	 *
	 * @param metric
	 * @param parent
	 * @param context
	 */
	private static void testParentChildValue(BaseMetric metric, Scope parent, Scope context) {
		if (metric.getMetricType() != MetricType.INCLUSIVE)
			return;

		if (!(metric instanceof DerivedMetric)) {

			// test comparison with the parent
			var parentVal = getValue(metric, parent);
			var childVal = getValue(metric, context);

			var delta = compareValue(parentVal, childVal);

			assertTrue(delta >= 0);
		}
	}

	private  static double getValue(BaseMetric metric, Scope scope) {
		var mv = metric.getValue(scope);
		if (mv == MetricValue.NONE)
			return 0.0;

		return mv.getValue();
	}

	public static void testSortedMetricCorrectness(BaseMetric metric, Scope scope1, Scope scope2) {
		if (scope1 == null || scope2 == null)
			return;

		var val1 = getValue(metric, scope1);
		var val2 = getValue(metric, scope2);

		var delta = compareValue(val1, val2);
		assertTrue(delta >= 0);
	}

	public static void testTreMetriceCorrectnes(List<BaseMetric> metrics, Scope root) {

		var children = root.getChildren();
		if (children.isEmpty())
			return;

		Map<Integer, Double> totalValues = new HashMap<>();

		for (var child: children) {
			testMetricCorrectness(metrics, child, totalValues);
		}
	}

	private static void testMetricCorrectness(List<BaseMetric> metrics, Scope scope, Map<Integer, Double> values) {

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
				var currentValue = values.computeIfAbsent(id, v -> 0.0d);
				assertTrue(rootValue >= currentValue);
			} else if (rootValue > 0) {
				// inclusive
				var delta = compareValue(rootValue, value);
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
