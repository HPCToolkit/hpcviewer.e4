package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.gui.SearchDialog;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import edu.rice.cs.hpctree.ScopeTreeTable;

/*********************************************************
 * 
 * Configuration to show the context menu for scope tree table.
 * The context menu contains:
 * <ul>
 * <li>Find : to search a text in the tree column (not the whole table)</li>
 * <li>Copy : to copy the selected row to the clip-board</li>
 * </ul>
 *
 *********************************************************/
public class ContextMenuConfiguration extends AbstractUiBindingConfiguration 
{
	private final Menu menu;
	
	public ContextMenuConfiguration(final ScopeTreeTable scopeTreeTable) {
		this.menu = new Menu(scopeTreeTable.getTable());
		MenuItem findMenu = new MenuItem(menu, SWT.PUSH);
		findMenu.setText("Find");
		findMenu.setAccelerator(SWT.MOD1 + 'f');
		findMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NatTable natTable = scopeTreeTable.getTable();
				SearchDialog searchDialog = new SearchDialog(natTable.getShell(),
	                    					    			 new CellValueAsStringComparator<>(),
	                    					    			 SWT.NONE );
		        searchDialog.setInput(natTable,  null);
		        searchDialog.open();
			}
		});
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(
														SWT.NONE, 
														null, 
														MouseEventMatcher.RIGHT_BUTTON), 
												   new PopupMenuAction(menu));
	}

}
