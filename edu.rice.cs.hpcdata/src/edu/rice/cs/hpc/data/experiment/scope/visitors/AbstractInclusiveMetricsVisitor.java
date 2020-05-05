package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.AlienScope;
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
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public abstract class AbstractInclusiveMetricsVisitor implements IScopeVisitor {

	final protected MetricValuePropagationFilter filter;
	public AbstractInclusiveMetricsVisitor(Experiment experiment, MetricValuePropagationFilter currentFilter) {
		this.filter = currentFilter;
	}
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(AlienScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(LineScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { up(scope, vt); }

	//----------------------------------------------------
	// propagate a child's metric values to its parent
	//----------------------------------------------------

	protected void up(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			Scope parent = scope.getParentScope();
			if (parent != null) {
				if (scope instanceof CallSiteScope) {
					// inclusive view: add everything
					accumulateToParent( parent, scope );
					
				} else {
					// New definition of exclusive cost:
					//	The cost of Outer loop does not include the cost of inner loop 
					if(parent instanceof LoopScope && scope instanceof LoopScope) {
						// for nested loop: we need to accumulate the inclusive but not exclusive.
						if(filter instanceof InclusiveOnlyMetricPropagationFilter) {
							// During the creation of CCT, we call this class twice: one for exclusive, the other for incl
							// so we need to make sure that only the inclusive is taken
							accumulateToParent( parent, scope );
						}
						return;
					} 
					accumulateToParent( parent, scope );
				}
			}
		}
	}
	
	
	/**
	 * Method to accumulate the metric value from the child to the parent
	 * @param parent
	 * @param source
	 */
	abstract protected void accumulateToParent(Scope parent, Scope source) ;
}
