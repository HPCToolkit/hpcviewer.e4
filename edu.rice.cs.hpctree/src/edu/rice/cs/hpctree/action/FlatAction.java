package edu.rice.cs.hpctree.action;

import java.util.Stack;

import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.FlatScopeTreeData;
import edu.rice.cs.hpctree.IScopeTreeAction;
import edu.rice.cs.hpctree.action.IUndoableActionManager.IUndoableActionListener;

public class FlatAction implements IUndoableActionListener
{
	private static final String CONTEXT = "Flat";
	
	private final IUndoableActionManager actionManager;
	private IScopeTreeAction treeAction;
	private Stack<Scope> 	 stackFlatNodes;
	private Stack<Integer>   currentLevel;
	private FlatScopeTreeData treeData;
	
	public FlatAction(IUndoableActionManager actionManager, IScopeTreeAction treeAction) {
		this.actionManager = actionManager;
		this.treeAction = treeAction;
		this.stackFlatNodes = new Stack<>();
		currentLevel = new Stack<>();
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
				
				// this node has children, add the children
				for (var child: node.getChildren()) {
					addNode(objFlattenedNode, child);
					addSelf = false;
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
		
		if(updateTable) {
			if (objFlattenedNode.hasChildren()) {
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
	}
	
	
	private void addNode(Scope parent, Scope child) {
		parent.addSubscope(child);
	}
	
	
	public boolean unflatten() {
		if (!actionManager.canUndo(CONTEXT))
			return false;
		
		if (stackFlatNodes.isEmpty())
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
