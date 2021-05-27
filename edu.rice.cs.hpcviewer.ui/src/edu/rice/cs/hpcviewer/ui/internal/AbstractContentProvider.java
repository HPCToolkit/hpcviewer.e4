package edu.rice.cs.hpcviewer.ui.internal;

import java.util.Arrays;
import java.util.LinkedHashSet;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.ScopeComparator;
import edu.rice.cs.hpcviewer.ui.base.ISortContentProvider;
import edu.rice.cs.hpcviewer.ui.util.SortColumn;

/********************************
 * 
 * Base class of content provider of all views
 * All the children need to implement hasChildren method
 *
 ********************************/
public abstract class AbstractContentProvider
	implements ILazyTreeContentProvider, ISortContentProvider 
{
	final int CACHE_SIZE = 10;

	final private TreeViewer viewer;
	final private ScopeComparator comparator;
	
	/** Cache to store the sorted children.
	 *  Every time Jface table reconstruct a table item, it requires
	 *  every item to check the children a lot and sometimes repeatedly.
	 *  To avoid such re-sorting the children, we need to cache them 
	 *  in a hash map here. It will require more memory but we save time.*/
	final private LinkedHashSet<SortNodeKey> cache_nodes;
	
    public AbstractContentProvider(TreeViewer viewer) {
    	this.viewer = viewer;
		comparator  = new ScopeComparator();

		// To save memory, we only store CACHE_SIZE of nodes
		cache_nodes = new LinkedHashSet<SortNodeKey>(CACHE_SIZE);
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
			try {
				viewer.getTree().setRedraw(false);
				viewer.replace(parent, index, element);
				updateChildCount(element, -1);
			} finally {
				viewer.getTree().setRedraw(true);
			}
		}
	} 

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		assert(element instanceof Scope); 

		Object []children = getRawChildren((Scope)element);
		int length = (children == null ? 0 : children.length);
		try {
			viewer.setChildCount(element, length);
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Cannot update the child count " + element.getClass() + ": "+ element.toString(), e);
		}
	}

    
    /*****
     * Sort a column
     * @param sort_column column to be sorted
     * @param direction SWT sort direction. Use {@code SortColumn} class to convert to SWT direction
     * 
     * @see SWT.UP, SWT.DOWN
     */
	@Override
    public void sort_column(TreeColumn sort_column, int direction) {

    	sort_column.getParent().setSortDirection(direction);		
    	sort_column.getParent().setSortColumn(sort_column);

    	Tree tree    = viewer.getTree();
    	
    	// perform the sort by refreshing the viewer
    	// this refresh method will force the table to recompute the children
    	try {
    		tree.setRedraw(false);

        	// need to select the top of the tree to avoid refreshing the whole
        	// tree in the table
        	TreeItem top = tree.getItem(0);
        	tree.setSelection(top);
        	
        	viewer.refresh(null, false);
        	viewer.expandToLevel(2, true);
    	} finally {
    		tree.setRedraw(true);
    	}
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
    	if (viewer == null || parent == null)
    		return null;
    	
    	TreeColumn sort_column = viewer.getTree().getSortColumn();
    	if (sort_column == null)
    		return parent.getChildren();
    	
		BaseMetric metric  = (BaseMetric) sort_column.getData();
    	int swt_direction  = viewer.getTree().getSortDirection();
    	int sort_direction = SortColumn.getSortDirection(swt_direction); 

		// check if this parent has already sorted children or not
    	// if yes, we look at the cache and return the children.
    	SortNodeKey key = new SortNodeKey(sort_direction, metric, parent);
    	
    	if (cache_nodes.contains(key)) {
    		// the cache exist. 
    		// Move the parent to the tail
    		Object []children = null;
    		for (SortNodeKey node: cache_nodes) {
    			if (node.equals(key)) {
    				children = node.children;
    				break;
    			}
    		}
    		key.children = children;
    		
    		// move to the tail
    		if (cache_nodes.remove(key)) 
    			cache_nodes.add(key);
    		return children;
    	}

    	// sort the children of the parent based on the selected metrics
    	// and the column direction
    	Object []children = getRawChildren(parent);
    	if (children == null || children.length == 1) 
    		return children;
    	
    	comparator.setMetric(metric);    		
		comparator.setDirection(sort_direction);
		
		Arrays.sort(children, comparator);
		
		// store to the cache
		// for the sake of performance optimization, we sacrifice the memory
		if (cache_nodes.size() > CACHE_SIZE) {
			SortNodeKey keyEvicted = cache_nodes.iterator().next();
			cache_nodes.remove(keyEvicted);
		}
		// we only store big children to save memory. No need to store all parents.
		key.children = children;
		cache_nodes.add(key);

		/* debug mode
		 * 
		System.out.print("[");
		cache_nodes.forEach(n -> {
			System.out.print( n.hashCode() + ": " + n.children.length + " , ");
		});
		System.out.println("]");
		*/
    	return children;
	}
	
    
    /****
     * Get the unsorted children of a given parent node
     * @param parent
     * @return Object[] array of the children
     */
    private Object[] getRawChildren(Scope parent) {
    	
    	if (parent instanceof ProcedureScope) {
    		ProcedureScope proc = (ProcedureScope) parent;
    		if (proc.toBeElided())
    			return null;
    	}
    	return getChildren(parent);
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
	
	
	/*****************
	 * 
	 * key pair to cache sorted children
	 * The key consists of 3 attributes:
	 * <ul>
	 * <li> sort direction
	 * <li> current sorted metric column
	 * <li> parent tree node
	 * </ul>
	 * The value of the cache:
	 * children of the parent node.
	 * 
	 *****************/
	private static class SortNodeKey 
	{
		// key set:
		int direction;
		BaseMetric metric;
		Scope parent;
		
		// values:
		Object []children;
		
		public SortNodeKey(int direction, BaseMetric metric, Scope parent) {
			this.direction = direction;
			this.metric = metric;
			this.parent = parent;
		}
		
		@Override
		public String toString() {
			return direction + ", " + metric.getDisplayName() + ", " + parent.getName();
		}
		
		@Override
		public int hashCode() {
			// Corner cases: 
			// - if we sort based on the tree column, the metric can be null
			// - if the tree is empty, the parent can be null
			
			int dir = direction & 0x2;
			int met = (metric == null ? 0xFFF   : (metric.getIndex() & 0xFFF)  )  << 2 ;
			int par = (parent == null ? 0xFFFFF : (parent.hashCode() & 0xFFFF) ) << 12;
			return dir | met | par;
		}
		
		@Override
		public boolean equals(Object other) {
			return hashCode() == other.hashCode();
		}
	}
}
