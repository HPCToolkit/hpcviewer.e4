package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public abstract class AbstractCombineMetric  {

	protected void combine_internal(Scope target, Scope source,
			MetricValuePropagationFilter inclusiveOnly,
			MetricValuePropagationFilter exclusiveOnly) {

		if (target.isCounterZero() && inclusiveOnly != null) {
			target.safeCombine(source, inclusiveOnly);
		}
		if (exclusiveOnly != null)
			target.combine(source, exclusiveOnly);

		target.incrementCounter();
		
	}
	
	abstract public void combine(Scope target, Scope source,
			MetricValuePropagationFilter inclusiveOnly,
			MetricValuePropagationFilter exclusiveOnly);
}
