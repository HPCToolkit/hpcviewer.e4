package edu.rice.cs.hpcdata.util;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface ICallPathInfo 
{
	/****
	 * Add a call path info of a given context id into the {@code ICallPathInfo} data
	 * @param id
	 * 			The context id (or cpid in the old database)
	 * @param scope
	 * 			The leaf scope or the scope object of the context id
	 * @param depth
	 * 			The maximum depth from the scope to the root
	 */
	public void addCallPath(int id, Scope scope, int depth);
	
	
	public IScopeDepth getScopeDepth(int id);
	
	public Scope getCallPathScope(int id);
	
	public int getCallPathDepth(int id);
	
	public Scope getScopeAt(int id, int depth);
	
	public List<String> getFunctionNames(int id);
	
	public interface IScopeDepth 
	{
		public Scope getScope();
		public int   getMaxDepth();
		public Scope getScopeAt(int depth);
	}
}
