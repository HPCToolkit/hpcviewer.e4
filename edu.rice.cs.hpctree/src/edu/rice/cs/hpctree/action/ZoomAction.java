/**
 * 
 */
package edu.rice.cs.hpctree.action;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.IScopeTreeAction;


/**
 * Class to manage zoom-in and zoom out of a scope
 */
public class ZoomAction 
{
	// --------------------------------------------------------------------
	//	ATTRIBUTES
	// --------------------------------------------------------------------
    private final java.util.Stack<Scope> stackRootTree;
    private final IScopeTreeAction treeAction;

	// --------------------------------------------------------------------
	//	CONSTRUCTORS
	// --------------------------------------------------------------------
	/**
	 * Constructor to prepare zooms
	 * @param treeViewer
	 * @param objGUI
	 */
	public ZoomAction ( IScopeTreeAction treeAction) {
		this.treeAction = treeAction;
		stackRootTree = new java.util.Stack<Scope>();
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
		
		treeAction.setRoot(current);
		treeAction.expand(0);
	}
	
	/**
	 * zoom out
	 */
	public void zoomOut () {
		Scope parent; 
		if(stackRootTree.size()>0) {
			// the tree has been zoomed
			parent = stackRootTree.pop();
		} else {
			// case where the tree hasn't been zoomed
			// FIXME: there must be a bug if the code comes to here !
			parent = treeAction.getRoot();
			throw( new java.lang.RuntimeException("ScopeViewActions - illegal zoomout: "+parent));
		}
		treeAction.setRoot(parent);
		treeAction.expand(0);
	}
	
	/**
	 * Verify if zoom out is possible
	 * @return
	 */
	public boolean canZoomOut () {
		boolean bRet = (stackRootTree != null);
		if (bRet) {
			bRet = ( stackRootTree.size()>0 );
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
		
		if (node instanceof CallSiteScopeCallerView) {
			// in caller view, we don't know exactly how many children a scope has
			// the most reliable way is to retrieve the "mark" if the scope has a child or not
			return ((CallSiteScopeCallerView)node).hasScopeChildren();
		}
		return ( node.getChildCount()>0 );
	}
}
