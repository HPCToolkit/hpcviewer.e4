//////////////////////////////////////////////////////////////////////////
//																		//
//	ComputedMetricVisitor.java											//
//																		//
//	ComputedMetricVisitor -- visitor class to compute scalability 		//
//	Created: May 15, 2007 												//
//																		//
//	(c) Copyright 2007 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;

public class ComputedMetricVisitor implements IScopeVisitor {
	private int n;
	private double scaling;
	private MetricValue[] totals;

	public ComputedMetricVisitor(int nMetrics, Scope root2, double scalingFactor) {
		this.n = nMetrics;
		this.scaling = scalingFactor;

		// TODO [me] Better way to get totals? less 2-exp specific
		totals = new MetricValue[n];
		for (int i=0; i<n; i++)
			totals[i] = root2.getMetricValue(n+i);
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(RootScope scope, 				ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(LoadModuleScope scope, 		ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(FileScope scope, 				ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(GroupScope scope, 			ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(Scope scope, 					ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(CallSiteScope scope, 			ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(ProcedureScope scope, 		ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(LoopScope scope, 				ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(StatementRangeScope scope, 	ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	public void visit(LineScope scope, 				ScopeVisitType vt) { addComputedMetrics(scope, vt); }
	
	private void addComputedMetrics(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			// base indices for experiments
			int exp1 = 0, exp2 = n; 
			final Experiment exp = (Experiment) scope.getExperiment();
			int cmi = exp.getMetricCount() - n;
			
			for (int i=0; i<n; i++) {
				double mv1 = MetricValue.getValue(scope.getMetricValue(exp1+i));
				double mv2 = MetricValue.getValue(scope.getMetricValue(exp2+i));
				double t2 = MetricValue.getValue(totals[i]);
				if (mv2 < 0 || t2 <= 0) break;
				if (mv1 < 0) mv1 = 0.0;
					
				double value = computeScalability(mv1,mv2,scaling,t2);

				MetricValue val = new MetricValue();
				MetricValue.setValue(val, value);
				scope.setMetricValue(cmi+i, val);
			}
		}
	}
	
	private double computeScalability(double mv1, double mv2, double scaling, double t2) {
		// S = ( m2*p2 - m1*p1 ) / (t2*p2)
		// S = m2/t2 - (m1/t2)*(p1/p2)
		return ( mv2 / t2 ) - ( scaling * ( mv1 / t2 ));
	}
}
