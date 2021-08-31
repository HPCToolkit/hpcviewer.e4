package edu.rice.cs.hpctree.internal;

import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.action.IActionListener;

public class TableUIBindingConfiguration extends AbstractUiBindingConfiguration 
{
	private final ScopeTreeDataProvider dataProvider;
	private final List<IActionListener> listeners = new FastList<>();

	public TableUIBindingConfiguration(ScopeTreeDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		CellLabelMouseEventMatcher mouseMatcher = new CellLabelMouseEventMatcher(
				GridRegion.BODY, 
				MouseEventMatcher.LEFT_BUTTON, 
				ScopeTreeLabelAccumulator.LABEL_SOURCE_AVAILABLE);
		
		uiBindingRegistry.registerFirstSingleClickBinding(mouseMatcher, new IMouseAction() {
			
			@Override
			public void run(NatTable natTable, MouseEvent event) {
	            NatEventData eventData = NatEventData.createInstanceFromEvent(event);
	            int rowIndex = natTable.getRowIndexByPosition(eventData.getRowPosition());	            
	            Scope scope = dataProvider.getRowObject(rowIndex);
	            
	            listeners.forEach(action -> action.select(scope));
			}
		});
	}

	public void addListener(IActionListener action) {
		listeners.add(action);
	}
	
	public void removeListener(IActionListener action) {
		listeners.remove(action);
	}
}
