package edu.rice.cs.hpcdata.experiment.scope.filters;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

//only propagagate exclusive metrics
public class ExclusiveOnlyMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected Experiment _experiment;

	public ExclusiveOnlyMetricPropagationFilter(Experiment experiment) {
		this._experiment = experiment;
	}

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		BaseMetric m = this._experiment.getMetric(src_idx);
		MetricType objType = m.getMetricType();
		
		// issue #251 and #253 support for derived metrics in meta.db
		// In this new database the exclusive can be either function scope (exclusive)
		// or lexical aware (custom exclusive)
		return ( objType == MetricType.EXCLUSIVE || objType == MetricType.LEXICAL_AWARE); 
	}
}