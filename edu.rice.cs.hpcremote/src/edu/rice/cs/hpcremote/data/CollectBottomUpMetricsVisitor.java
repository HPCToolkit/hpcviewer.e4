// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import org.hpctoolkit.db.local.experiment.scope.CallSiteScope;
import org.hpctoolkit.db.local.experiment.scope.InstructionScope;
import org.hpctoolkit.db.local.experiment.scope.ProcedureScope;
import org.hpctoolkit.db.local.experiment.scope.Scope;
import org.hpctoolkit.db.local.experiment.scope.ScopeVisitType;
import org.hpctoolkit.db.local.util.IProgressReport;

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
