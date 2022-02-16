package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/*******************************
 * 
 * Special painter to draw an arrow based on a Unicode character
 *
 *******************************/
public class CallSiteArrowPainter extends BackgroundPainter 
{
	private static final String EMPTY = "";
	private static final Point  EMPTY_SIZE = new Point(0, 0);

    protected boolean calculateByWidth  = true;
    protected boolean calculateByHeight = true;


    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        Point size = getGlyphBound(cell, gc, configRegistry);
        if (size != EMPTY_SIZE) {        	
            return size.x;
        }
        return 0;
    }

    
    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        Point size = getGlyphBound(cell, gc, configRegistry);
        if (size != EMPTY_SIZE) {
            return size.y;
        }
        return 0;
    }


    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        Point size = getGlyphBound(cell, gc, configRegistry);
        if (size != EMPTY_SIZE) {
            int contentHeight = size.y;
            if (this.calculateByHeight && (contentHeight > bounds.height)) {
                int contentToCellDiff = (cell.getBounds().height - bounds.height);
                ILayer layer = cell.getLayer();
                layer.doCommand(new RowResizeCommand(
                        layer,
                        cell.getRowPosition(),
                        contentHeight + contentToCellDiff,
                        true));
            }

            int contentWidth = size.x;
            if (this.calculateByWidth && (contentWidth > bounds.width)) {
                int contentToCellDiff = (cell.getBounds().width - bounds.width);
                ILayer layer = cell.getLayer();
                layer.doCommand(new ColumnResizeCommand(
                        layer,
                        cell.getColumnPosition(),
                        contentWidth + contentToCellDiff,
                        true));
            }
        	Color color = ColorManager.COLOR_ARROW_ACTIVE;
        	if (isDisabled(cell, configRegistry)) {
        		Color oldBackgrColor = gc.getBackground();
        		color = ColorManager.getTextFg(oldBackgrColor);
        	}
        	
        	gc.setFont(FontManager.getCallsiteGlyphFont());
        	gc.setForeground(color);
        	
        	// need to paint the background manually
        	// otherwise the background will be white even when we select the row
            Color originalBackground = gc.getBackground();
            Color backgroundColor = getBackgroundColour(cell, configRegistry);
            if (backgroundColor != null) {
                gc.setBackground(backgroundColor);
            }

            IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
        	String text = getCallsiteGlyph(cell, configRegistry);
        	
            gc.drawText(text, 
                    	bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, size.x),
                    	bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, size.y),
                    	SWT.DRAW_TRANSPARENT );

            gc.setBackground(originalBackground);
        }
    }

    
    /****
     * Retrieve the size of the glyph including the padding.
     * 
     * @param cell
     * @param gc
     * @param configRegistry
     * @return
     */
    protected Point getGlyphBound(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
    	String text = getCallsiteGlyph(cell, configRegistry);
    	if (EMPTY == text)
    		return EMPTY_SIZE;
    	
    	final int PADDING = 2;    	
    	Point size  = gc.stringExtent(text);
    	
    	size.x += PADDING;
    	size.y += PADDING;
    	return size;
    }
    

    /*****
     * Get the symbol representation of the call site. Depending if it's
     * a bottom-up (caller) or top-down (callsite), it return the corrsponding
     * symbol.
     * 
     * @param cell
     * @param configRegistry
     * @return
     */
	private String getCallsiteGlyph(ILayerCell cell, IConfigRegistry configRegistry) {
    	LabelStack labels = cell.getConfigLabels();
    	
    	boolean enabled = isEnabled(cell, configRegistry);
    	boolean disabled = isDisabled(cell, configRegistry);
    			
    	if (!enabled && !disabled)
    		return EMPTY;
    	
    	boolean callTo = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
    					 labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);

    	if (callTo) 
    		return ViewerPreferenceManager.INSTANCE.getCallToGlyph();
		return ViewerPreferenceManager.INSTANCE.getCallFromGlyph();
	}
	
	
	private boolean isEnabled(ILayerCell cell, IConfigRegistry configRegistry) {
    	LabelStack labels = cell.getConfigLabels();
    	return labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
		 	   labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER);
	}
	
	
	private boolean isDisabled(ILayerCell cell, IConfigRegistry configRegistry) {
    	LabelStack labels = cell.getConfigLabels();
    	return labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED) ||
		 	   labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED);
	}
}
