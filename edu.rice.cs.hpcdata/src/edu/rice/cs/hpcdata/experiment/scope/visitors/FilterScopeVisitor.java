package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

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
import edu.rice.cs.hpcdata.util.CallPath;
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
	public static final int STATUS_INIT=0;
	public static final int STATUS_OK = 1;
	public static final int STATUS_FAKE_PROCEDURE = 2; 
	
	private final IFilterData filter;
	private final IExperiment experiment;
	private final ICallPath   callPathTraces;
	
	private final List<BaseMetric> metrics;
	private final List<Scope> listScopesToRemove;
	private final List<Scope> listTreeToRemove;
	
	/** map from a filtered scope to another scope. 
	 *  This map is useful if we want to associate a child
	 *  with a filtered parent. **/
	private final IntObjectHashMap<Scope> mapReplacement;
	
	/**** flag to allow the dfs to continue to go deeper or not.  
	      For inclusive filter, we should stop going deeper      *****/
	private boolean needToContinue;

	private int filterStatus = STATUS_INIT;
	private int currentDepth;
	private int maxDepth;
	
	/***********
	 * Constructor to filter a cct
	 * 
	 * @param rootFilter : the main root for filter tree 
	 * @param rootOriginalCCT : the original cct tree
	 * @param filter : filter map to filter a string
	 */
	public FilterScopeVisitor(RootScope rootOriginalCCT, IFilterData filter)
	{
		this.filter  = filter;
		
		rootOriginalCCT.getMetricValues();
		needToContinue 	= true;
		currentDepth = 0;
		maxDepth     = 0;
		
		listScopesToRemove = FastList.newList();
		listTreeToRemove   = FastList.newList();
		mapReplacement = new IntObjectHashMap<>();
		
		experiment = rootOriginalCCT.getExperiment();
		if (experiment instanceof Experiment)
		{
			Experiment exp = (Experiment) experiment;
			metrics        = exp.getMetricList();
			callPathTraces = exp.getScopeMap();
		} else {
			callPathTraces = null;
			metrics = null;
		}
	}
	
	
	/****
	 * Optionally, the caller can call {@code dispose} to free up
	 * allocated resources. Sometimes GC is unable to free them.
	 */
	public void dispose() {
		mapReplacement.clear();
		listScopesToRemove.clear();
		listTreeToRemove.clear();
	}
	
	
	/*****
	 * List of scopes to be removed.
	 * 
	 * @return {@code List} of scopes
	 */
	public List<Scope> getScopeToRemove() {
		return listScopesToRemove;
	}
	
	
	/****
	 * Get the list of sub trees to be removed
	 * 
	 * @return {@code List} of scopes
	 */
	public List<Scope> getTreeToRemove() {
		return listTreeToRemove;
	}
	
	/**************
	 * return a flag whether the caller needs to dig deeper to their descendants or not
	 * 
	 * @return true if one needs to continue to walk into the descendant, false otherwise.
	 */
	public boolean needToContinue()
	{
		return needToContinue;
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
		return maxDepth;
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
			if (CallPath.isTraceScope(scope)) {
				currentDepth++;
				maxDepth = Math.max(maxDepth, currentDepth);
			} 
			FilterAttribute filterAttribute = filter.getFilterAttribute(scope.getName());
			if (filterAttribute == null) {
				// filter is not needed, we can surely continue to investigate the descendants
				needToContinue = true;
				return needToContinue;
			}
			filterStatus   = filterStatus != STATUS_FAKE_PROCEDURE ? STATUS_OK : STATUS_FAKE_PROCEDURE;
			needToContinue = (filterAttribute.filterType == FilterAttribute.Type.Self_Only);
			if (filterAttribute.filterType == FilterAttribute.Type.Descendants_Only &&
				scope.getSubscopeCount() > 0)
			{
				//-------------------------------------------------------------------
				// Filtering only the children, not the scope itself.
				// remove all the children
				// We need to remove the child using the iterator's remove method
				// to avoid ConcurrentModificationException 
				//-------------------------------------------------------------------
				var childIterator = scope.getChildren().iterator();
				while (childIterator.hasNext())
				{
					var child = childIterator.next();
					
					// merge with the metrics of the children
					if (metrics != null)
						mergeMetrics(scope, child, false);

					removeNode(child, filterAttribute.filterType);
				}
			} else if(filterAttribute.filterType == FilterAttribute.Type.Self_And_Descendants ||
					  filterAttribute.filterType == FilterAttribute.Type.Self_Only)
			{
				//-------------------------------------------------------------------
				// Filtering the scope or/and the children
				//-------------------------------------------------------------------
				if (metrics != null && !(scope instanceof LineScope))
				{
					// no need to merge metric if the filtered child is a line statement.
					// in this case, the parent (PF) already includes the exclusive value.
					Scope parent = scope.getParentScope();
					mergeMetrics(parent, scope, needToContinue);
				}
				removeChild(scope, filterAttribute.filterType);
			}
		} else 
		{ // PostVisit
			if (CallPath.isTraceScope(scope)) {
				currentDepth--;
			}
		}
		return needToContinue;
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
	private void removeChild(Scope childToRemove, FilterAttribute.Type filterType)
	{
		// remove its children and glue it the parent
		if (filterType == FilterAttribute.Type.Self_Only)
		{
			addGrandChildren(getAncestor(childToRemove), childToRemove);
		}
		// skip to current scope
		removeNode(childToRemove, filterType);
	}

	
	private Scope getAncestor(Scope scope) {
		Scope ancestor = scope.getParentScope();
		Scope replacement = mapReplacement.get(ancestor.getCpid());
		if (replacement != null) {
			ancestor = replacement;
		}
		mapReplacement.put(scope.getCpid(), ancestor);
		return ancestor;
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
	private Scope removeNode(Scope child, FilterAttribute.Type filterType) {
		Scope ancestor = getAncestor(child);			

		// move the trace call-path id (if exist) to the parent
		if (experiment.getTraceDataVersion() > 0)
			propagateTraceID(ancestor, child, filterType);

		if (filterType == FilterAttribute.Type.Self_Only) {
			// remove the child node
			listScopesToRemove.add(child);
		} else {
			listTreeToRemove.add(child);
		}
		
		return ancestor;
	}
	
	
	private void propagateTraceID(Scope parent, Scope child, FilterAttribute.Type filterType) {
		if (filterType == FilterAttribute.Type.Self_Only) {
			int depth = CallPath.getDepth(parent);

			// replace the current trace id with the ancestor (or parent)
			callPathTraces.replaceCallPath(child.getCpid(), parent, depth);
			
			// replace the children trace id with the ancestor (or parent), 
			// only if they are not trace scope
			if (child.hasChildren())
				// sometimes the elided child has no children at all
				// in this case, we don't propagate the trace id
				child.getChildren().stream()
								   .filter(scope -> !CallPath.isTraceScope(scope))
								   .forEach(scope -> callPathTraces.replaceCallPath(scope.getCpid(), parent, depth));
			return;
		}
		
		// children have been removed
		// copy the cpid to the parent
		CallPathTraceVisitor cptv = new CallPathTraceVisitor();
		cptv.parentScope  = parent;
		cptv.callpathInfo = callPathTraces;
		cptv.parentDepth  = CallPath.getDepth(parent);
		
		child.dfsVisitScopeTree(cptv);
	}
		
	
	/*****
	 * Add the grand children to the parent
	 * @param parent
	 * @param scopeToRemove
	 */
	private void addGrandChildren(Scope parent, Scope scopeToRemove)
	{
		var children = scopeToRemove.getChildren();
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
	 * @param exclusiveFilter : whether to merge exclusive only (true) or inclusive metric
	 * 	to the exclusive metric of the parent
	 */
	private void mergeMetrics(Scope parent, Scope child, boolean exclusiveFilter)
	{
		if (parent instanceof ProcedureScope && ((ProcedureScope)parent).isFalseProcedure()) {
			filterStatus = STATUS_FAKE_PROCEDURE;
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
			if (exclusiveFilter && metric.getMetricType() == MetricType.EXCLUSIVE)
			{
				// fix Marty's reported bug that filtering a loop or a line scope
				// double count the exclusive cost of the procedure scope parent. 
				// Since the cost of the procedure scope includes the cost of the child
				// we shouldn't merge the cost of the child to the parent.
				if (!isParentEnclosingChild(parent, child)) {					
					MetricValue value = parent.getMetricValue(metric);
					if (value != MetricValue.NONE)
					  value = value.duplicate();
					
					parent.setMetricValue(metric.getIndex(), value);
					
					// exclusive filter: merge the exclusive metrics to the parent's exclusive
					mergeMetricToParent(parent, metric.getIndex(), childValue);
				}
				
			} else if (!exclusiveFilter && metric.getMetricType() == MetricType.INCLUSIVE)
			{
				// inclusive filter: merge the inclusive metrics to the parent's exclusive
				int exclusiveMetricIndex   = metric.getPartner();
				BaseMetric exclusiveMetric = ((Experiment)experiment).getMetric(exclusiveMetricIndex);
				mergeMetricToParent(parent, exclusiveMetric.getIndex(), childValue);
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
	 * @param exclusiveMetricIndex
	 * @param mvChild
	 */
	private void mergeMetricToParent(Scope target, 
			int exclusiveMetricIndex, MetricValue mvChild)
	{
		// corner case: we shouldn't modify the value of the root.
		// they are supposed to be constant, unless it's a derived metric :-(
		if (target instanceof RootScope)
			return;
		
		MetricValue mvParentExc = target.getDirectMetricValue(exclusiveMetricIndex);
		float value = 0;
		if (mvParentExc.getValue() >= 0) {
			// Initialize with the original value if it has a value (otherwise the value is -1)
			value = mvParentExc.getValue();
		}
		// update the filtered value
		value            += mvChild.getValue();		
		MetricValue mv    = new MetricValue(value);
		target.setMetricValue(exclusiveMetricIndex, mv);
	}
	
	
	private static class CallPathTraceVisitor implements IScopeVisitor
	{
		Scope parentScope;
		int parentDepth;
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
					callpathInfo.replaceCallPath(cpid, parentScope, parentDepth);
				}
			}
		}
	}
}
