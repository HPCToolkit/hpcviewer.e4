package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.Iterator;
import java.util.List;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.GroupScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcdata.filter.IFilterData;
import edu.rice.cs.hpcdata.util.ICallPath;


/******************************************************************
 * 
 * Visitor class to filter a CCT tree and generate a new filtered tree
 * This class is designed only for CCT, and it definitely doesn't work
 * with callers tree and flat tree.<br/>
 * <br/>
 * To generate a filtered caller tree (or flat tree) one need to 
 * create first a filtered CCT and then transform it to callers tree
 * (or flat tree) 
 *
 ******************************************************************/
public class FilterScopeVisitor implements IScopeVisitor 
{
	static public final int STATUS_INIT=0, STATUS_OK = 1, STATUS_FAKE_PROCEDURE = 2; 
	
	private final IFilterData filter;
	private final IExperiment experiment;
	private List<BaseMetric> metrics = null;
	
	/**** flag to allow the dfs to continue to go deeper or not.  
	      For inclusive filter, we should stop going deeper      *****/
	private boolean need_to_continue;

	private int num_scope_filtered = 0;
	private int filterStatus 	   = STATUS_INIT;
	private int current_depth;
	private int max_depth;
	
	/***********
	 * Constructor to filter a cct
	 * 
	 * @param rootFilter : the main root for filter tree 
	 * @param rootOriginalCCT : the original cct tree
	 * @param filter : filter map to filter a string
	 */
	public FilterScopeVisitor(RootScope rootOriginalCCT, IFilterData filter)
	{
		this.filter 		  = filter;
		rootOriginalCCT.getMetricValues();
		need_to_continue 	  = true;
		current_depth = 0;
		max_depth = 0;
		
		experiment = rootOriginalCCT.getExperiment();
		if (experiment instanceof Experiment)
		{
			metrics = ((Experiment)experiment).getMetricList();
		}
	}
	
