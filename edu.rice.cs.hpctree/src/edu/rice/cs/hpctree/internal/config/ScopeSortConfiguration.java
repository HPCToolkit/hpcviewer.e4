// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.event.ColumnHeaderClickEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.internal.ScopeSortColumnAction;

/******************************************************************
 * 
 * Sort configuration to ensure we preserve the selected row (if exist)
 *
 ******************************************************************/
public class ScopeSortConfiguration extends DefaultSortConfiguration 
{
	private final ScopeTreeTable table;
	
	public ScopeSortConfiguration(ScopeTreeTable table) {
		this.table = table;
	}
	
    /**
     * Remove the original key bindings and implement new ones.
     */
    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Register new bindings
        uiBindingRegistry.registerFirstSingleClickBinding(
                new ColumnHeaderClickEventMatcher(SWT.NONE, 1),
                new ScopeSortColumnAction(table, false));

        uiBindingRegistry.registerSingleClickBinding(
                MouseEventMatcher.columnHeaderLeftClick(SWT.MOD3),
                new ScopeSortColumnAction(table, true));
    }
}
