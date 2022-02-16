package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctree.resources.ViewerColorManager;

/**************************************************
 * 
 * Special painter for the line number of a call site
 *
 **************************************************/
public class CallSiteTextPainter extends TextPainter 
{
	private final ScopeTreeDataProvider dataProvider;
	private boolean enabled;
	private DisplayMode displayMode;
	
	/****
	 * Create a call site text painter given the data provider
	 * @param dataProvider
	 */
	public CallSiteTextPainter(ScopeTreeDataProvider dataProvider) {
		super();
		this.dataProvider = dataProvider;
		this.enabled = false;
	}
	
	
	@Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		String text = convertDataType(cell, configRegistry);
		if (text.equals(EMPTY))
			return 0;
		
		return super.getPreferredWidth(cell, gc, configRegistry);
	}
	
	
	@Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		String text = convertDataType(cell, configRegistry);
		if (text.equals(EMPTY))
			return 0;

		return super.getPreferredHeight(cell, gc, configRegistry);
	}
	
	@Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		String text = convertDataType(cell, configRegistry);
		if (text.equals(EMPTY))
			return;
		
		super.paintCell(cell, gc, rectangle, configRegistry);
	}
	
	@Override
	protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
    	LabelStack labels = cell.getConfigLabels();
    	enabled = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
    			  labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER);
    	
    	displayMode = cell.getDisplayMode();

    	int rowIndex = cell.getRowIndex();
		Scope scope = dataProvider.getRowObject(rowIndex);
		if (scope instanceof CallSiteScope) {
			LineScope ls = ((CallSiteScope)scope).getLineScope();
			int ln = ls.getFirstLineNumber();
			if (ln < 1)
				return EMPTY;
			int line = 1 + ln;
			return String.valueOf(line);
		}
		return EMPTY;
	}
    
    
	@Override
    public void setupGCFromConfig(GC gc, IStyle cellStyle) {
    	super.setupGCFromConfig(gc, cellStyle);    	
    	
		Color oldBackgrColor = gc.getBackground();
		Color color = ColorManager.getTextFg(oldBackgrColor);
		// Fix issue #134: do not change the active color if we are in the select mode
    	if (enabled && displayMode != DisplayMode.SELECT) {
			color = ViewerColorManager.getActiveColor(oldBackgrColor);
    	}
    	gc.setForeground(color);
    }
}
