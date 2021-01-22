package edu.rice.cs.hpcviewer.ui.internal;

import java.util.Arrays;

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
	final private ScopeComparator comparator;
	
	private TreeViewerColumn sort_column = null;
	private int sort_direction 			 = 0;

    public AbstractContentProvider(TreeViewer viewer) {
    	this.viewer = viewer;
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
		
		Object element = getSortedChild( (Scope)parent, index);
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
    	Object [] children = getChildren(parent);
    	if (sort_column == null || children == null)
    		return null;
    	
		BaseMetric metric = (BaseMetric) sort_column.getColumn().getData();
		comparator.setMetric(metric);    		
		comparator.setDirection(sort_direction);
		
		Arrays.sort(children, comparator);
		
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
