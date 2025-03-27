// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.action;

import java.util.ArrayDeque;
import java.util.Deque;
import org.hpctoolkit.db.local.experiment.scope.CallSiteScope;
import org.hpctoolkit.db.local.experiment.scope.CallSiteScopeFlat;
import org.hpctoolkit.db.local.experiment.scope.ProcedureScope;
import org.hpctoolkit.db.local.experiment.scope.Scope;
import edu.rice.cs.hpctree.FlatScopeTreeData;
import edu.rice.cs.hpctree.IScopeTreeAction;
import edu.rice.cs.hpctree.action.IUndoableActionManager.IUndoableActionListener;

public class FlatAction implements IUndoableActionListener
{
	private static final String CONTEXT = "Flat";
	
	private final IUndoableActionManager actionManager;
	private final IScopeTreeAction treeAction;
	
	private Deque<Scope> 	  stackFlatNodes;
	private Deque<Integer>    currentLevel;
	private FlatScopeTreeData treeData;
	
	
	/****
	 * Create a flat action performed for a specific tree.
	 * 
	 * @param actionManager
	 * 			{@code IUndoableActionManager} an action manager to store undo/redo actions
	 * 
	 * @param treeAction
	 * 			{@code IScopeTreeAction} an object to allow actions on tree
	 */
	public FlatAction(IUndoableActionManager actionManager, IScopeTreeAction treeAction) {
		this.actionManager  = actionManager;
		this.treeAction     = treeAction;
		this.stackFlatNodes = new ArrayDeque<>();
		this.currentLevel   = new ArrayDeque<>();
		currentLevel.push(0);
		
		actionManager.addActionListener(ZoomAction.CONTEXT, this);
	}
	
	
	/*******
	 * Main action to flatten of a given root scope. If the root has no kids,
	 * no modification is executed.<br/>
	 * Flatten means:
	 * <ul>
	 *  <li> remove one level of the children of the root
	 *  <li> the grand children of the root become its children.
	 * </ul>
	 * @param root
	 */
	public void flatten(Scope root) {
		if (!canFlatten())
			return;
		
		// -------------------------------------------------------------------
		// copy the "root" of the current input
		// -------------------------------------------------------------------
		Scope objFlattenedNode = root.duplicate();
		root.copyMetrics(objFlattenedNode, 0);
		
		boolean updateTable = false;

		// create the list of flattened node
		for (var node: root.getChildren()) {
			boolean addSelf = true;
			
			if(node.hasChildren()) {
				addSelf = false;
				
				// this node has children, add the children
				for (var child: node.getChildren()) {
					if (child instanceof CallSiteScopeFlat) {
						CallSiteScopeFlat csFlat = (CallSiteScopeFlat) child;
						Scope parent = csFlat.getProcedureScope();
						
						// check if the tree has been zoomed-in or not.
						// - if the tree has never been zoomed, it means they share the same ancestor
						// - if the root zoom-in is not the ancestor of the procedure scope,
						//     it means the child nodes of the procedure scope won't be visible if
						//     we flatten this scope.
						//     To make sure this scope is visible, we'll include it in the 
						//     flattened tree.
						for (; parent != null && parent.getCCTIndex() != 0; parent = parent.getParentScope());
						
						if (parent == null || !csFlat.cctHasChildren()) {
							// it doesn't have the same ancestor between the call site and the procedure:
							// 	add to the tree
							addNode(objFlattenedNode, child);
						}
					} else if (!(child instanceof CallSiteScope))
						// old xml database: no call site as a leaf
						addNode(objFlattenedNode, child);
				}
				// we only update the table if there are one or more grand child nodes
				// move to one level up.
				updateTable = true;
			} else if (node instanceof ProcedureScope) {
				addSelf = false;
			}
			if (addSelf){
				// no children: add the node itself !
				addNode(objFlattenedNode, node);
			}
		}
		
		if(updateTable && objFlattenedNode.hasChildren()) {
			stackFlatNodes.push(root);
			
			int level = currentLevel.pop();
			level++;
			currentLevel.push(level);
			
			treeAction.setRoot(objFlattenedNode);
			treeData.setCurrentLevel(level);
			
			treeAction.traverseOrExpand(0);
			actionManager.push(CONTEXT);
		}
	}
	
	
	private void addNode(Scope parent, Scope child) {
		parent.addSubscope(child);
	}
	
	
	/*****
	 * Undo the flattened tree.
	 * 
	 * @return {@code boolean}
	 * 			true if the action can be done sucessfully.
	 */
	public boolean unflatten() {
		if (!canUnflatten())
			return false;
		
		Scope objParentNode = stackFlatNodes.pop();
		if(objParentNode == null) 
			return false;

		int level = currentLevel.pop();
		level--;
		assert(level >= 0);
		currentLevel.push(level);
		
		treeAction.setRoot(objParentNode);
		treeData.setCurrentLevel(level);
		
		treeAction.traverseOrExpand(0);
		actionManager.undo();
		
		return true;
	}
	
	
	public boolean canFlatten() {
		Scope root = treeAction.getRoot();
		var children = root.getChildren();
		var hasGrandChildren = children.stream().filter(Scope::hasChildren).findAny();
		return hasGrandChildren.isPresent();
	}
	
	
	public boolean canUnflatten() {
		if (!actionManager.canUndo(CONTEXT))
			return false;
		
		return !stackFlatNodes.isEmpty();
	}


	public void setTreeData(FlatScopeTreeData treeData) {
		this.treeData = treeData;
	}


	@Override
	public void actionPush(String context) {
		int level = 0;
		currentLevel.push(level);
		treeData.setCurrentLevel(level);
	}


	@Override
	public void actionUndo(String context) {
		currentLevel.pop();
		treeData.setCurrentLevel(currentLevel.peek());
	}


	@Override
	public void actionClear() {
		currentLevel.clear();
		currentLevel.push(0);
		stackFlatNodes.clear();
	}
}
