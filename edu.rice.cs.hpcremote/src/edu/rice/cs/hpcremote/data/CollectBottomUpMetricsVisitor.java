package edu.rice.cs.hpcremote.data;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.util.IProgressReport;

public class CollectBottomUpMetricsVisitor extends CollectMetricsVisitor 
{
	
	public CollectBottomUpMetricsVisitor(IProgressReport progress) {
		super(progress);
	}
	
	
	@Override
	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}
	
	
	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}
	
	
	@Override
	public void visit(Scope scope, ScopeVisitType vt) {
		if (!(scope instanceof InstructionScope))
			return;

		add(scope);
	}
}
