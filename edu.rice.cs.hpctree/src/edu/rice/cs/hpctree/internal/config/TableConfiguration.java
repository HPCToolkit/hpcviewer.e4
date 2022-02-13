package edu.rice.cs.hpctree.internal.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.action.IActionListener;
import edu.rice.cs.hpctree.internal.ScopeAttributeMouseEventMatcher;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;
import edu.rice.cs.hpctree.resources.ViewerColorManager;


/*******************************************************************
 * 
 * Base class to set the configuration of the table and its layers.
 * This class is tightly coupled with {@link ScopeTreeLabelAccumulator}
 * and will use the labels based defined in this class.
 * 
 * @see ScopeTreeLabelAccumulator.LABEL_TOP_ROW
 * @see ScopeTreeLabelAccumulator.LABEL_SOURCE_AVAILABLE
 * @see ScopeTreeLabelAccumulator.LABEL_CALLSITE
 * @see ScopeTreeLabelAccumulator.LABEL_CALLER
 * @see ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED
 * @see ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED
 *
 *******************************************************************/
public class TableConfiguration implements IConfiguration 
{
	private final ScopeTreeDataProvider dataProvider;
	private final Control widget;
	private final List<IActionListener> listeners = new FastList<>();

	/***
	 * General configuration for the profile table
	 * 
	 * @param widget 
	 * 			Any widget that hold the table or even the table itself. 
	 * 			Only used for the coloring to make sure it has a good contrast.
	 * 
	 * @param dataProvider
	 * 			The table data provider. 
	 */
	public TableConfiguration(Control widget, ScopeTreeDataProvider dataProvider) {
		this.widget = widget;
		this.dataProvider = dataProvider;
	}
	
	
	@Override
	public void configureLayer(ILayer layer) {}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {

		// set the style for the top row (experiment aggregate)
		final Style styleTopRow = new Style();
		Color clrBg = ViewerColorManager.getBgTopRow(widget);
		Color clrFg = ViewerColorManager.getFgTopRow(widget);
		
		styleTopRow.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, clrBg);
		styleTopRow.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, clrFg);

		configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					styleTopRow, 
					DisplayMode.NORMAL, 
					ScopeTreeLabelAccumulator.LABEL_TOP_ROW);
		
		// set the "clickable" cell. It' clickable if the cell has the source code to display
		final Style styleActive = new Style();
		Color clrActive = ViewerColorManager.getActiveColor(widget.getBackground());
		styleActive.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, clrActive);
		configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
					styleActive, 
					DisplayMode.NORMAL, 
					ScopeTreeLabelAccumulator.LABEL_SOURCE_AVAILABLE);
		
		// Issue #142: the display converted is needed for the "Find" feature
		// without this attribute, it always fails to find a text
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, 
											   new DefaultDisplayConverter(), 
											   DisplayMode.NORMAL, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);
	}

	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {		
		
		List<String> labels = new ArrayList<>(2);
    	labels.add(ScopeTreeLabelAccumulator.LABEL_CALLSITE);
    	labels.add(ScopeTreeLabelAccumulator.LABEL_CALLER);

		ScopeAttributeMouseEventMatcher scopeMatcher = new ScopeAttributeMouseEventMatcher(
				GridRegion.BODY, 
				MouseEventMatcher.LEFT_BUTTON, 
				labels);
		
		uiBindingRegistry.registerSingleClickBinding(scopeMatcher, (natTable, event) -> {
            NatEventData eventData = NatEventData.createInstanceFromEvent(event);
            int rowIndex = natTable.getRowIndexByPosition(eventData.getRowPosition());	            
            Scope scope = dataProvider.getRowObject(rowIndex);

            if (scope instanceof CallSiteScope) {
            	Scope cs = ((CallSiteScope)scope).getLineScope();
	            listeners.forEach(action -> action.select(cs));
            }
		});

		
		CellLabelMouseEventMatcher mouseMatcher = new CellLabelMouseEventMatcher(
				GridRegion.BODY, 
				MouseEventMatcher.LEFT_BUTTON, 
				ScopeTreeLabelAccumulator.LABEL_SOURCE_AVAILABLE);
		
		uiBindingRegistry.registerSingleClickBinding(mouseMatcher, new IMouseAction() {
			
			@Override
			public void run(NatTable natTable, MouseEvent event) {
	            NatEventData eventData = NatEventData.createInstanceFromEvent(event);
	            int rowIndex = natTable.getRowIndexByPosition(eventData.getRowPosition());	            
	            Scope scope = dataProvider.getRowObject(rowIndex);
	            
	            listeners.forEach(action -> action.select(scope));
			}
		});
	}
	

	/****
	 * Add a click listener to the icon or text when the source file is available.
	 * 
	 * @param action
	 * 	 to be executed when a user clicks on a node when its source is available
	 */
	public void addListener(IActionListener action) {
		listeners.add(action);
	}
	
	
	/*****
	 * Remove a click listener
	 * @param action
	 */
	public void removeListener(IActionListener action) {
		listeners.remove(action);
	}
}
