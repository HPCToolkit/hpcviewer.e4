package edu.rice.cs.hpcdata.experiment.scope.visitors;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.EntryScope;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class FlaViewScopeBuilder 
{

	
	/***********************************************************
	 * Iteratively finding an enclosing procedure of a CCT scope
	 * @param cctScope
	 * @return
	 ***********************************************************/
	protected ProcedureScope findEnclosingProcedure(Scope cctScope)
	{
		if (cctScope == null)
			return null;
			
		if (cctScope instanceof ProcedureScope) 
			return (ProcedureScope) cctScope;
		
		if (cctScope instanceof InstructionScope)
			return ((InstructionScope)cctScope).getProcedureScope();
		
		Scope parent = cctScope.getParentScope();
		if (parent instanceof EntryScope && cctScope instanceof CallSiteScope) {
			return ((CallSiteScope)cctScope).getProcedureScope();
		}
		while(parent != null) {
			if (parent instanceof CallSiteScope) {
				return ((CallSiteScope) parent).getProcedureScope();
			}
			if (parent instanceof ProcedureScope) {
				ProcedureScope proc = (ProcedureScope) parent;
				if (!proc.isAlien())
					return proc;
			}
			if (parent instanceof InstructionScope)
				return ((InstructionScope) parent).getProcedureScope();
			if (parent instanceof RootScope) 
				return null;
			parent = parent.getParentScope();
		}
		return null;
	}
	
	
	/******************************************************************
	 * add child to the parent
	 * 
	 * @param parent
	 * @param child
	 ****************************************************************/
	protected void addChild(Scope parent, Scope child) {
		parent.addSubscope(child);
		child.setParentScope(parent);
	}

	
	/***********************************************************
	 * check if a scope has been assigned as the outermost instance
	 * @param scope
	 * @return
	 ***********************************************************/
	protected boolean isOutermostInstance(Scope scope) {
		return scope.getCounter() == 1;
	}

	
	/*************************************************************************
	 * Each scope in the flat view has to be linked with 3 enclosing scopes:
	 * <ul>
	 *  <li> load module
	 *  <li> file
	 *  <li> procedure
	 * </ul>
	 *************************************************************************/
	static class FlatScopeInfo {
		LoadModuleScope flatLM;
		FileScope flatFile;
		Scope flatScope;
		
		
		/***
		 * Attach file scope to the load module scope
		 */
		public void attachFileScope() {
			flatLM.addSubscope(flatFile);
			flatFile.setParentScope(flatLM);
		}
	}
}
