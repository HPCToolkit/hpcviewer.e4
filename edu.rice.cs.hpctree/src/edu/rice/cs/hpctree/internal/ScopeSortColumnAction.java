// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.sort.action.SortColumnAction;
import org.eclipse.swt.events.MouseEvent;

import org.hpctoolkit.db.local.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeTable;

/*******************************************************
 * 
 * Special class to sort a column and restore the selected row.
 * The default sort column action doesn't restore properly 
 * the selected row after the sort.
 * 
 * @see
 * issue #155 about the bug {@link https://github.com/HPCToolkit/hpcviewer.e4/issues/155}  
 * 
 *
 ******************************************************/
public class ScopeSortColumnAction extends SortColumnAction 
{
	private final ScopeTreeTable table;

	/****
	 * Create action object to sort a column
	 * 
	 * @param table
	 * @param accumulate
	 */
	public ScopeSortColumnAction(ScopeTreeTable table, boolean accumulate) {
		super(accumulate);
		this.table = table;
	}

    @Override
    public void run(NatTable natTable, MouseEvent event) {
    	// store the old selection
    	Scope scope = table.getSelection();
    	
    	// do the sort
    	super.run(natTable, event);
    	
    	// clear the selection
    	table.clearSelection();
    	
    	// restore the selection
    	int row = table.indexOf(scope);
    	table.setSelection(row);
    }	
}
