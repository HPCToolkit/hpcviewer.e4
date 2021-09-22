package edu.rice.cs.hpctree.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.action.IActionListener;
import edu.rice.cs.hpctree.resources.IconManager;

public class TableConfiguration implements IConfiguration 
{
	private final ScopeTreeDataProvider dataProvider;
	private final List<ICellPainter> painters = new FastList<>();
	private final List<IActionListener> listeners = new FastList<>();

	public TableConfiguration(ScopeTreeDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	
	@Override
	public void configureLayer(ILayer layer) {}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		painters.addAll( addIconLabel(configRegistry, IconManager.Image_CallTo, ScopeTreeLabelAccumulator.LABEL_CALLSITE) );
		addIconLabel(configRegistry, IconManager.Image_CallToDisabled, ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);

		painters.addAll( addIconLabel(configRegistry, IconManager.Image_CallFrom, ScopeTreeLabelAccumulator.LABEL_CALLER) );
		addIconLabel(configRegistry, IconManager.Image_CallFromDisabled, ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED);
		

		final Style styleActive = new Style();
		styleActive.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLUE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
					styleActive, 
					DisplayMode.NORMAL, 
					ScopeTreeLabelAccumulator.LABEL_SOURCE_AVAILABLE);
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {		
		
		List<String> labels = new ArrayList<>(2);
    	labels.add(ScopeTreeLabelAccumulator.LABEL_CALLSITE);
    	labels.add(ScopeTreeLabelAccumulator.LABEL_CALLER);

		ScopeAttributeMouseEventMatcher scopeMatcher = new ScopeAttributeMouseEventMatcher(
				GridRegion.BODY, 
				MouseEventMatcher.LEFT_BUTTON, 
				labels, 
				painters);
		
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
	
	

	public void addListener(IActionListener action) {
		listeners.add(action);
	}
	
	public void removeListener(IActionListener action) {
		listeners.remove(action);
	}

	
	
	private List<ICellPainter> addIconLabel(IConfigRegistry configRegistry, 
											String  imageName, 
											String  label) {
		
		IconManager iconManager = IconManager.getInstance();
		
		ImagePainter imagePainter = new ImagePainter(iconManager.getImage(imageName));
		ScopeAttributePainter att = new ScopeAttributePainter(dataProvider);
		
		CellPainterDecorator attPainter  = new CellPainterDecorator(imagePainter, CellEdgeEnum.RIGHT, att);
		CellPainterDecorator cellPainter = new CellPainterDecorator(new TextPainter(), CellEdgeEnum.LEFT, attPainter);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   cellPainter, 
											   DisplayMode.NORMAL, 
											   label);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   cellPainter, 
											   DisplayMode.SELECT, 
											   label);
		List<ICellPainter> listPainter = new ArrayList<>(2);
		listPainter.add(imagePainter);
		listPainter.add(att);
		
		return listPainter;
	}
}
