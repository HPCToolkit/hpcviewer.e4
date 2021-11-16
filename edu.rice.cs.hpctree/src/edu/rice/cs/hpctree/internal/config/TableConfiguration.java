package edu.rice.cs.hpctree.internal.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctree.action.IActionListener;
import edu.rice.cs.hpctree.internal.ScopeAttributeMouseEventMatcher;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;
import edu.rice.cs.hpctree.resources.ViewerColorManager;
import edu.rice.cs.hpctree.resources.IconManager;


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
	private static final String SUFFIX_LINE  = ": ";

	private final ScopeTreeDataProvider dataProvider;
	private final Control widget;
	
	private final List<ICellPainter> painters     = new FastList<>();
	private final List<IActionListener> listeners = new FastList<>();

	public TableConfiguration(Control widget, ScopeTreeDataProvider dataProvider) {
		this.widget = widget;
		this.dataProvider = dataProvider;
	}
	
	
	@Override
	public void configureLayer(ILayer layer) {}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		painters.addAll( addIconLabel(configRegistry, IconManager.Image_CallTo, ScopeTreeLabelAccumulator.LABEL_CALLSITE, true));
		addIconLabel(configRegistry, IconManager.Image_CallToDisabled, ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED, false);

		painters.addAll( addIconLabel(configRegistry, IconManager.Image_CallFrom, ScopeTreeLabelAccumulator.LABEL_CALLER, true));
		addIconLabel(configRegistry, IconManager.Image_CallFromDisabled, ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED, false);
		
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
		
		final Style styleActive = new Style();
		Color clrActive = ViewerColorManager.getActiveColor();
		styleActive.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, clrActive);
		configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_STYLE, 
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

	
	
	private List<ICellPainter> addIconLabel(IConfigRegistry configRegistry, 
											String  imageName, 
											String  label,
											boolean active) {
		
		IconManager iconManager = IconManager.getInstance();
		
		ImagePainter imagePainter = new ImagePainter(iconManager.getImage(imageName));
		TextPainter  textPainter  = new TextPainter() {
			@Override
			protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
				int rowIndex = cell.getRowIndex();
				Scope scope = dataProvider.getRowObject(rowIndex);
				if (scope instanceof CallSiteScope) {
					LineScope ls = ((CallSiteScope)scope).getLineScope();
					int ln = ls.getFirstLineNumber();
					if (ln < 1)
						return EMPTY;
					int line = 1 + ln;
					return line + SUFFIX_LINE;
				}
				return EMPTY;
			}
			
		    public void setupGCFromConfig(GC gc, IStyle cellStyle) {
		    	super.setupGCFromConfig(gc, cellStyle);
		    	
				Color oldBackgrColor = gc.getBackground();
				Color color = ColorManager.getTextFg(oldBackgrColor);
		    	if (active) {
					color = ViewerColorManager.getActiveColor();		    		
		    	}
		    	gc.setForeground(color);
		    }
		};
		
		CellPainterDecorator attPainter  = new CellPainterDecorator(imagePainter, CellEdgeEnum.RIGHT, textPainter);
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
		listPainter.add(textPainter);
		
		return listPainter;
	}
}
