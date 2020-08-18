package edu.rice.cs.hpctraceviewer.data;

import java.util.HashMap;

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
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;


/**********************************************************
 * Visitor class for gathering procedure names and the 
 * maximum depth.
 * 
 * To get the maximum depth, the caller requires to instantiate
 * the class, call dfsVisitScopeTree() method from the CCT root
 * and call the method getMaxDepth()
 **********************************************************/
public class TraceDataVisitor implements IScopeVisitor 
{
	final private HashMap<Integer, CallPath> map;
	final private ColorTable colorTable;
	
	private int current_depth = 0;
	private int max_depth = 0;

	public TraceDataVisitor(ColorTable colorTable) {
		
		map = new HashMap<Integer, CallPath>();
		this.colorTable = colorTable;
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) {  }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { }
	public void visit(FileScope scope, ScopeVisitType vt) { }
	public void visit(AlienScope scope, ScopeVisitType vt) { }
	public void visit(LoopScope scope, ScopeVisitType vt) { }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }	
	public void visit(GroupScope scope, ScopeVisitType vt) { }

	public void visit(ProcedureScope scope, ScopeVisitType vt) { 
		update(scope, vt);
	}

	public void visit(CallSiteScope scope, ScopeVisitType vt) { 
		update(scope, vt);
	}

	public void visit(LineScope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) {
			int cpid = scope.getCpid();
			if (cpid > 0)
			{
				this.map.put(cpid, new CallPath(scope, current_depth));
				if (current_depth <= 0) {
					System.err.println("Warning: depth cannot be less than 1: "  + current_depth);
				}
			}
		}
	}
	
	/****
	 * get the maximum depth from the tree traversal based on the scope
	 * where this visitor is used.
	 * 
	 * @return the maximum depth of the tree, which includes the call site+procedure scope
	 */
	public int getMaxDepth()
	{
		return max_depth;
	}
	
	/****
	 * get the map of cpid and its call path
	 * @return a hash map
	 */
	public HashMap<Integer, CallPath> getMap()
	{
		return map;
	}
	
	private void update(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			current_depth++;
			max_depth = Math.max(max_depth, current_depth);
			
			// assign a color to this procedure scope
			colorTable.addProcedure(scope.getName());
		} else {
			current_depth--;
		}

	}
}
