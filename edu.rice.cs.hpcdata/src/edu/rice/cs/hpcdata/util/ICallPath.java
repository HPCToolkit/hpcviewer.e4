package edu.rice.cs.hpcdata.util;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface ICallPath 
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
	
	
	/****
	 * Retrieve the call path object for a given context id
	 * 
	 * @param id
	 * 			The context id (or cpid in the old database)
	 * @return {@code ICallPathInfo}
	 * 			The info object of the call path associated with the context id
	 */
	public ICallPathInfo getCallPathInfo(int id);
	
	
	/****
	 * Retrieve the scope object of a given context id.
	 * This is a shortcut of {@code getCallPathInfo(id).getScope()}.
	 *  
	 * @param id
	 * 			The context id
	 * @return {@code Scope}
	 * 			The scope object
	 */
	public Scope getCallPathScope(int id);
	
	
	/*****
	 * Retrieve the maximum depth of a given context id.
	 * This is a shortcut of {@code getCallPathInfo(id).getMaxDepth()}.
	 *  
	 * @param id
	 * 			The context id
	 * @return {@code int}
	 * 			The maximum depth
	 */
	public int getCallPathDepth(int id);
	
	
	/***
	 * Retrieve the scope of a certain depth if exists given a context id.
	 * Usually the context id is the id of the leaf scope, and one may want
	 * to retrieve the scope node of a certain depth.
	 * <p>
	 * This is a shortcut of {@code getCallPathInfo(id).getScopeAt(depth)}
	 * 
	 * @param id
	 * 			The context id
	 * @param depth
	 * 			The depth to inquire
	 * @return {@code Scope}
	 * 			The scope of the depth if exists. null otherwise.
	 */
	public Scope getScopeAt(int id, int depth);
	
	
	/*****
	 * Get the list of all procedures in the given context id.
	 * 
	 * @param id
	 * @return {@code List} of {@code String}
	 */
	public List<String> getFunctionNames(int id);
	
	
	/*****
	 * 
	 * The interface to represent an object of a call path.
	 * Usually it contains just a scope and its maximum depth. 
	 *
	 */
	public interface ICallPathInfo 
	{
		public Scope getScope();
		public int   getMaxDepth();
		public Scope getScopeAt(int depth);
	}
}
