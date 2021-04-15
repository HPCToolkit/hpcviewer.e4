package edu.rice.cs.hpc.data.experiment.scope.visitors;

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

public class TraceScopeVisitor implements IScopeVisitor 
{
	int depth = 1;
	int maxDepth = 1;
	
	public int getMaxDepth() {
		return maxDepth;
	}
	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			
		} else if (vt == ScopeVisitType.PostVisit) {
			scope.setDepth(depth);
			maxDepth = Math.max(maxDepth, depth);
		}
	}

	@Override
	public void visit(StatementRangeScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(LoopScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(CallSiteScope scope, ScopeVisitType vt) { update(scope, vt); }

	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(FileScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(GroupScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(LoadModuleScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(RootScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(Scope scope, ScopeVisitType vt) { update(scope, vt); }

	
	private void update(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			depth++;
		} else if (vt == ScopeVisitType.PostVisit) {
			depth--;
		}
	}
}
