package edu.rice.cs.hpcdata.experiment.scope.filters;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

//only propagagate inclusive metrics
public class InclusiveOnlyMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected Experiment _experiment;

	public InclusiveOnlyMetricPropagationFilter(Experiment experiment) {
		this._experiment = experiment;
	}

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		//-------------------------------------------------------------------------
		// This part looks suspicious: the target (flat) will never be the same as the 
		// 		source (cct). So far there is no proof that there is a path that return false
		//-------------------------------------------------------------------------
		if ((source instanceof LineScope)) {
		   Scope parent = source.getParentScope();
		   if ((parent != null) && (parent instanceof CallSiteScope)) {
                     if ((target != null) && (target == parent.getParentScope()))
                    	 return false;
		   }
		}
		return ( _experiment.getMetric(src_idx).getMetricType() == MetricType.INCLUSIVE );
	}
}