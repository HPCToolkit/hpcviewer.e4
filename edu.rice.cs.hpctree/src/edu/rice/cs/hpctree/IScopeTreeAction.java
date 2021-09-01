package edu.rice.cs.hpctree;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

public interface IScopeTreeAction 
{
	public void refresh();
	
	/***
	 * Expand one level the node of index  
	 * @param index the index of the node to expand 
	 */
	public void traverseOrExpand(int index);
	
	/***
	 * Expand one level a scope node and return the list
	 * of sorted children
	 * @param scope the parent scope
	 * @return
	 */
	public List<? extends TreeNode> traverseOrExpand(Scope scope);
	
	public void setRoot(Scope scope);
	
	public Scope getRoot();
	
	public Scope getSelection();
	
	public int getSortedColumn();
}
