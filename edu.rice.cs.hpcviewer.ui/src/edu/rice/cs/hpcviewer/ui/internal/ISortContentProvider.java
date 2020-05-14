package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.viewers.TreeViewerColumn;


/**********************************
 * 
 * Interface of content provider to sort dynamically and lazily
 *  items in a tree view.
 *  
 *  The child class needs to implement sort_begin to start the sorting,
 *  and sort_end to perform clean-up after the sort.
 *  
 *  The sort is performed in synchronous way. There is no plan to interrupt the sort.
 *
 **********************************/
public interface ISortContentProvider 
{

	public void sort_column(TreeViewerColumn sort_column, int direction);
}
