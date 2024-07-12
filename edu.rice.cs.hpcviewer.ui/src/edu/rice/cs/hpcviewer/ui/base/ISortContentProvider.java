// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.base;

import org.eclipse.swt.widgets.TreeColumn;


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

    /*****
     * Sort a column
     * @param sort_column column to be sorted
     * @param direction SWT sort direction. Use {@code SortColumn} class to convert to SWT direction
     * 
     * @see SWT.UP, SWT.DOWN
     */
	public void sort_column(TreeColumn sort_column, int direction);
}
