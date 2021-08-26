package edu.rice.cs.hpctree;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IScopeTreeAction 
{
	public void refresh();
	
	/***
	 * Expand one level the node of index  
	 * @param index the index of the node to expand 
	 */
	public void expand(int index);
	
	public void setRoot(Scope scope);
	
	public Scope getRoot();
}
