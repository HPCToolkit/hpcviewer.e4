package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.ArrayDeque;
import java.util.Deque;

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

public abstract class BaseDuplicateScopeTreesVisitor implements IScopeVisitor 
{
	protected Deque<Scope> scopeStack;
	private final int metricOffset;
	
	protected BaseDuplicateScopeTreesVisitor(Scope newRoot, int metricOffset) {
		scopeStack = new ArrayDeque<>();
		scopeStack.push(newRoot);
		this.metricOffset = metricOffset;
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

	
	private void mergeInsert(Scope scope, ScopeVisitType vt) {
		if (!scope.isCounterZero())
			return;
		
		if (vt == ScopeVisitType.PreVisit) {
			Scope newParent = scopeStack.peek();
			
			Scope kid = findMatch(newParent, scope);
			
			Scope newKid = this.addMetricColumns(newParent, kid, scope);
			
			scopeStack.push(newKid);
			
		} else { // PostVisit
			scopeStack.pop();
		}
	}

	
	/*****
	 * add child and its metric values if needed
	 * 
	 * @param parent
	 * @param target
	 * @param source
	 * @return
	 */
	protected Scope addMetricColumns(Scope parent, Scope target, Scope source) {
		
		if (target == null) {
			// no target scope; create it under parent, and copy over source metrics
			target = source.duplicate();
			parent.addSubscope(target);
			target.setParentScope(parent);
			
			if (target instanceof RootScope)
			{
				target.setRootScope((RootScope) target);
				((RootScope)target).setExperiment(parent.getExperiment());
			} else 
			{
				target.setRootScope(parent.getRootScope());
			}
		} // else match! just copy source's metrics over to target
		
		accumulateMetrics(target, source);

		if (source instanceof CallSiteScope && target instanceof CallSiteScope) {
			accumulateMetrics(	((CallSiteScope)target).getLineScope(),
								((CallSiteScope)source).getLineScope());
		}
		
		return target;
	}
	
	protected void accumulateMetrics(Scope target, Scope source) {
				
		source.copyMetrics(target, metricOffset);
	}

	/****
	 * Try to find a scope kid that matches with another scope
	 * to be implemented in the child class: returning null if it doesn't match,
	 * 	 or the kid if it matches
	 * 
	 * @param parent
	 * @param toMatch
	 * @return the kid
	 */
	protected abstract Scope findMatch(Scope parent, Scope toMatch);

}
