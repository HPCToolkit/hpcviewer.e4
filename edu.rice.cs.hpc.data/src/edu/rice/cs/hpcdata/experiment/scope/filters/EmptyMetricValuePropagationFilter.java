package edu.rice.cs.hpcdata.experiment.scope.filters;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class EmptyMetricValuePropagationFilter implements
		MetricValuePropagationFilter {

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) { 
		return true; 
	}

}
