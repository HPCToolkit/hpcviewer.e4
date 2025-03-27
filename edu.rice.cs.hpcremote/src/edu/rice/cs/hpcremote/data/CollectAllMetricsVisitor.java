// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import org.hpctoolkit.db.local.experiment.scope.CallSiteScope;
import org.hpctoolkit.db.local.experiment.scope.LineScope;
import org.hpctoolkit.db.local.experiment.scope.LoopScope;
import org.hpctoolkit.db.local.experiment.scope.ProcedureScope;
import org.hpctoolkit.db.local.experiment.scope.RootScope;
import org.hpctoolkit.db.local.experiment.scope.Scope;
import org.hpctoolkit.db.local.experiment.scope.ScopeVisitType;
import org.hpctoolkit.db.local.util.IProgressReport;

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
