package edu.rice.cs.hpcviewer.ui.internal;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.util.ScopeComparator;
import edu.rice.cs.hpcviewer.ui.base.ISortContentProvider;

/********************************
 * 
 * Base class of content provider of all views
 * All the children need to implement hasChildren method
 *
 ********************************/
public abstract class AbstractContentProvider
	implements ILazyTreeContentProvider, ISortContentProvider 
{
    final private TreeViewer viewer;
	final private HashMap<Integer, Object[]> sort_scopes;
	final private ScopeComparator comparator;
	
	private TreeViewerColumn sort_column = null;
	private int sort_direction 			 = 0;

    public AbstractContentProvider(TreeViewer viewer) {
    	this.viewer = viewer;
		sort_scopes = new HashMap<Integer, Object[]>();
		comparator  = new ScopeComparator();
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
    	if(element instanceof Scope)
            return ((Scope) element).getParent();
    	
    	return null;
    } 


	@Override
	public void updateElement(Object parent, int index) {

		int child_position = index;
		/*
		 * this part will cause stack overflow on Java 11
		 * 
		if (parent == viewer.getInput()) {
			if (index == 0)
 				return;

			// if the parent is a root, the first row is a header
			// this header row is not counted as a child 
			// issue #11: force the index to be 0. We cannot allow negative index.
			child_position = Math.max(0, index-1);
		}*/
		
		Object element = getSortedChild( (Scope)parent, child_position);
		if (element != null) {
			viewer.getTree().setRedraw(false);
			viewer.replace(parent, index, element);
			viewer.getTree().setRedraw(true);
			updateChildCount(element, -1);
		}
	} 

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		assert(element instanceof Scope); 

		Object []children = getSortedChildren((Scope)element);
		int length = (children == null ? 0 : children.length);
		try {
			viewer.setChildCount(element, length);
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Cannot update the child count " + element.getClass() + ": "+ element.toString(), e);
		}
	}

    
    @Override
    public void sort_column(TreeViewerColumn sort_column, int direction) {
    	
    	this.sort_column    = sort_column;
    	this.sort_direction = direction;
    	
		sort_scopes.clear();

    	// perform the sort by refreshing the viewer
    	// this refresh method will force the table to recompute the children
    	
    	viewer.refresh();
    }

    
    /****
     * get the current viewer
     * @return TreeViewer
     */
    protected TreeViewer getViewer() {
    	return viewer;
    }
    
    /**
     * Return an array of objects which is the children of the node (if exists)
     * to be implemented by derived class.
     * 
     * @param node
     * @return
     */
    abstract public Object[] getChildren(Object node);
    
    
    //////////////////////////////////////////
    // private methods
    //////////////////////////////////////////
    
    
    /***
     * return the sorted list of children 
     * 
     * @param parent
     * @return
     */
    public Object[] getSortedChildren(Scope parent) {
		
    	if (parent instanceof ProcedureScope) {
    		ProcedureScope proc = (ProcedureScope) parent;
    		if (proc.toBeElided())
    			return null;
    	}
		int hash = System.identityHashCode(parent);
    	Object [] children = sort_scopes.get(hash);
    	
    	if (children == null && sort_column != null) {
    		children = getChildren(parent);
    		if (children == null)
    			return null;
    		
    		BaseMetric metric = (BaseMetric) sort_column.getColumn().getData();
    		comparator.setMetric(metric);    		
    		comparator.setDirection(sort_direction);
    		
    		Arrays.sort(children, comparator);
    		
    		sort_scopes.put(hash, children);
    	}
    	return children;
	}
	
    /***
     * Retrieve a child of a parent for a specific sorted index.
     * 
     * @param parent
     * @param index
     * @return
     */
	private Object getSortedChild(Scope parent, int index) {
    	if (index < 0)
    		return null;
    	
    	Object []children = getSortedChildren(parent);
    	if (children != null && index < children.length)
    		return children[index];
    	
    	return null;
    }
}
