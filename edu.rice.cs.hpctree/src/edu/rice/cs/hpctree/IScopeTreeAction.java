// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IScopeTreeAction 
{
	void refresh();
	
	/***
	 * Expand one level the node (if it's collapsed) of index  
	 * @param index the index of the node to expand 
	 */
	void traverseOrExpand(int index);
	
	/***
	 * Expand one level a scope node (if it's collapsed) and return the list
	 * of sorted children
	 * @param scope the parent scope
	 * @return
	 */
	List<Scope> traverseOrExpand(Scope scope);
	
	/****
	 * Reset the root of the table
	 * @param scope
	 */
	void setRoot(Scope scope);
	
	
	/****
	 * Get the current root of the table
	 * @return
	 */
	Scope getRoot();
	
	/***
	 * Get the selected node (if it's selected)
	 * @return the selected node, null if none is selected
	 */
	Scope getSelection();
	
	/****
	 * Get the current sorted column (zero-based number) 
	 * @return
	 */
	int getSortedColumn();

	
	/****
	 * Export the content of the table to a file
	 */
	void export();
}
