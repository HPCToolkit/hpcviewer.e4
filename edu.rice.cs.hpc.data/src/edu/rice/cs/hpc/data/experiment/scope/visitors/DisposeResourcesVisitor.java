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


/*******************************************************************
 * 
 * Visitor class to remove resources manually
 * 
 * It appears that mutual object references will not be removed from
 * the head even if the parent or the children or one of them is nullified. 
 * It seems JVM is not smart enough to traverse the tree to free the heap.
 * 
 * This class will traverse all the tree and dispose all the resources
 * manually.
 *
 *******************************************************************/
public class DisposeResourcesVisitor implements IScopeVisitor 
{
	public void visit(Scope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(RootScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(AlienScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(LineScope scope, ScopeVisitType vt) { inspect(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { inspect(scope, vt); }

	
	private void inspect(Scope scope, ScopeVisitType vt) {
		
		if (vt == ScopeVisitType.PostVisit) {
			scope.dispose();
		}
	}
}
