package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class EmptyMetricValuePropagationFilter implements
		MetricValuePropagationFilter {

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) { 
		return true; 
	}

}
