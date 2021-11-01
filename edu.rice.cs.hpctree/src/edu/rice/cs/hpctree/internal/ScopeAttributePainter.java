package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpcsetting.color.ColorManager;

public class ScopeAttributePainter extends TextPainter 
{
	private static final String EMPTY_STRING = "";
	private static final String SUFFIX_LINE  = ": ";
	
	private final ScopeTreeDataProvider dataProvider;
	
	public ScopeAttributePainter(ScopeTreeDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	
	@Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
        if (this.paintBg) {
            super.paintCell(cell, gc, rectangle, configRegistry);
        }

        if (this.paintFg) {
    		int rowIndex   = cell.getRowIndex();
    		Scope scope    = dataProvider.getRowObject(rowIndex);
    		
    		if (scope instanceof CallSiteScope) {
                LineScope ls = ((CallSiteScope)scope).getLineScope();
                if (ls.getFirstLineNumber()<0)
                	return;
                
				Rectangle originalClipping = gc.getClipping();
                gc.setClipping(rectangle.intersection(originalClipping));

                IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
                setupGCFromConfig(gc, cellStyle);

				Color oldForeground = gc.getForeground();
				Color oldBackgrColor = gc.getBackground();

                // setup the color depending on the file availability
				Color color = ColorManager.getTextFg(oldBackgrColor);
				if (Util.isFileReadable(ls)) {
					if (Display.isSystemDarkTheme())
						color = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
					else
						color = gc.getDevice().getSystemColor(SWT.COLOR_DARK_CYAN);
				}
				
				gc.setForeground(color);

                int fontHeight = gc.getFontMetrics().getHeight();
                String text = convertDataType(cell, configRegistry);

                // Draw Text
                text = getTextToDisplay(cell, gc, rectangle.width, text);

                int numberOfNewLines = getNumberOfNewLines(text);

                // if the content height is bigger than the available row height
                // we're extending the row height (only if word wrapping is enabled)
                int contentHeight = (fontHeight * numberOfNewLines) + (this.lineSpacing * (numberOfNewLines - 1)) + (this.spacing * 2);
                int contentToCellDiff = (cell.getBounds().height - rectangle.height);

                if (performRowResize(contentHeight, rectangle)) {
                    ILayer layer = cell.getLayer();
                    int rowPosition = cell.getRowPosition();
                    if (cell.isSpannedCell()) {
                        // if spanned only resize bottom row and reduce height by
                        // upper row heights to resize to only the necessary height
                        rowPosition = cell.getOriginRowPosition() + cell.getRowSpan() - 1;
                        for (int i = cell.getOriginRowPosition(); i < rowPosition; i++) {
                            contentHeight -= layer.getRowHeightByPosition(i);
                        }
                    }
                    layer.doCommand(
                            new RowResizeCommand(layer, rowPosition, contentHeight + contentToCellDiff, true));
                }

                if (numberOfNewLines == 1) {
                    int contentWidth = Math.min(getLengthFromCache(gc, text), rectangle.width);

                    gc.drawText(
                            text,
                            rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth) + this.spacing,
                            rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight) + this.spacing,
                            SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB);

                    // start x of line = start x of text
                    int x = rectangle.x
                            + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth)
                            + this.spacing;
                    // y = start y of text
                    int y = rectangle.y
                            + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight)
                            + this.spacing;
                    int length = gc.textExtent(text).x;
                    paintDecoration(cellStyle, gc, x, y, length, fontHeight);
                }
				gc.setForeground(oldForeground);

	            gc.setClipping(originalClipping);
	            resetGC(gc);
    		}
        }
	}
	
	@Override
	protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
		int rowIndex = cell.getRowIndex();
		Scope scope  = dataProvider.getRowObject(rowIndex);
		if (scope instanceof CallSiteScope) {
			CallSiteScope cs = (CallSiteScope) scope;
            int lineNumber = 1 + cs.getLineScope().getFirstLineNumber();
            if (lineNumber > 0)
            	return lineNumber + SUFFIX_LINE;
		}
		return EMPTY_STRING;
	}
}
