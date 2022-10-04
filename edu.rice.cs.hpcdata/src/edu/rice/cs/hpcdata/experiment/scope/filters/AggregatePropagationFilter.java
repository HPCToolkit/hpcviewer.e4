package edu.rice.cs.hpcdata.experiment.scope.filters;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.AggregateMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.FinalMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

/***********************
 * 
 * Filter metric to filter inclusive metrics and pre-aggregated metrics 
 *
 ***********************/
public class AggregatePropagationFilter extends InclusiveOnlyMetricPropagationFilter {

	public AggregatePropagationFilter(Experiment experiment) {
		super(experiment);
	}

	/****************
	 *  propagate only if the parent's diPropagation filter it and the metric is a type of aggregate
	 *  
	 *  @param source
	 *  @param target
	 *  @param indexSource
	 *  @param indexTarget
	 */
	@Override
	public boolean doPropagation(Scope source, 
								 Scope target, 
								 int indexSource,
								 int indexTarget) {
		boolean bParentResult = super.doPropagation(source, target, indexSource, indexTarget);
		if (!bParentResult) {
			BaseMetric m = this._experiment.getMetric(indexSource);
			bParentResult = (m instanceof FinalMetric) || (m instanceof AggregateMetric);
		}
		return bParentResult;
	}

}
