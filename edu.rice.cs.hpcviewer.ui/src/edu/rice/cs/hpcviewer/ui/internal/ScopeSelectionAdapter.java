package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
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
		int sort_direction  = SWT.DOWN;
		TreeColumn oldColumnSort = column.getColumn().getParent().getSortColumn();

		if (oldColumnSort == column.getColumn()) {
			// we click the same column: want to change the sort direction
			if (column.getColumn().getParent().getSortDirection() == SWT.DOWN)
				sort_direction = SWT.UP;
		}
		setSorter(sort_direction);
		
		// ----------------
		// post-sorting 
		// ----------------
		
		// Issue #34: mac requires to delay the selection after sort
		/*
		tree.getDisplay().asyncExec(() -> {
			
			// issue #36
			// Linux/GTK only: if a user already select an item, we shouldn't expand it
			//
			// issue #34 (macOS only): we need to refresh and expand the table after sorting
			// otherwise the tree items are not visible
			if (!OSValidator.isMac() && tree.getSelectionCount() > 0) {
				return;
			}
			
			try {
				// Hack: use the tree viewer set selection to fire selection event
				// in jface so that the view can update the buttons (enabled/disabled)
				// if we use tree's set selection, there's no selection event fired.
				
				//((ScopeTreeViewer)viewer).initSelection(-1);
				TreeItem item = viewer.getTree().getItem(0);
				((ScopeTreeViewer)viewer).setSelection(new StructuredSelection(item.getData()));
			} catch (Exception exc) {
			}
		});
		*/
	}
	
	/**
	 * Sort the column according to the direction
	 * @param direction The value has to be either {@code SWT.UP} or {@code SWT.DOWN}
	 */
	private void setSorter(int direction) {
		// bug Eclipse no 199811 https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
		// sorting can be very slow in mac OS
		// we need to manually disable redraw before comparison and the refresh after the comparison 
		try {
			viewer.getTree().setRedraw(false);
			
			TreeColumn col = column.getColumn();
			
			// prepare the sorting for this column with a specific direction
			ISortContentProvider sortProvider = (ISortContentProvider) viewer.getContentProvider();
			
			 // start sorting
			sortProvider.sort_column(col, direction);
		} finally {
			viewer.getTree().setRedraw(true);
		}
	}	
}
