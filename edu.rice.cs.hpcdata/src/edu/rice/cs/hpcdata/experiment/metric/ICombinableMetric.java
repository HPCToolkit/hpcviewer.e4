package edu.rice.cs.hpcdata.experiment.metric;

import edu.rice.cs.hpcdata.experiment.scope.Scope;


/************************************************************
 * 
 * Interface for metrics that can be combined with other context.
 * <p>
 * The implementation should handle how to combine metric values
 * from one context to another.
 * </p>
 * 
 ************************************************************/
public interface ICombinableMetric 
{
	/***
	 * Combine the metric value from the source to the target
	 * 
	 * @param scopeTarget
	 * 			The target context
	 * @param scopeSource
	 * 			The source context
	 */
	public void combine(Scope scopeTarget, Scope scopeSource);
}