	/**************
	 * return a flag whether the caller needs to dig deeper to their descendants or not
	 * 
	 * @return true if one needs to continue to walk into the descendant, false otherwise.
	 */
	public boolean needToContinue()
	{
		return need_to_continue;
	}
	
	
	/****
	 * Retrieve the minimum omitted scopes due to filtering
	 * @return int
	 */
	public int numberOfFilteredScopes() 
	{
		return num_scope_filtered;
	}
	
	
	/***
	 * Retrieve the current filter status
	 * 
	 * @return STATUS_INIT, STATUS_OK, STATUS_FAKE_PROCEDURE
	 */
	public int getFilterStatus() 
	{
		return filterStatus;
	}
	
	
	/****
	 * Retrieve the possibly new maximum depth 
	 * @return int
	 */
	public int getMaxDepth() {
		return max_depth;
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(RootScope scope, 				ScopeVisitType vt) { 
		if (scope.getType() != RootScopeType.Invisible)	
			mergeInsert(scope, vt);
	}
	public void visit(LoadModuleScope scope, 		ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(FileScope scope, 				ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(GroupScope scope, 			ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(Scope scope, 					ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(CallSiteScope scope, 			ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(ProcedureScope scope, 		ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(LoopScope scope, 				ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(StatementRangeScope scope, 	ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(LineScope scope, 				ScopeVisitType vt) { mergeInsert(scope, vt); }

	/*******
	 * Filter the scope (if it matches one of the filter pattern), and remove also
	 * the descendants if necessary
	 * 
	 * @param scope
	 * @param vt
	 * 
	 * @return boolean true if the scope itself has been removed, false otherwise
	 */
	private boolean mergeInsert(Scope scope, ScopeVisitType vt) {

		if (vt == ScopeVisitType.PreVisit) {
			// Previsit
			if (isScopeTrace(scope)) {
				current_depth++;
				max_depth = Math.max(max_depth, current_depth);
			} 
			FilterAttribute filterAttribute = filter.getFilterAttribute(scope.getName());
			if (filterAttribute != null)
			{
				if (filterStatus == STATUS_INIT) {
					filterStatus = STATUS_OK;
				}
				num_scope_filtered++;
				need_to_continue = (filterAttribute.filterType == FilterAttribute.Type.Self_Only);

				if (filterAttribute.filterType == FilterAttribute.Type.Descendants_Only)
				{
					//-------------------------------------------------------------------
					// merge with the metrics of the children
					//-------------------------------------------------------------------
					if (metrics != null)
					{
						// glue the metrics of all the children to the scope
						for(Object child: scope.getChildren())
						{
							if (!(child instanceof LineScope))
							{
								Scope child_scope = (Scope) child;
								mergeMetrics(scope, child_scope, false);
							}
						}
					}
					//-------------------------------------------------------------------
					// Filtering only the children, not the scope itself.
					// remove all the children
					// We need to remove the child using the iterator's remove method
					// to avoid ConcurrentModificationException 
					//-------------------------------------------------------------------
					var iterator = scope.getChildren().iterator();
					while (iterator.hasNext())
					{
						var child = iterator.next();
						removeNode(iterator, (Scope) child, filterAttribute.filterType);
					}
				} else
				{
					//-------------------------------------------------------------------
					// Filtering the scope or/and the children
					//-------------------------------------------------------------------
					if (metrics  != null)
					{
						if (!(scope instanceof LineScope))
						{
							// no need to merge metric if the filtered child is a line statement.
							// in this case, the parent (PF) already includes the exclusive value.
							Scope parent = scope.getParentScope();
							mergeMetrics(parent, scope, need_to_continue);
						}
					}
					removeChild(null, scope, vt, filterAttribute.filterType);
				}
			} else 
			{
				// filter is not needed, we can surely continue to investigate the descendants
				need_to_continue = true;
			}	
		} else 
		{ // PostVisit
			if (isScopeTrace(scope)) {
				current_depth--;
			}
		}
		return need_to_continue;
	}
	
	
	private boolean isScopeTrace(Scope scope) {
		return (scope instanceof ProcedureScope || scope instanceof CallSiteScope) ;
	}
	
	
	/********
	 * Remove a child from its parent. if the filter type is SELF, we'll attach the grandchildren
	 * to the parent
	 * 
	 * @param iterator
	 * 			The current iterator to remove the child. If the iterator is null, we remove it from the parent.
	 * @param childToRemove 
	 * 			scope to remove
	 * @param filterType 
	 * 			filter type
	 * 
	 * @see FilterAttribute.Type
	 */
	private void removeChild(Iterator<Scope> iterator, Scope childToRemove, ScopeVisitType vt, FilterAttribute.Type filterType)
	{
		// skip to current scope
		Scope parent = removeNode(iterator, childToRemove, filterType);
		
		// remove its children and glue it the parent
		if (filterType == FilterAttribute.Type.Self_Only)
		{
			addGrandChildren(parent, vt, childToRemove);
		}
	}
	

	/****
	 * Remove a child node from the parent, and propagate the trace ID to the parent
	 * 
	 * @param iterator
	 * 			The current iterator to remove the child. If the iterator is null, we remove it from the parent.
	 * @param child
	 * 			The child node to be removed
	 * @param filterType
	 * 			The type of the filter
	 * @return Scope
	 * 			The parent node
	 * 
	 * @see FilterAttribute.Type
	 */
	private Scope removeNode(Iterator<Scope> iterator, Scope child, FilterAttribute.Type filterType) {
		// 1. remove the child node
		Scope parent = child.getParentScope();
		if (iterator == null)
			parent.remove(child);
		else
			iterator.remove();

		// 2. move the trace call-path id (if exist) to the parent
		propagateTraceID(parent, child, filterType);

		// 3. clear the child node
		child.setParentScope(null);
		
		return parent;
	}
	
	
	private void propagateTraceID(Scope parent, Scope child, FilterAttribute.Type filterType) {
		if (filterType == FilterAttribute.Type.Self_Only)
			return;
		
		// children have been removed
		// copy the cpid to the parent
		CallPathTraceVisitor cptv = new CallPathTraceVisitor();
		//cptv.map = experiment.getScopeMap();
		cptv.parent_scope = parent;
		cptv.parent_depth = current_depth-1;
		
		//child.dfsVisitScopeTree(cptv);

	}
	
	
	
	/*****
	 * Add the grand children to the parent
	 * @param parent
	 * @param scope_to_remove
	 */
	private void addGrandChildren(Scope parent, ScopeVisitType vt, Scope scope_to_remove)
	{
		var children = scope_to_remove.getChildren();
		if (children != null)
		{
			for(Object child : children)
			{
				Scope child_scope = (Scope) child;
				parent.addSubscope(child_scope);
				child_scope.setParentScope(parent);
			}
		}
	}
	
	/******
	 * Merging metrics
     * X : exclusive metric value
     * I : Inclusive metric value
     * 
     * Exclusive filter
     * Xp <- For all filtered i, sum Xi
     * Ip <- Ip (no change)
     * 
     * Inclusive filter
     * Xp <- For all filtered i, sum Ii
     * Ip <- Ip (no change)
	 *
	 * @param parent : the parent scope
	 * @param child  : the child scope to be excluded  
	 * @param exclusive_filter : whether to merge exclusive only (true) or inclusive metric
	 * 	to the exclusive metric of the parent
	 */
	private void mergeMetrics(Scope parent, Scope child, boolean exclusive_filter)
	{
		if (parent instanceof ProcedureScope) {
			if (((ProcedureScope)parent).isFalseProcedure() ) {
				filterStatus = STATUS_FAKE_PROCEDURE;
			}
		}
		// we need to merge the metric values
		IMetricValueCollection values = child.getMetricValues();
		for (BaseMetric metric: metrics)
		{
			MetricValue childValue = values.getValue(child, metric);
			if (childValue == MetricValue.NONE) {
				// in the original hpcview (2002), we assign the -1 value as the "none existence value"
				// this is not proper. we should assign as null for the non-existence
				// special case: every time the child has "none" value we can skip it instead of
				//  merging to the parent since x - 1 is not the same as x - 0
				continue;
			}
			if (exclusive_filter && metric.getMetricType() == MetricType.EXCLUSIVE)
			{
				MetricValue value = parent.getMetricValue(metric);
				if (value != MetricValue.NONE)
				  value = value.duplicate();
				parent.setMetricValue(metric.getIndex(), value);
				
				// exclusive filter: merge the exclusive metrics to the parent's exclusive
				mergeMetricToParent(parent, metric.getIndex(), childValue);
				
			} else if (!exclusive_filter && metric.getMetricType() == MetricType.INCLUSIVE)
			{
				// inclusive filter: merge the inclusive metrics to the parent's exclusive
				int index_exclusive_metric = metric.getPartner();
				BaseMetric metric_exc = ((Experiment)experiment).getMetric(index_exclusive_metric);
				mergeMetricToParent(parent, metric_exc.getIndex(), childValue);
			}
		}
	}
	
	/*******
	 * merge a metric value to the parent 
     *
	 * @param root
	 * @param target
	 * @param metric_exclusive_index
	 * @param mvChild
	 */
	private void mergeMetricToParent(Scope target, 
			int metric_exclusive_index, MetricValue mvChild)
	{
		// corner case: we shouldn't modify the value of the root.
		// they are supposed to be constant, unless it's a derived metric :-(
		if (target instanceof RootScope)
			return;
		
		MetricValue mvParentExc = target.getMetricValue(metric_exclusive_index);
		float value = 0;
		if (mvParentExc.getValue() >= 0) {
			// Initialize with the original value if it has a value (otherwise the value is -1)
			value = mvParentExc.getValue();
		}
		// update the filtered value
		value             += mvChild.getValue();
		
		MetricValue mv    = new MetricValue(value);
		target.setMetricValue(metric_exclusive_index, mv);
	}
	
	
	private static class CallPathTraceVisitor implements IScopeVisitor
	{
		Scope parent_scope;
		int parent_depth;
		ICallPath callpathInfo;
		
		//----------------------------------------------------
		// visitor pattern instantiations for each Scope type
		//----------------------------------------------------
		public void visit(RootScope scope, 				ScopeVisitType vt) { update(scope, vt); }
		public void visit(LoadModuleScope scope, 		ScopeVisitType vt) { update(scope, vt); }
		public void visit(FileScope scope, 				ScopeVisitType vt) { update(scope, vt); }
		public void visit(GroupScope scope, 			ScopeVisitType vt) { update(scope, vt); }
		public void visit(Scope scope, 					ScopeVisitType vt) { update(scope, vt); }
		public void visit(CallSiteScope scope, 			ScopeVisitType vt) { update(scope, vt); }
		public void visit(ProcedureScope scope, 		ScopeVisitType vt) { update(scope, vt); }
		public void visit(LoopScope scope, 				ScopeVisitType vt) { update(scope, vt); }
		public void visit(StatementRangeScope scope, 	ScopeVisitType vt) { update(scope, vt); }
		public void visit(LineScope scope, 				ScopeVisitType vt) { update(scope, vt); }

		private void update(Scope scope, ScopeVisitType vt) {
			if (vt == ScopeVisitType.PreVisit) {
				int cpid = scope.getCpid();
				if (cpid >= 0) {
					callpathInfo.addCallPath(cpid, parent_scope, parent_depth);
				}
			}
		}
	}
}
