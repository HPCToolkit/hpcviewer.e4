package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.scope.filters.MetricValuePropagationFilter;


/*************
 * Interface for a scope class to merge with another scope
 * The implementer of this interface should carefully compute the children of the merged scope
 * 
 * @author laksonoadhianto
 *
 *************/
public interface IMergedScope {
	
	/******
	 * INIT: initialization phase
	 * INCREMENTAL: one phase after initialization which incrementally create and merge scopes
	 */
	static public enum MergingStatus {INIT, INCREMENTAL}
	
	/******
	 * Return the children of the current scope which can be merged previously.
	 * 
	 * @param finalizeVisitor
	 * @param percentVisitor
	 * @param inclusiveOnly
	 * @param exclusiveOnly
	 * @return
	 */
	public Object[] getAllChildren(	MetricValuePropagationFilter inclusiveOnly, 
									MetricValuePropagationFilter exclusiveOnly );
	
	public boolean hasScopeChildren();
}
