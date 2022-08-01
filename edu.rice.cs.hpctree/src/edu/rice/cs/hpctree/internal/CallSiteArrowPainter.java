package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctree.resources.ViewerColorManager;


/*******************************
 * 
 * Special painter to draw an arrow based on a Unicode character
 *
 *******************************/
public class CallSiteArrowPainter extends BackgroundPainter 
{
	private static final String EMPTY = "";
	private static final String SPACE = " ";
	private static final int PADDING = 2;    	

	private final ScopeTreeDataProvider dataProvider;

    protected boolean calculateByWidth  = true;
    protected boolean calculateByHeight = true;

    
    /****
     * Create a call site painter wrapper containing a line number text and a call site glyph.
     * The font and the symbol of the call site can be customized in the preference window.
     * 
     * @param dataProvider
     */
    public CallSiteArrowPainter(ScopeTreeDataProvider dataProvider) {
    	this.dataProvider = dataProvider;
	}

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
    	final String csGlyph = getCallsiteGlyph(cell);
    	if (csGlyph.equals(EMPTY))
    		return 0;

    	final Font oldFont = gc.getFont();

    	// compute the width of the call site text (line number)
    	Point sizeText = new Point(0, 0);
    	String csText  = getCallsiteText(cell);
    	if (!csText.equals(EMPTY)) {
        	gc.setFont(FontManager.getFontGeneric());
        	sizeText = gc.stringExtent(SPACE + csText);
    	}
    	
    	// compute the width of the call site symbol
    	gc.setFont(FontManager.getCallsiteGlyphDefaultFont());
    	Point sizeGlyph = gc.stringExtent(csGlyph);
    	
    	gc.setFont(oldFont);
    	
    	return PADDING + sizeGlyph.x + sizeText.x;
    }

    
    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return gc.getFontMetrics().getHeight();
    }


    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
    	String glyph = getCallsiteGlyph(cell);

        if (glyph.equals(EMPTY)) 
        	return;
        
    	Color color = ColorManager.COLOR_ARROW_ACTIVE;
        Color originalBackground = gc.getBackground();
        final boolean isDisabled = isDisabled(cell);
    	if (isDisabled) {
    		color = ColorManager.getTextFg(originalBackground);
    	}
    	Font oldFont = gc.getFont();        
        Font font = FontManager.getFontGeneric();
        gc.setFont(font);
    	
    	// need to paint the background manually
    	// otherwise the background will be white even when we select the row
        Color backgroundColor = getBackgroundColour(cell, configRegistry);
        if (backgroundColor != null) {
            gc.setBackground(backgroundColor);
        }
        
        int fontHeight = gc.getFontMetrics().getHeight();
        int deltaY = (bounds.height - fontHeight) / 2;

        // ------------------------------------------
        // Display the call site line number
        // ------------------------------------------

    	String lineNum = getCallsiteText(cell);
    	if (!lineNum.equals(EMPTY)) {
    		var textColor = ColorManager.getTextFg(originalBackground);
        	var displayMode = cell.getDisplayMode();

        	// Fix issue #134: do not change the active color if we are in the select mode
        	if (!isDisabled && displayMode != DisplayMode.SELECT) {
        		textColor = ViewerColorManager.getActiveColor(originalBackground);
        	}
        	gc.setForeground(textColor);
        	gc.drawText(lineNum, bounds.x + PADDING, bounds.y + deltaY);
    	}
        
    	Point sizeText = lineNum != EMPTY ? gc.stringExtent(lineNum + SPACE) : new Point(0, 0);

        // ------------------------------------------
        // Display the call site symbols (the arrow)
        // ------------------------------------------

    	gc.setFont(FontManager.getCallsiteGlyphFont());
    	gc.setForeground(color);

    	var size = gc.stringExtent(glyph);
    	deltaY = (bounds.height-size.y)/2;

    	// Do not use SWT.TRANSPARENT in this gc
    	// that flag will make all arrows the same color!
        gc.drawText(glyph, 
                	bounds.x + PADDING + sizeText.x,
                	bounds.y + deltaY);
    	
        gc.setBackground(originalBackground);
    	gc.setFont(oldFont);
    }
    

    /*****
     * Get the symbol representation of the call site. Depending if it's
     * a bottom-up (caller) or top-down (callsite), it return the corrsponding
     * symbol.
     * 
     * @param cell
     * @return
     */
	private String getCallsiteGlyph(ILayerCell cell) {
    	LabelStack labels = cell.getConfigLabels();
    	
    	boolean enabled = isEnabled(cell);
    	boolean disabled = isDisabled(cell);
    			
    	if (!enabled && !disabled)
    		return EMPTY;
    	
    	boolean callTo = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
    					 labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);

    	if (callTo) 
    		return ViewerPreferenceManager.INSTANCE.getCallToGlyph();
		return ViewerPreferenceManager.INSTANCE.getCallFromGlyph();
	}
	
	
	private String getCallsiteText(ILayerCell cell) {
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

	
	private boolean isEnabled(ILayerCell cell) {
    	LabelStack labels = cell.getConfigLabels();
    	return labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
		 	   labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER);
	}
	
	
	private boolean isDisabled(ILayerCell cell) {
    	LabelStack labels = cell.getConfigLabels();
    	return labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED) ||
		 	   labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED);
	}
}
