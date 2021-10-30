package edu.rice.cs.hpctree;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

public interface IScopeTreeAction 
{
	public void refresh();
	
	/***
	 * Expand one level the node (if it's collapsed) of index  
	 * @param index the index of the node to expand 
	 */
	public void traverseOrExpand(int index);
	
	/***
	 * Expand one level a scope node (if it's collapsed) and return the list
	 * of sorted children
	 * @param scope the parent scope
	 * @return
	 */
	public List<? extends TreeNode> traverseOrExpand(Scope scope);
	
	/****
	 * Reset the root of the table
	 * @param scope
	 */
	public void setRoot(Scope scope);
	
	
	/****
	 * Get the current root of the table
	 * @return
	 */
	public Scope getRoot();
	
	/***
	 * Get the selected node (if it's selected)
	 * @return the selected node, null if none is selected
	 */
	public Scope getSelection();
	
	/****
	 * Get the current sorted column (zero-based number) 
	 * @return
	 */
	public int getSortedColumn();
}
