package edu.rice.cs.hpcdata.util;

import java.util.List;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class CallPath implements ICallPath
{
	private final IntObjectHashMap<Info> mapToInfo;
	
	/******
	 * Constructor for call path class.
	 * 
	 * @param scope the scope of the trace. Doesn't have to be a leaf.
	 * @param depth the depth of the scope.
	 */
	public CallPath()
	{
		mapToInfo = new IntObjectHashMap<>();
	}
	
	/*******
	 * Return true if the scope is part of trace scope. False otherwise
	 * @param scope
	 * @return
	 */
	public static boolean isTraceScope(Scope scope) {
		return (scope instanceof CallSiteScope || scope instanceof ProcedureScope);
	}
	


	@Override
	public ICallPathInfo getCallPathInfo(int id) {
		return mapToInfo.get(id);
	}

	@Override
	public Scope getScopeAt(int id, int depth)
	{
		if (depth < 0)
			return null;
		
		var info = mapToInfo.get(id);
		if (info == null)
			return null;
		
		return info.getScopeAt(depth);
	}	
	
	
	/*************************************
	 * retrieve the list of function names of this call path
	 * 
	 * @return {@code List} of procedure names
	 ************************************/
	@Override
	public List<String> getFunctionNames(int id)
	{
		var info = mapToInfo.get(id);
		if (info == null)
			return null;
		
		final List<String> functionNames = new FastList<String>();
		Scope currentScope = info.leafScope;
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
	 * @param id 
	 * 			context id (cpid in old database)
	 * @return {@code int}
	 * 			the max depth from the leaf to the root
	 *******************************/
	@Override
	public int getCallPathDepth(int id)
	{
		var info = mapToInfo.get(id);
		if (info == null)
			return -1;
		
		return info.maxDepth;
	}

	@Override
	public void addCallPath(int id, Scope scope, int depth) {
		var info = new Info(scope, depth);
		mapToInfo.put(id, info);
	}
	

	@Override
	public Scope getCallPathScope(int id) {
		var info = mapToInfo.get(id);
		if (info == null)
			return null;
		
		return info.leafScope;
	}


	@Override
	public String toString() {
		if (mapToInfo.size()==0)
			return "[]";

		Info info = mapToInfo.getFirst();
		if (mapToInfo.size()==1)  {
			return String.format("[%s %d]", info.leafScope.getName(), info.maxDepth);
		}
		return String.format("[%s %d, ... /%d]", info.leafScope, info.maxDepth, mapToInfo.size());
	}
	
	
	/*****
	 * 
	 * The implementation class of {@code ICallPathInfo}.
	 * There is nothing interesting here as it's supposed to be used
	 * just as an internal class.
	 *
	 */
	private static class Info implements ICallPathInfo
	{
		/**the Scope at the current cpid*/
		Scope leafScope;
		
		/**the depth of leafScope (where current cpid is)*/
		int maxDepth;

		public Info(Scope scope, int depth) {
			leafScope = scope;
			maxDepth = depth;
		}

		@Override
		public Scope getScope() {
			return leafScope;
		}

		@Override
		public int getMaxDepth() {
			return maxDepth;
		}

		@Override
		public Scope getScopeAt(int depth) {
			
			int cDepth = maxDepth;
			Scope cDepthScope = leafScope;

			if (cDepthScope.getParentScope() == null)
				return cDepthScope;

			// this is a hack to solve issue #99 (filtering leaf nodes problem)
			// If the leaf node is visible on the trace view, we have to decrement
			// the current depth.
			// The problem with the original code, we assume the leaf node is always
			//  a line scope. Which is not correct for prof2 or filtered trees
			//
			// Example: a -> b -> c -> d where a, b, c, and d are all trace scope
			//          1    2    3    4  depth stored (1-based)
			//          0    1    2    3  depth visualized (0-based)
			//
			// if depth == 3, we should return d instead of c
			if (isTraceScope(cDepthScope))
				cDepth--;
			
			while(  cDepthScope != null &&
					!(cDepthScope instanceof RootScope) && 
					(cDepth > depth || !isTraceScope(cDepthScope)))
			{
				cDepthScope = cDepthScope.getParentScope();
				if(isTraceScope(cDepthScope)) {
					cDepth--;
				}
			}
			
			return cDepthScope;
		}
	}
}
