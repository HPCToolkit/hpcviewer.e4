/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;

/**
 * @author laksonoadhianto
 *
 */
public class ExclusiveCallingContextVisitor implements IScopeVisitor {

	final private ExclusiveOnlyMetricPropagationFilter filterExclusive;
	final private int numberOfPrimaryMetrics;
	/**
	 * 
	 */
	public ExclusiveCallingContextVisitor(Experiment experiment) {
		this.numberOfPrimaryMetrics = experiment.getMetricCount();
		this.filterExclusive = new ExclusiveOnlyMetricPropagationFilter(experiment);
	}

	/* (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor#visit(edu.rice.cs.hpc.data.experiment.scope.LoopScope, edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType)
	 */
	public void visit(LoopScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			Scope parent = scope.getParentScope();
			if(parent != null) {
				if ( (parent instanceof LoopScope) ) {
					this.accumulateAncestor(scope);
				} else {
					// for exclusive filter, we want to add all the loop cost into the parent if the parent 
					// is either procedure scope or call site scope
					// this is the effect of due to not attributing the inner loop cost into outer loop cost
					parent.accumulateMetrics(scope, this.filterExclusive, this.numberOfPrimaryMetrics);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor#visit(edu.rice.cs.hpc.data.experiment.scope.CallSiteScope, edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType)
	 */
	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			Scope parent = scope.getParentScope();
			if(parent != null) {
				// accumulate the value of the call into the scope
				LineScope lineScope = scope.getLineScope();
				parent.accumulateMetrics(lineScope, this.filterExclusive, numberOfPrimaryMetrics);
			}
		}

	}

	/* (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor#visit(edu.rice.cs.hpc.data.experiment.scope.ProcedureScope, edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType)
	 */
	public void visit(RootScope scope, ScopeVisitType vt) { 
		// no action
	}
	/**
	 * do we need to add this to the parent ?
	 */
	public void visit(LineScope scope, ScopeVisitType vt) {add(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { add(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { add(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { add(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { add(scope, vt); }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { add(scope, vt); }
	public void visit(Scope scope, ScopeVisitType vt) { add(scope, vt); }

	//--------------------------------------------------------
	
	private void add(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			Scope parent = scope.getParentScope();
			if (parent == null)
				return;
			
			parent.accumulateMetrics(scope, this.filterExclusive, this.numberOfPrimaryMetrics);
		}
	}
	
	private void accumulateAncestor(Scope scope) {
		Scope parent = scope.getParentScope();
		if(parent != null) {
			if(parent instanceof RootScope)
				return;
			while ( (parent != null) && !(parent instanceof CallSiteScope) && 
					!(parent instanceof RootScope) && !(parent instanceof ProcedureScope) ) {
				parent = parent.getParentScope();
			}
			if (parent != null) {
				if ( (parent instanceof CallSiteScope) || (parent instanceof ProcedureScope) ) {
					parent.accumulateMetrics(scope, this.filterExclusive, this.numberOfPrimaryMetrics);
				}
			}
		}
	}
}
