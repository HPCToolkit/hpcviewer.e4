package edu.rice.cs.hpcdata.experiment.scope.visitors;

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

public class CallingContextReassignment implements IScopeVisitor 
{

	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) {
			var list = scope.getScopeReduce();
			if (list.isEmpty())
				return;
			for(var child: list) {
				scope.reduce(child, MetricType.INCLUSIVE);
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
