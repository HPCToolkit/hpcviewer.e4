package edu.rice.cs.hpcviewer.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.base.IUserMessage;
import edu.rice.cs.hpcviewer.ui.internal.AbstractContentProvider;


/***********************
 * 
 * Class to manage traversing the tree to find a hot call path
 *
 ***********************/
public class HotCallPath 
{

	private TreeViewer   treeViewer;
	private IUserMessage lblMessage;
	
    
	public HotCallPath(TreeViewer viewer, IUserMessage lblMessage) {
		this.treeViewer = viewer;
		this.lblMessage = lblMessage;
	}
	
	/**
	 * show the hot path below the selected node in the tree
	 */
	public void showHotCallPath() {
		// find the selected node
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof TreeSelection)) {
			return;
		}
		TreeSelection objSel = (TreeSelection) sel;
		// get the node
		Object node = objSel.getFirstElement();
		if (!(node instanceof Scope)) {
			lblMessage.showErrorMessage("Please select a scope node.");
			return;
		}
		// get the item
		TreeItem item = this.treeViewer.getTree().getSelection()[0];
		// get the selected metric
		TreeColumn colSelected = this.treeViewer.getTree().getSortColumn();
		if((colSelected == null) || colSelected.getWidth() == 0) {
			// the column is hidden or there is no column sorted
			lblMessage.showErrorMessage("Please select a column to sort before using this feature.");
			return;
		}
		// get the metric data
		Object data = colSelected.getData();
		
		if(data instanceof BaseMetric && item != null) {
			BaseMetric metric = (BaseMetric) data;
			// find the hot call path
			boolean is_found = false;
			
			CallPathItem objHotPath = new CallPathItem();
			objHotPath.level = 0;
			objHotPath.path  = objSel.getPaths()[0];
			objHotPath.node  = (Scope) node;
			
			is_found = getHotCallPath((Scope) node, metric, objHotPath);

			try {
				treeViewer.getTree().setRedraw(false);		
				// whether we find it or not, we should reveal the tree path to the last scope
				treeViewer.reveal(objHotPath.path);
				treeViewer.setSelection(new StructuredSelection(objHotPath.path), false);
			} finally {
				treeViewer.getTree().setRedraw(true);
			}

			if(!is_found && objHotPath.node.hasChildren()) {
				lblMessage.showErrorMessage("No hot child.");
			}
		} else {
			lblMessage.showErrorMessage("Please select a metric column !");
		}
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

		AbstractContentProvider content = (AbstractContentProvider)treeViewer.getContentProvider();
		Object []children = content.getSortedChildren(scope);
		
		if (objHotPath == null) objHotPath = new CallPathItem();
		
		objHotPath.level++;
		
		// singly depth first search
		// bug fix: we only drill once !
		if (children != null && children.length > 0) {
			Object o = children[0];
			if(o instanceof Scope) {
				// get the child node
				Scope scopeChild = (Scope) o;

				// compare the value of the parent and the child
				// if the ratio is significant, we stop 
				MetricValue mvParent = metric.getValue(scope);
				MetricValue mvChild  = metric.getValue(scopeChild);
				
				double dParent = MetricValue.getValue(mvParent);
				double dChild  = MetricValue.getValue(mvChild);

				// simple comparison: if the child has "significant" difference compared to its parent
				// then we consider it as hot path node.
				if(dChild >= (0.5 * dParent)) {
					// fix issue #107 (hot path is too 1 level too deep): 
					// 	add the path only if we "meet" the criteria
					objHotPath.path = objHotPath.path.createChildPath(scopeChild);
					objHotPath.node = scopeChild;
					return getHotCallPath(scopeChild, metric, objHotPath);
					
				} else {
					return true;
				}
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
    	TreePath path = null;
    }

}
