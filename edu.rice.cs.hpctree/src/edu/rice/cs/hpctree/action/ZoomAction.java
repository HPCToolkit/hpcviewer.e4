// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

/**
 * 
 */
package edu.rice.cs.hpctree.action;

import java.util.Stack;

import org.hpctoolkit.db.local.experiment.scope.Scope;
import edu.rice.cs.hpctree.IScopeTreeAction;
import edu.rice.cs.hpctree.action.IUndoableActionManager.IUndoableActionListener;


/**
 * Class to manage zoom-in and zoom out of a scope
 */
public class ZoomAction implements IUndoableActionListener
{
	public static final String CONTEXT = "Zoom";
	
	// --------------------------------------------------------------------
	//	ATTRIBUTES
	// --------------------------------------------------------------------
    private final Stack<Scope> 		     stackRootTree;
    private final IScopeTreeAction       treeAction;
    private final IUndoableActionManager actionManager;
    
	// --------------------------------------------------------------------
	//	CONSTRUCTORS
	// --------------------------------------------------------------------
	/**
	 * Constructor to prepare zooms
	 * @param treeViewer
	 * @param objGUI
	 */
	public ZoomAction (IUndoableActionManager actionManager, IScopeTreeAction treeAction) {
		this.treeAction    = treeAction;
		this.actionManager = actionManager;
		this.stackRootTree = new Stack<>();

		actionManager.addActionListener(ZoomAction.CONTEXT, this);
	}
	
	// --------------------------------------------------------------------
	//	METHODS
	// --------------------------------------------------------------------
	/**
	 * Zoom in from "old" scope to "new" scope, store the tree description (expanded items) 
	 * if necessary
	 * @param current
	 * @param old
	 */
	public void zoomIn (Scope current) {

		Scope old = treeAction.getRoot();
		
		stackRootTree.push(old); // save the node for future zoom-out
		actionManager.push(CONTEXT);
		
		treeAction.setRoot(current);
		treeAction.traverseOrExpand(0);
	}
	
	/**
	 * zoom out
	 */
	public void zoomOut () {
		if (!actionManager.canUndo(CONTEXT))
			return;
		
		Scope parent; 
		if(!stackRootTree.isEmpty()) {
			// the tree has been zoomed
			parent = stackRootTree.pop();
			actionManager.undo();
			
			treeAction.setRoot(parent);
			treeAction.traverseOrExpand(0);

		} else {
			// case where the tree hasn't been zoomed
			// there must be a bug if the code comes to here !
			parent = treeAction.getRoot();
			throw( new java.lang.RuntimeException("ScopeViewActions - illegal zoomout: "+parent));
		}
	}
	
	/**
	 * Verify if zoom out is possible
	 * @return
	 */
	public boolean canZoomOut () {
		if (!actionManager.canUndo(CONTEXT))
			return false;
		
		boolean bRet = (stackRootTree != null);
		if (bRet) {
			bRet = ( !stackRootTree.isEmpty() );
		}
		return bRet;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	public boolean canZoomIn ( Scope node ) {
		if (node == null)
			return false;
		
		Scope input = treeAction.getRoot();
		if (input == node)
			return false;

		return ( node.hasChildren());
	}

	@Override
	public void actionPush(String context) {}

	@Override
	public void actionUndo(String context) {}

	@Override
	public void actionClear() {
		stackRootTree.clear();
	}
}
