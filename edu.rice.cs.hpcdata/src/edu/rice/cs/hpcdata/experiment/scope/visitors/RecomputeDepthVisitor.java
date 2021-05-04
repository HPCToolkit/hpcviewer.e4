package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.Map;

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
import edu.rice.cs.hpcdata.util.CallPath;

public class RecomputeDepthVisitor implements IScopeVisitor 
{
	private int depth = 0;
	private Map<Integer, CallPath> mapCpid;

	public RecomputeDepthVisitor(Map<Integer, CallPath> mapCpid) {
		this.mapCpid = mapCpid;
	}
	
	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			updateDepth(scope);
		}
	}

	@Override
	public void visit(StatementRangeScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(LoopScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			depth++;
		} else if (vt == ScopeVisitType.PostVisit) {
			depth--;
		}
	}

	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			depth++;
		} else if (vt == ScopeVisitType.PostVisit) {
			depth--;
		}
	}

	@Override
	public void visit(FileScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(GroupScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(LoadModuleScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(RootScope scope, ScopeVisitType vt) {}

	@Override
	public void visit(Scope scope, ScopeVisitType vt) {}

	
	private void updateDepth(Scope scope) {
		int cpid = scope.getCpid();
		if (scope.getCpid() >= 0) {
			CallPath cp = mapCpid.get(cpid);
			if (cp != null) {
				cp.setMaxDepth(depth);
			}
		}
	}
}
