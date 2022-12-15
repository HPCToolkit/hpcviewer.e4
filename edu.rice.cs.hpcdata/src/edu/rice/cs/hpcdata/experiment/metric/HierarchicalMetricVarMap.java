package edu.rice.cs.hpcdata.experiment.metric;

public class HierarchicalMetricVarMap extends MetricVarMap 
{

	@Override
	public double getValue(String varName) {
		if (varName.equals("$$")) {
			var mv = getScope().getDirectMetricValue(getMetric().getIndex());
			if (mv == MetricValue.NONE)
				return 0.0f;
			return mv.getValue();
		}
		return super.getValue(varName);
	}
}
