package edu.rice.cs.hpc.data.experiment.scope.visitors;

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

public class ResetCounterVisitor implements IScopeVisitor {

	public void visit(Scope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(RootScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(AlienScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(LineScope scope, ScopeVisitType vt) { reset(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { reset(scope, vt); }

	private void reset(Scope scope, ScopeVisitType vt)
	{
		if (vt == ScopeVisitType.PreVisit)
		{
			scope.setCounter(0);
		}
	}
}
