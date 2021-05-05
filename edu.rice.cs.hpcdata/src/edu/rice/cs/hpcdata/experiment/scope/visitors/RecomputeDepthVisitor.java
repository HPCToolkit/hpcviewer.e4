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


/***************************************************
 * 
 * Class to recompute the depth and update the map cpid to call path
 *
 ***************************************************/
public class RecomputeDepthVisitor implements IScopeVisitor 
{
	private int depth = 0;
	private int maxDepth = 0;
	private Map<Integer, CallPath> mapCpid;

	/****
	 * Constructor. Requires the map cpid to call-path.
	 * This class will modify the content of the map to reflect the current
	 * cpid and call-path in the tree.
	 * 
	 * @param mapCpid (IN) the map cpid to call path
	 */
	public RecomputeDepthVisitor(Map<Integer, CallPath> mapCpid) {
		this.mapCpid = mapCpid;
		this.mapCpid.clear();
	}
	
	/**
	 * Retrieve the new max depth of the tree
	 * @return int max depth
	 */
	public int getMaxDepth() {
		return maxDepth;
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
		// prof2 database may have cpid in interior nodes
		checkScope(scope, vt);
	}

	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) {
		// prof2 database may have cpid in interior nodes
		checkScope(scope, vt);
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

	private void checkScope(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			depth++;
			maxDepth = Math.max(maxDepth, depth);
			
			updateDepth(scope);
		} else if (vt == ScopeVisitType.PostVisit) {
			depth--;
		}
	}
	
	private void updateDepth(Scope scope) {
		int cpid = scope.getCpid();
		if (cpid < 0)
			return;
		
		CallPath cp = new CallPath(scope, depth);
		mapCpid.put(cpid, cp);
	}
}
