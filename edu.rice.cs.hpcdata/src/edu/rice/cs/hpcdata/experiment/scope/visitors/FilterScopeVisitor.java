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
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
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
	public static final int STATUS_INIT=0, STATUS_OK = 1, STATUS_FAKE_PROCEDURE = 2; 
	
	private final IFilterData filter;
	private final IExperiment experiment;
	private final ICallPath   callPathTraces;
	
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
			Experiment exp = (Experiment) experiment;
			metrics        = exp.getMetricList();
			callPathTraces = exp.getScopeMap();
		} else {
			callPathTraces = null;
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
						for(var child: scope.getChildren())
						{
							if (!(child instanceof LineScope))
							{
								mergeMetrics(scope, child, false);
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
						removeNode(iterator, child, filterAttribute.filterType);
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
			addGrandChildren(parent, childToRemove);
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
		if (experiment.getTraceAttribute().dbTimeMax > 0)
			propagateTraceID(parent, child, filterType);

		// 3. clear the child node
		child.setParentScope(null);
		
		return parent;
	}
	
	
	private void propagateTraceID(Scope parent, Scope child, FilterAttribute.Type filterType) {
		if (filterType == FilterAttribute.Type.Self_Only) {
			callPathTraces.replaceCallPath(child.getCpid(), parent, current_depth);
			return;
		}
		
		// children have been removed
		// copy the cpid to the parent
		CallPathTraceVisitor cptv = new CallPathTraceVisitor();
		cptv.parent_scope = parent;
		cptv.callpathInfo = callPathTraces;
		cptv.parent_depth = current_depth-1;
		
		child.dfsVisitScopeTree(cptv);
	}
	
	
	
	/*****
	 * Add the grand children to the parent
	 * @param parent
	 * @param scope_to_remove
	 */
	private void addGrandChildren(Scope parent, Scope scope_to_remove)
	{
		var children = scope_to_remove.getChildren();
		if (children != null)
		{
			for(var child : children)
			{
				parent.addSubscope(child);
				child.setParentScope(parent);
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
				// fix Marty's reported bug that filtering a loop or a line scope
				// double count the exclusive cost of the procedure scope parent. 
				// Since the cost of the procedure scope includes the cost of the child
				// we shouldn't merge the cost of the child to the parent.
				if (isParentEnclosingChild(parent, child))
					continue;
				
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
	
	
	/***
	 * Check if the parent is the enclosing procedure of the child.
	 * A child is within the same context as the parent is when
	 * the parent is a procedure context while the child is not.
	 * 
	 * @param parent
	 * @param child
	 * 
	 * @return true if the parent is the enclosing procedure
	 */
	private boolean isParentEnclosingChild(Scope parent, Scope child) {
		// if the child is not a line or a loop, they must be in different context
		// like between a call site and an instruction scope. 
		if (!(child instanceof LineScope || child instanceof LoopScope))
			return false;
		
		// if a child is a line and the parent is a loop, they are in the same context
		if (child instanceof LineScope && parent instanceof LoopScope)
			return true;
		
		// only if the parent is a call site or a procedure scope, we change the context
		// Usually an instruction scope also changes the context.
		// Note: It's almost impossible to have instruction as the parent and child is a loop
		//
		// e.g.: libxxx@123 -> loop at file.c: 0
		//
		// My understanding is that this is very unlikely.
		
		return (parent instanceof CallSiteScope  || 
				parent instanceof ProcedureScope || 
				parent instanceof InstructionScope);
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
				if (cpid > 0) {
					callpathInfo.replaceCallPath(cpid, parent_scope, parent_depth);
				}
			}
		}
	}
}
