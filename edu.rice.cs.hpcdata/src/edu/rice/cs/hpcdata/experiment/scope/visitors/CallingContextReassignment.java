package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.GroupScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.StatementRangeScope;


/*****
 * 
 * Combining line scope and procedure scopes for meta.db database.
 * <br/>
 * After tree traversal, need to call {@link postProcess} method to
 * finalizing the calling context tree reassignment.
 * <p> The line scopes will be removed from the top-down tree if after
 * the post processing there is no metrics associated with the line.
 * This is to avoid accidentally removing a line which is the parent of
 * other scopes that are not procedure frames.
 */
public class CallingContextReassignment implements IScopeVisitor 
{
	private final List<Scope> listScopesToRemove = new ArrayList<>();
	
	
	/****
	 * Finalizing the calling context reassignment by removing scopes
	 * that are not needed to be displayed.
	 * <br/>
	 * These scopes are usually the line scopes that have been "reduced" 
	 * and have no metric values. 
	 */
	public void postProcess() {
		// Fix for issue #245 and #248: remove unneeded scopes
		for(var scope: listScopesToRemove) {
			var parent = scope.getParentScope();
			parent.remove(scope);
			
			if (scope.hasChildren()) {
				// warning: we assume all children are the call sites
				// if a child is not a call then we shouldn't remove it.
				for(var child: scope.getChildren()) {
					parent.addSubscope(child);
					child.setParentScope(parent);
				}
			}		
		}
	}
	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) {
			var list = scope.getScopeReduce();
			if (list.isEmpty())
				return;
			for(var child: list) {
				scope.reduce(child, MetricType.INCLUSIVE);
			}
			if (!scope.hasNonzeroMetrics()) {
				listScopesToRemove.add(scope);
			}
		}
	}

	@Override
	public void visit(StatementRangeScope scope, ScopeVisitType vt) {/* unused */}

	@Override
	public void visit(LoopScope scope, ScopeVisitType vt) { /* unused */ }

	@Override
	public void visit(CallSiteScope scope, ScopeVisitType vt) { /* unused */ }

	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) { /* unused */ }

	@Override
	public void visit(FileScope scope, ScopeVisitType vt) {/* not treated in this class */}

	@Override
	public void visit(GroupScope scope, ScopeVisitType vt) {/* unused */}

	@Override
	public void visit(LoadModuleScope scope, ScopeVisitType vt) {/* not treated in this class */}

	@Override
	public void visit(RootScope scope, ScopeVisitType vt) {/* not treated in this class */}

	@Override
	public void visit(Scope scope, ScopeVisitType vt) {/* not treated in this class */}
}
