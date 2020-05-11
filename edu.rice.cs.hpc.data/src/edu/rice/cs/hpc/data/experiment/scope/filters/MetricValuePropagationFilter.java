package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

public interface MetricValuePropagationFilter {
	public abstract boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx);
}