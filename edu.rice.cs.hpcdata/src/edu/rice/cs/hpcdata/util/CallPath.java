package edu.rice.cs.hpcdata.util;

import java.util.List;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope.ProcedureType;

import java.util.Collections;

/*****************************************
 * 
 * Implementation of {@link ICallPath}
 *
 *****************************************/
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
		
		// special prof2: the cpid zero is no activity
		Scope noActivity = new ProcedureScope(null, null, null, 0, 0, Constants.PROC_NO_ACTIVITY, ProcedureType.REGULAR, 0, 0, null, 0);
		Info infoNoActivity = new Info(noActivity);
		mapToInfo.put(0, infoNoActivity);
	}
	
	/*******
	 * Return true if the scope is part of trace scope. False otherwise
	 * @param scope
	 * @return
	 */
	public static boolean isTraceScope(Scope scope) {
		return (scope instanceof CallSiteScope  || 
				scope instanceof ProcedureScope ||
				scope instanceof InstructionScope);
	}
	

	@Override
	public void dispose() {
		mapToInfo.clear();
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
			return Collections.emptyList();
		
		final List<String> functionNames = new FastList<String>();
		Scope currentScope = info.leafScope;

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
	 * Get the trace depth of a scope
	 * 
	 * @param scope
	 * 
	 * @return
	 *******************************/
	public static int getDepth(Scope scope) {
		Scope current = scope;
		int depth = 0;
		
		while(current != null && !(current instanceof RootScope)) {
			if (isTraceScope(current))
				depth++;
			current = current.getParentScope();
		}
		return depth;
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
		
		return getDepth(info.leafScope);
	}

	@Override
	public void addCallPath(int id, Scope scope, int depth) {
		var info = new Info(scope);
		mapToInfo.put(id, info);
	}
	
	
	@Override
	public ICallPathInfo replaceCallPath(int id, Scope scope, int depth) {
		ICallPathInfo info = mapToInfo.remove(id);
		addCallPath(id, scope, depth);
		
		return info;
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
			return String.format("[%s %d]", info.leafScope.getName(), info.leafScope.getCpid());
		}
		return String.format("[%s %d, ... /%d]", info.leafScope, info.leafScope.getCpid(), mapToInfo.size());
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

		public Info(Scope scope) {
			leafScope = scope;
		}

		@Override
		public Scope getScope() {
			return leafScope;
		}

		@Override
		public int getMaxDepth() {
			return CallPath.getDepth(leafScope);
		}

		@Override
		public Scope getScopeAt(int depth) {
			
			int cDepth = getMaxDepth();
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
