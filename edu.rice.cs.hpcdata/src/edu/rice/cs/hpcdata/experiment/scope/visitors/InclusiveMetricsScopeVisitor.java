package edu.rice.cs.hpcdata.experiment.scope.visitors;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.filters.MetricValuePropagationFilter;

/**
 * Visitor class to accumulate metrics
 * This class is used to compute inclusive and exclusive cost for :
 *  - CCT when filter are InclusiveOnlyMetricPropagationFilter and ExclusiveOnlyMetricPropagationFilter
 *  - FT when filter is FlatViewInclMetricPropagationFilter
 *  
 *
 */
public class InclusiveMetricsScopeVisitor extends AbstractInclusiveMetricsVisitor {
	private int numberOfPrimaryMetrics;

	public InclusiveMetricsScopeVisitor(Experiment experiment, MetricValuePropagationFilter filter) {
		super(filter);
		this.numberOfPrimaryMetrics = experiment.getMetricCount();

	}



	/**
	 * Method to accumulate the metric value from the child to the parent
	 * @param parent
	 * @param source
	 */
	protected void accumulateToParent(Scope parent, Scope source) {
		parent.accumulateMetrics(source, this.filter, this.numberOfPrimaryMetrics);
	}
}
