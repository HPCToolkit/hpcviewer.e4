package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import edu.rice.cs.hpc.data.util.ScopeComparator;
import edu.rice.cs.hpcviewer.ui.base.ISortContentProvider;


/*********************************************************
 * 
 * Class to handle column header selection (a.k.a sort event)
 *
 *********************************************************/
public class ScopeSelectionAdapter extends SelectionAdapter 
{
	final private TreeViewer viewer;
	final private TreeViewerColumn column;

	
    public ScopeSelectionAdapter(TreeViewer viewer, TreeViewerColumn column) {
		this.viewer 	= viewer;
		this.column     = column;
	}
	
	public void widgetSelected(SelectionEvent e) {
		
		// ----------------
		// pre-sorting : 
		// we don't want to sort all expanded items, including unwanted items
		// ----------------
		
		// before sorting, we need to check if the first row is an element header 
		// something like "aggregate metrics" or zoom-in item
		Tree tree = viewer.getTree();
		if (tree.getItemCount()==0)
			return; // no items: no need to sort
		
		// ----------------
		// sorting 
		// ----------------
		int sort_direction  = ScopeComparator.SORT_DESCENDING;
		TreeColumn oldColumnSort = column.getColumn().getParent().getSortColumn();

		if (oldColumnSort == column.getColumn()) {
			// we click the same column: want to change the sort direction
			int swt_direction = column.getColumn().getParent().getSortDirection();

			if (swt_direction == SWT.DOWN)
				sort_direction = ScopeComparator.SORT_ASCENDING;
		}
		setSorter(sort_direction);
		
		// ----------------
		// post-sorting 
		// ----------------
		((ScopeTreeViewer)viewer).initSelection();
	}
	
	/**
	 * Sort the column according to the direction
	 * @param direction The value has to be either {@code SORT_DESCENDING} or {@code SORT_ASCENDING}
	 */
	public void setSorter(int direction) {
		// bug Eclipse no 199811 https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
		// sorting can be very slow in mac OS
		// we need to manually disable redraw before comparison and the refresh after the comparison 
				
		viewer.getTree().setRedraw(false);
		
		int swt_direction = SWT.NONE;
		
		if( direction == ScopeComparator.SORT_DESCENDING ) {
			swt_direction = SWT.DOWN;
		} else if( direction == ScopeComparator.SORT_ASCENDING ) {
			swt_direction = SWT.UP;
		} else {
			// incorrect value. Let's try to be permissive instead of throwing exception
			direction = 0;
		}
		
		TreeColumn col = column.getColumn();
		col.getParent().setSortDirection(swt_direction);		
		col.getParent().setSortColumn(col);
		
		// prepare the sorting for this column with a specific direction
		ISortContentProvider sortProvider = (ISortContentProvider) viewer.getContentProvider();
		
		 // start sorting
		sortProvider.sort_column(column, direction);

		viewer.getTree().setRedraw(true);
	}	
}
