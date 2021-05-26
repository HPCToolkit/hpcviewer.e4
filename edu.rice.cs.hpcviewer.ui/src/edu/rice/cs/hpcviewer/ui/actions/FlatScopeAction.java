package edu.rice.cs.hpcviewer.ui.actions;

import java.util.Stack;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

public class FlatScopeAction 
{
	
	private Stack<Scope> 	   stackFlatNodes;
	final private ScopeTreeViewer   treeViewer;
	
	//-----------------------------------------------------------------------
	// 					METHODS
	//-----------------------------------------------------------------------

	/**
	 * @param treeViewer the tree viewer to flatten/unflatten
	 */
	public FlatScopeAction(ScopeTreeViewer treeViewer) {

		this.treeViewer = treeViewer;
		stackFlatNodes  = new Stack<Scope>();
	}


	//-----------------------------------------------------------------------
	// 					FLATTEN: PUBLIC INTERFACES
	//-----------------------------------------------------------------------
	

	/**
	 * Flatten the tree one level more
	 * @return true if the action is successful, false otherwise
	 */
	public boolean flatten() {
		// save the current root scope
		Scope objParentNode = (Scope) treeViewer.getInput();
		Scope objParentChildNode = (Scope) objParentNode.getChildAt(0);
		
		// -------------------------------------------------------------------
		// copy the "root" of the current input
		// -------------------------------------------------------------------
		Scope objFlattenedNode = (objParentChildNode.duplicate());
		Scope root = objFlattenedNode.createRoot();

		objParentChildNode.copyMetrics(objFlattenedNode, 0);
		
		boolean hasKids = false;

		// create the list of flattened node
		for (int i=0;i<objParentChildNode.getChildCount();i++) {
			Scope node =  (Scope) objParentChildNode.getChildAt(i);
			if(node.getChildCount()>0) {
				
				// this node has children, add the children
				for (Object child: node.getChildren()) {
					Scope childNode = (Scope) child;
					if (!(childNode instanceof CallSiteScope)) {
						objFlattenedNode.add(childNode);
					}
				}
				hasKids = true;
			} else {
				// no children: add the node itself !
				objFlattenedNode.add(node);
			}
		}
		if(hasKids) {
			if (objFlattenedNode.hasChildren()) {
				stackFlatNodes.push(objParentNode);
				try {
					treeViewer.getTree().setRedraw(false);
					// we update the data of the table
					treeViewer.setInput(null);
					treeViewer.setInput(root);
					// expand the tree until reaching level 2
					treeViewer.expandToLevel(2, true);
				} finally {
					treeViewer.getTree().setRedraw(true);
				}
			}
		}
		return (hasKids && objFlattenedNode.hasChildren());
	}

	
	/**
	 * Unflatten a flattened tree (tree has to be flattened before)
	 * @return true if the action is successful, false otherwise
	 */
	public boolean unflatten() {
		if (stackFlatNodes.isEmpty())
			return false;
		
		Scope objParentNode = stackFlatNodes.pop();
		if(objParentNode == null) 
			return false;

		try {
			treeViewer.getTree().setRedraw(false);
			treeViewer.setInput(null);
			treeViewer.setInput(objParentNode);
			treeViewer.expandToLevel(2, true);
		} finally {
			treeViewer.getTree().setRedraw(true);
		}
		
		objParentNode.setParent(null);
		return true;
	}

	/***
	 * Check if the tree can be unflattened by verifying if
	 * it's flattened previously or not.
	 * 
	 * @return true if it's possible to be unflatten
	 */
	public boolean canUnflatten() {
		return (!stackFlatNodes.isEmpty());
	}
}
