package edu.rice.cs.hpcviewer.ui.actions;

import java.util.Stack;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

public class FlatScopeAction 
{
	
	private Stack<Scope> 	   stackFlatNodes;
	private Stack<Object[]>    stackExpandedNodes;
	
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
	 */
	public void flatten() {
		// save the current root scope
		Scope objParentNode = (Scope) treeViewer.getInput();
		
		// -------------------------------------------------------------------
		// copy the "root" of the current input
		// -------------------------------------------------------------------
		Scope objFlattenedNode = (objParentNode.duplicate());

		objParentNode.copyMetrics(objFlattenedNode, 0);
		
		boolean hasKids = false;

		// create the list of flattened node
		for (int i=0;i<objParentNode.getChildCount();i++) {
			Scope node =  (Scope) objParentNode.getChildAt(i);
			if(node.getChildCount()>0) {
				
				// this node has children, add the children
				for (Object child: node.getChildren()) {
					Scope childNode = (Scope) child;
					if (!(childNode instanceof CallSiteScope)) {
						objFlattenedNode.add(childNode);
						if (childNode.getName().length()==0) {
							System.out.println("no name: " + childNode.getClass().getName()+" fid: " + childNode.getFlatIndex());
						}
					}
				}
				//addChildren(node, objFlattenedNode);
				hasKids = true;
			} else {
				// no children: add the node itself !
				objFlattenedNode.add(node);
			}
		}
		if(hasKids) {
			if (objFlattenedNode.hasChildren()) {
				pushElementStates();
				
				stackFlatNodes.push(objParentNode);

				treeViewer.getTree().setRedraw(false);
				// we update the data of the table
				treeViewer.setInput(objFlattenedNode);
				// refreshing the table to take into account a new data
				treeViewer.refresh();
				
				treeViewer.insertParentNode(objFlattenedNode);

				treeViewer.getTree().setRedraw(true);
			}
		}
	}

	
	/**
	 * Unflatten flattened tree (tree has to be flattened before)
	 */
	public void unflatten() {
		if (stackFlatNodes.isEmpty())
			return;
		
		Scope objParentNode = stackFlatNodes.pop();
		if(objParentNode != null) {
			this.treeViewer.setInput(objParentNode);
			
			treeViewer.insertParentNode(objParentNode);
			
			popElementStates();
			
			objParentNode.setParent(null);
		}
	}

	public boolean canUnflatten() {
		return (!stackFlatNodes.isEmpty());
	}
	

	
	//-----------------------------------------------------------------------
	// 					FLATTEN: PRIVATE METHODS
	//-----------------------------------------------------------------------

	/**
	 * store the current expanded elements into a stack
	 */
	private void pushElementStates() {
		Object []arrNodes = this.treeViewer.getExpandedElements();
		if (stackExpandedNodes == null)
			stackExpandedNodes = new java.util.Stack<Object[]>();

		stackExpandedNodes.push(arrNodes);
	}
	
	/**
	 * recover the latest expanded element into stack
	 */
	private void popElementStates() {
		Object []arrNodes = stackExpandedNodes.pop();
		this.treeViewer.setExpandedElements(arrNodes);
	}	
}
