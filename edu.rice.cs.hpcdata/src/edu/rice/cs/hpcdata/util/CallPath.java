package edu.rice.cs.hpcdata.util;

import java.util.Vector;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class CallPath
{

	/**the Scope at the current cpid*/
	private Scope leafScope;
	
	/**the depth of leafScope (where current cpid is)*/
	private int maxDepth;

	
	/******
	 * Constructor for call path class.
	 * 
	 * @param scope the scope of the trace. Doesn't have to be a leaf.
	 * @param depth the depth of the scope.
	 */
	public CallPath(Scope scope, int depth)
	{
		leafScope = scope;
		maxDepth = depth;
	}
	
	/*******
	 * Return true if the scope is part of trace scope. False otherwise
	 * @param scope
	 * @return
	 */
	public static boolean isTraceScope(Scope scope) {
		return (scope instanceof CallSiteScope || scope instanceof ProcedureScope);
	}
	
	/**returns the scope at the given depth that's along the path between the root scope and the leafScope*/
	public Scope getScopeAt(int depth)
	{
		if (depth < 0)
			return null;
		
		int cDepth = maxDepth;
		Scope cDepthScope = leafScope;
		
		if (isTraceScope(cDepthScope))
			cDepth--;

		while(!(cDepthScope.getParentScope() instanceof RootScope) && 
				(cDepth > depth || !isTraceScope(cDepthScope)))
		{
			cDepthScope = cDepthScope.getParentScope();
			if(isTraceScope(cDepthScope)) {
				cDepth--;
			}
		}
		
		return cDepthScope;
	}
	
	
	
	/*************************************
	 * retrieve the list of function names of this call path
	 * 
	 * @return vector of procedure names
	 ************************************/
	public Vector<String> getFunctionNames()
	{
		final Vector<String> functionNames = new Vector<String>();
		Scope currentScope = leafScope;
		//int depth = maxDepth;
		while(currentScope != null && !(currentScope instanceof RootScope))
		{
			if (isTraceScope(currentScope))
			{
				functionNames.add(0, currentScope.getName());
			}
			currentScope = currentScope.getParentScope();
		}
		return functionNames;
	}
	
	
	/*******************************
	 * Retrieve the maximum depth of this call path
	 * 
	 * @return the max depth
	 *******************************/
	public int getMaxDepth()
	{
		return maxDepth;
	}
	
	
	/*******************************
	 * Update the maximum depth of this call path
	 * @param maxDepth
	 *******************************/
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	public Scope getLeafScope() {
		return leafScope;
	}

	public void setLeafScope(Scope leafScope) {
		this.leafScope = leafScope;
	}

	@Override
	public String toString() {
		return maxDepth + ": " + leafScope.getName();
	}
}