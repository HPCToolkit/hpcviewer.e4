package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.data.filter.FilterAttribute;
import edu.rice.cs.hpc.data.filter.IFilterData;


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
	private final IMetricValueCollection rootMetricValues;
	private final BaseExperiment experiment;
	private final RootScope rootOriginalCCT;
	
	private BaseMetric []metrics = null;
	
	/**** flag to allow the dfs to continue to go deeper or not.  
	      For inclusive filter, we should stop going deeper      *****/
	private boolean need_to_continue;

	private int num_scope_filtered = 0;
	private int filterStatus 	   = STATUS_INIT;
	
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
		this.rootMetricValues = rootOriginalCCT.getMetricValues();
		this.rootOriginalCCT  = rootOriginalCCT;
		need_to_continue 	  = true;
		
		experiment = rootOriginalCCT.getExperiment();
		if (experiment instanceof Experiment)
		{
			metrics = ((Experiment)experiment).getMetrics();
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
	
	public int numberOfFilteredScopes() 
	{
		return num_scope_filtered;
	}
	
	public int getFilterStatus() 
	{
		return filterStatus;
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
			Scope parent = scope.getParentScope();
			
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
					//-------------------------------------------------------------------
					for (Object child: scope.getChildren())
					{
						scope.remove((TreeNode) child);
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
							mergeMetrics(parent, scope, need_to_continue);
						}
					}
					removeChild(scope, vt, filterAttribute.filterType);
				}
			} else 
			{
				// filter is not needed, we can surely continue to investigate the descendants
				need_to_continue = true;
			}			
		} else 
		{ // PostVisit
		}
		return need_to_continue;
	}
	
	/********
	 * Remove a child from its parent. if the filter type is SELF, we'll attach the grandchildren
	 * to the parent
	 * 
	 * @param childToRemove : scope to remove
	 * @param filterType : filter type
	 */
	private void removeChild(Scope childToRemove, ScopeVisitType vt, FilterAttribute.Type filterType)
	{
		// skip to current scope
		Scope parent = childToRemove.getParentScope();
		parent.remove(childToRemove);
		
		// remove its children and glue it the parent
		if (filterType == FilterAttribute.Type.Self_Only)
		{
			addGrandChildren(parent, vt, childToRemove);
		}
	}
	
	/*****
	 * Add the grand children to the parent
	 * @param parent
	 * @param scope_to_remove
	 */
	private void addGrandChildren(Scope parent, ScopeVisitType vt, Scope scope_to_remove)
	{
		Object []children = scope_to_remove.getChildren();
		if (children != null)
		{
			for(Object child : children)
			{
				Scope child_scope = (Scope) child;
				parent.add(child_scope);
				child_scope.setParent(parent);
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
		for (int i=0; i<metrics.length; i++)
		{
			MetricValue childValue = values.getValue(child, i);
			if (childValue == MetricValue.NONE) {
				// in the original hpcview (2002), we assign the -1 value as the "none existence value"
				// this is not proper. we should assign as null for the non-existence
				// special case: every time the child has "none" value we can skip it instead of
				//  merging to the parent since x - 1 is not the same as x - 0
				continue;
			}
			if (exclusive_filter && metrics[i].getMetricType() == MetricType.EXCLUSIVE)
			{
				MetricValue value = parent.getMetricValue(i);
				if (value != MetricValue.NONE)
				  value = value.duplicate();
				parent.setMetricValue(i, value);
				
				// exclusive filter: merge the exclusive metrics to the parent's exclusive
				mergeMetricToParent(parent, i, childValue);
				
			} else if (!exclusive_filter && metrics[i].getMetricType() == MetricType.INCLUSIVE)
			{
				// inclusive filter: merge the inclusive metrics to the parent's exclusive
				int index_exclusive_metric = metrics[i].getPartner();

				// this is tricky: the original index of the metric is the same as the short name
				// however, when we ask getMetric(), it requires the metric index in the array (which is 0..n)
				// we can cheat this by converting the index into "short name" and get the metric.
				BaseMetric metric_exc = ((Experiment)experiment).getMetric(String.valueOf(index_exclusive_metric));
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
		float rootValue   = rootMetricValues.getValue(rootOriginalCCT, metric_exclusive_index).getValue();
		float annotation  = value / rootValue;
		
		MetricValue mv    = new MetricValue(value, annotation);
		target.setMetricValue(metric_exclusive_index, mv);
	}
}
