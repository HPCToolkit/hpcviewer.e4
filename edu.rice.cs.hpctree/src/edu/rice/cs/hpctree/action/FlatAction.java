package edu.rice.cs.hpctree.action;

import java.util.Stack;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.IScopeTreeAction;

public class FlatAction 
{
	private final static String CONTEXT = "Flat";
	
	private final IUndoableActionManager actionManager;
	private IScopeTreeAction treeAction;
	private Stack<Scope> 	 stackFlatNodes;

	
	public FlatAction(IUndoableActionManager actionManager, IScopeTreeAction treeAction) {
		this.actionManager = actionManager;
		this.treeAction = treeAction;
		this.stackFlatNodes = new Stack<>();
	}
	
	public void flatten(Scope root) {
		
		// -------------------------------------------------------------------
		// copy the "root" of the current input
		// -------------------------------------------------------------------
		Scope objFlattenedNode = (root.duplicate());
		root.copyMetrics(objFlattenedNode, 0);
		
		boolean hasKids = false;

		// create the list of flattened node
		for (int i=0;i<root.getChildCount();i++) {
			Scope node =  (Scope) root.getChildAt(i);
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
				stackFlatNodes.push(root);
				treeAction.setRoot(objFlattenedNode);
				treeAction.traverseOrExpand(0);
				actionManager.push(CONTEXT);
			}
		}
	}
	
	public boolean unflatten() {
		if (!actionManager.canUndo(CONTEXT))
			return false;
		
		if (stackFlatNodes.isEmpty())
			return false;
		
		Scope objParentNode = stackFlatNodes.pop();
		if(objParentNode == null) 
			return false;

		treeAction.setRoot(objParentNode);
		treeAction.traverseOrExpand(0);
		actionManager.undo();
		
		return true;
	}
	
	
	public boolean canFlatten() {
		Scope root = treeAction.getRoot();
		return root.hasChildren();
	}
	
	
	public boolean canUnflatten() {
		if (!actionManager.canUndo(CONTEXT))
			return false;
		
		return !stackFlatNodes.isEmpty();
	}
}
