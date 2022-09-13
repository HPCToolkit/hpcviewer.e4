package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.*;
import edu.rice.cs.hpcdata.experiment.scope.*;
import edu.rice.cs.hpcdata.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.filters.MetricValuePropagationFilter;


/*************************************************************************
 * 
 * A class that manages the initialization phase of the creation of callers tree
 * For the second phase of callers tree (incremental callers path) should be 
 * seen in {@code CallSiteScopeCallerView} 
 *
 *************************************************************************/
public class CallersViewScopeVisitor extends CallerScopeBuilder implements IScopeVisitor 
{
	private static final String SEPARATOR = ":";

	//----------------------------------------------------
	// private data
	//----------------------------------------------------
	private final CombineCallerScopeMetric combinedMetrics;
	
	private final ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private final InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	
	private final ListCombinedScopes listCombinedScopes;
	private final HashMap<String, Scope> calleeht;
	
	private final RootScope callersViewRootScope;
	
	/****
	 * Constructor to create a dynamic bottom-up tree
	 * 
	 * @param experiment
	 * @param cvrs
	 * @param filter
	 ****/
	public CallersViewScopeVisitor(Experiment experiment, RootScope cvrs, 
								   MetricValuePropagationFilter filter) {
		this.callersViewRootScope = cvrs;
		exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(experiment);
		inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(experiment);
		combinedMetrics = new CombineCallerScopeMetric();

		listCombinedScopes = new ListCombinedScopes();
		calleeht = new HashMap<>();
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		
		//--------------------------------------------------------------------------------
		// if there are no exclusive costs to attribute from this context, we are done here
		//--------------------------------------------------------------------------------
		if (!scope.hasNonzeroMetrics()) {
			return; 
		}
		
		if (vt == ScopeVisitType.PreVisit) {
			this.listCombinedScopes.push();

			// Find (or add) callee in top-level hashtable
			ProcedureScope callee = this.createProcedureIfNecessary(scope, scope.getProcedureScope());			
			prepareCallChain(scope, callee);

		} else if (vt == ScopeVisitType.PostVisit)  {			
			this.decrementCounter();
		}
	}
	
	
	public void visit(ProcedureScope scope, ScopeVisitType vt) { 
		
		//--------------------------------------------------------------------------------
		// if there are no exclusive costs to attribute from this context, we are done here
		//--------------------------------------------------------------------------------
		if ( !scope.hasNonzeroMetrics() || 
			  scope.isTopDownProcedure() ) {
			return; 
		}
		
		if (vt == ScopeVisitType.PreVisit) {
			// have to push whether we will create a procedure or not since
			// we will pop during Post visit.
			this.listCombinedScopes.push();
			
			if (!scope.isAlien()) {
				// Find (or add) callee in top-level hashtable
				ProcedureScope callee = this.createProcedureIfNecessary(scope, scope);
				prepareCallChain(scope, callee);
			}
		} else if (vt == ScopeVisitType.PostVisit){			
			this.decrementCounter();
		}
	}
	 
	public void visit(Scope scope, ScopeVisitType vt) { 
		// we are interested only in instruction scope
		if (!(scope instanceof InstructionScope))
			return;
		
		//--------------------------------------------------------------------------------
		// if there are no exclusive costs to attribute from this context, we are done here
		//--------------------------------------------------------------------------------
		if (!scope.hasNonzeroMetrics()) {
			return; 
		}
		
		if (vt == ScopeVisitType.PreVisit) {
			this.listCombinedScopes.push();

			// Find (or add) callee in top-level hashtable
			InstructionScope is   = (InstructionScope) scope;
			ProcedureScope callee = this.createProcedureIfNecessary(scope, is.getProcedureScope());			
			prepareCallChain(scope, callee);

		} else if (vt == ScopeVisitType.PostVisit)  {			
			this.decrementCounter();
		}
	}
	
