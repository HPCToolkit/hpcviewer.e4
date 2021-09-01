package edu.rice.cs.hpctree.action;


import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;
import edu.rice.cs.hpctree.IScopeTreeAction;
import edu.rice.cs.hpctree.ScopeTreeTable;


public class HotPathAction 
{
	public final static int RET_OK  = 0;
	public final static int RET_ERR = 1;
	
	private final ScopeTreeTable treeAction;
	
	private String errMsg;
	
	public HotPathAction(IScopeTreeAction treeAction) {
		this.treeAction = (ScopeTreeTable) treeAction;
	}

	 
	public String getMessage() {
		return errMsg;
	}
	
	/**
	 * show the hot path below the selected node in the tree
	 */
	public int showHotCallPath() {
		Scope scope = treeAction.getSelection();
		if (!scope.hasChildren()) {
			errMsg = "No children";
			return RET_ERR;
		}
		
		int col = treeAction.getSortedColumn();
		if (col == 0) {
			errMsg = "No metric column is selected";
			return RET_ERR;
		}
		BaseMetric metric = treeAction.getMetric(col);
		CallPathItem objHotPath = new CallPathItem();
		
		if (!getHotCallPath(scope, metric, objHotPath)) {
			errMsg = "No hot path found";
			// return RET_ERR;
		}
		treeAction.redraw();
		int rowIndex = treeAction.indexOf(objHotPath.node);
		treeAction.setSelection(rowIndex);
		
		return RET_OK;
	}

    
    /**
	 * find the hot call path
	 * @param Scope scope
	 * @param BaseMetric metric
	 * @param int iLevel
	 * @param TreePath path
	 * @param HotCallPath objHotPath (caller has to allocate it)
	 */
	private boolean getHotCallPath(Scope scope, BaseMetric metric, CallPathItem objHotPath) {
		if(scope == null || metric == null )
			return false;

		List<? extends TreeNode> children = treeAction.traverseOrExpand(scope);
		
		// singly depth first search
		// bug fix: we only drill once !
		if (children != null && children.size() > 0) {
			// get the highest child node
			Scope scopeChild = (Scope) children.get(0);
			objHotPath.node = scopeChild;

			// compare the value of the parent and the child
			// if the ratio is significant, we stop 
			MetricValue mvParent = metric.getValue(scope);
			MetricValue mvChild  = metric.getValue(scopeChild);
			
			double dParent = MetricValue.getValue(mvParent);
			double dChild  = MetricValue.getValue(mvChild);

			// simple comparison: if the child has "significant" difference compared to its parent
			// then we consider it as hot path node.
			if(dChild < (0.5 * dParent)) {
				return true;
			} else {
				return getHotCallPath(scopeChild, metric, objHotPath);
			}
		}
		// if we reach at this statement, then there is no hot call path !
		return false;
	}
    
    /////////////////////////////////////////////////////////
    ///
    ///  classes
    ///
    /////////////////////////////////////////////////////////
    
    
    static class CallPathItem 
    {
    	// last node iterated
    	Scope node = null;
    	int level  = 0;
    }

}
