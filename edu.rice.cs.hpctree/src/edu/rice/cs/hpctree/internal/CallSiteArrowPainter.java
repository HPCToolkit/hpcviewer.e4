package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/*******************************
 * 
 * Special painter to draw an arrow based on a Unicode character
 *
 *******************************/
public class CallSiteArrowPainter extends TextPainter 
{
	private static final Color bgColorActive = new Color(new RGB(255, 69, 0));
	private static final Color bgColorNonActive = new Color(new RGB(255, 201, 160));
	
	private boolean enabled;
	

	@Override
	protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
    	LabelStack labels = cell.getConfigLabels();
    	
    	enabled = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
    			 labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER);
    	
    	boolean disabled = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED) ||
    					   labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);
    	
    	if (!enabled && !disabled)
    		return EMPTY;
    	
    	boolean callTo = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
    					 labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);

    	if (callTo) 
    		return ViewerPreferenceManager.INSTANCE.getCallToCharacter();
		return ViewerPreferenceManager.INSTANCE.getCallFromCharacter();
	}
	
	@Override
    public void setupGCFromConfig(GC gc, IStyle cellStyle) {
    	super.setupGCFromConfig(gc, cellStyle);
    	Color color = bgColorActive;
    	if (!enabled) {
    		color = bgColorNonActive;
    	}
    	
    	gc.setFont(FontManager.getCallsiteFont());
    	gc.setForeground(color);
    }
}