	public void visit(RootScope scope, ScopeVisitType vt) 			{ /* no action */ }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) 	{ /* no action */ }
	public void visit(FileScope scope, ScopeVisitType vt) 			{ /* no action */ }
	public void visit(AlienScope scope, ScopeVisitType vt) 			{ /* no action */ }
	public void visit(LoopScope scope, ScopeVisitType vt) 			{ /* no action */ }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { /* no action */ }
	public void visit(LineScope scope, ScopeVisitType vt) 			{ /* no action */ }
	public void visit(GroupScope scope, ScopeVisitType vt) 			{ /* no action */ }

	
	//----------------------------------------------------
	// helper functions  
	//----------------------------------------------------
	
	
	/*****
	 * prepare and initialize call chain of a given scope node and procedure node
	 * 
	 * @param scope: the current scope
	 * @param callee: the callee node (the procedure scope of this call chain)
	 */
	private void prepareCallChain(Scope scope, ProcedureScope callee) {
		LinkedList<CallSiteScopeCallerView> callPathList = createCallChain(callersViewRootScope,
				scope, scope, 
				combinedMetrics, this.inclusiveOnly, this.exclusiveOnly);

		//-------------------------------------------------------
		// ensure my call path is represented among my children.
		//-------------------------------------------------------
		mergeCallerPath(IMergedScope.MergingStatus.INIT, 0, callee, callPathList, 
				combinedMetrics, this.inclusiveOnly, this.exclusiveOnly);
	}
	
	/********
	 * Find caller view's procedure of a given scope. 
	 * If it doesn't exist, create a new one, attach to the tree, and copy the metrics
	 * 
	 * @param scopeCCT : either call site or procedure
	 * @return ProcedureScope
	 ********/
	private ProcedureScope createProcedureIfNecessary( Scope scopeCCT, ProcedureScope procScopeCCT ) {
		
		String objCode = procScopeCCT.getSourceFile().getFileID() + SEPARATOR + 
						 procScopeCCT.getFirstLineNumber() + SEPARATOR + 
						 procScopeCCT.getName().hashCode();

		ProcedureScope procCaller = (ProcedureScope) calleeht.get(objCode);
		
		if (procCaller == null) {
			// create a new procedure scope
			procCaller = (ProcedureScope) procScopeCCT.duplicate();
			procCaller.setRootScope(callersViewRootScope);

			// add to the tree
			callersViewRootScope.addSubscope(procCaller);
			procCaller.setParentScope(this.callersViewRootScope);
			
			// add to the dictionary to make sure we have unique procedure for each procedures
			calleeht.put(objCode, procCaller);
		}
		
		// accumulate the metrics
		this.combinedMetrics.combine(procCaller, scopeCCT, inclusiveOnly, exclusiveOnly);
		return procCaller;
	}
		

	
	/********
	 * decrement the counter of a caller scope.
	 * Increment and decrement the counter have to match to avoid miscalculation
	 * for recursive functions.
	 * @see CombineCallerScopeMetric 
	 ********/
	private void decrementCounter() {
		
		//---------------------------------------------------------------------------
		// decrement all the combined scopes which are computed in this scope
		// 	When a caller view scope is created, it creates also its children and its merged children
		//		the counter of these children are then need to be decremented based on the CCT scope
		//---------------------------------------------------------------------------
		var list = this.listCombinedScopes.pop();
		if (list != null) {
			Iterator<Scope> iter = list.iterator();
			while (iter.hasNext()) {
				Scope combinedScope = iter.next();
				combinedScope.decrementCounter();
			}
		}
	}
	

	
	//----------------------------------------------------
	// helper classes  
	//----------------------------------------------------
	
	/********************************************************************
	 *
	 * class helper to store the list of combined scopes
	 * 
	 ********************************************************************/
	private static class ListCombinedScopes {
		private Deque<ArrayList<Scope>> combinedScopes;
		
		public List<Scope> push() {
			if (this.combinedScopes == null) {
				this.combinedScopes = new ArrayDeque<>();
			}
			ArrayList<Scope> list = new ArrayList<>();
			this.combinedScopes.push(list);
			return list;
		}
		
		public List<Scope> pop() {
			return this.combinedScopes.pop();
		}
		
		public void addList(Scope combined) {
			ArrayList<Scope> list = this.combinedScopes.peek();
			list.add(combined);
		}
	}
	
	
	/********************************************************************
	 *
	 * class to combine metrics from different scopes.
	 * This class will increment the counter of the "combined" scope to avoid
	 *  miscalculation of recursive functions.
	 * The caller has to decrement the counter upon the post visit.
	 * 
	 * @see  decrementCounter
	 ********************************************************************/
	private class CombineCallerScopeMetric extends AbstractCombineMetric {

		public void combine(Scope target, Scope source,
				MetricValuePropagationFilter inclusiveOnly,
				MetricValuePropagationFilter exclusiveOnly) {

			if (target.isCounterZero() && inclusiveOnly != null) {
				target.safeCombine(source, inclusiveOnly);
			}
			if (exclusiveOnly != null)
				target.combine(source, exclusiveOnly);

			target.incrementCounter();

			listCombinedScopes.addList(target);
		}
	}
}
