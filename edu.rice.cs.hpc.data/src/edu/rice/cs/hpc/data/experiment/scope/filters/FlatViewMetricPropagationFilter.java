package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

//when copying from the CCT to the flat view, don't copy metrics on LoopScopes and 
//ProcedureScopes. in the CCT, they have already been updated
//with inclusive values. in the flat view, they will be synthesized 
//fresh (from the data on LineScopes) to reflect metrics in the flat view.
public class FlatViewMetricPropagationFilter implements MetricValuePropagationFilter {
	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		if (source instanceof LoopScope) return false;
		if (source instanceof ProcedureScope) return false;
		if (source instanceof CallSiteScope)  return false; // should never get here
		return true;
	}
}