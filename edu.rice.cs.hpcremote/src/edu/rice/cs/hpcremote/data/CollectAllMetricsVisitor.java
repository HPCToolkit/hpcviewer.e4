// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.util.IProgressReport;

public class CollectAllMetricsVisitor extends CollectMetricsVisitor 
{

	public CollectAllMetricsVisitor(IProgressReport progress) {
		super(progress);
	}

	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(LoopScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
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
	public void visit(RootScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}
}
