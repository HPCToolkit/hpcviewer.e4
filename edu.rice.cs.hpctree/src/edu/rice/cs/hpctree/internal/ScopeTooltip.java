package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.string.StringUtil;



/************************************************************
 * 
 * Class to display tooltips only for column header and the tree column
 *
 ************************************************************/
public class ScopeTooltip extends NatTableContentTooltip 
{
	private final static int MAX_TOOLTIP_CHAR = 80;
	private final ScopeTreeDataProvider bodyDataProvider;

	public ScopeTooltip(NatTable natTable, ScopeTreeDataProvider bodyDataProvider) {
		super(natTable, GridRegion.BODY, GridRegion.COLUMN_HEADER);
		this.bodyDataProvider = bodyDataProvider;
	}
	
	@Override
    protected String getText(Event event) {

        int col = this.natTable.getColumnPositionByX(event.x);
        int row = this.natTable.getRowPositionByY(event.y);
        int colIndex = this.natTable.getColumnIndexByPosition(col);
        int rowIndex = this.natTable.getRowIndexByPosition(row);
        
        // We only show the tooltip for column header and the tree column (col index = 0)
    	if (rowIndex == 0) {
    		// header of the table
    		if (colIndex > 0) {
        		BaseMetric metric = bodyDataProvider.getMetric(colIndex);
        		String name = metric.getDisplayName();
        		String desc = StringUtil.wrapScopeName(metric.getDescription(), MAX_TOOLTIP_CHAR);
        		if (desc == null)
        			return name;
        		return name + "\n" + desc;
    		}
    		return null;
    	}

        ILayerCell cell = this.natTable.getCellByPosition(col, row);
        if (cell == null)
        	return null;
    	IConfigRegistry configRegistry = this.natTable.getConfigRegistry();
        ICellPainter painter = cell.getLayer().getCellPainter(col, row, cell, configRegistry); 

        GC gc = new GC(natTable);
        try {
        	Rectangle adjustedBounds = natTable.getLayerPainter().adjustCellBounds(col, row, cell.getBounds());
        	ICellPainter clickedCell = painter.getCellPainterAt(event.x, event.y, cell, gc, adjustedBounds, configRegistry);
        	if (clickedCell != null ) {

            	if (clickedCell instanceof CallSiteTextPainter || 
            		clickedCell instanceof CallSiteArrowPainter) {
                		Scope scope = bodyDataProvider.getRowObject(rowIndex);

                		if (scope instanceof CallSiteScope) {
                			LineScope ls = ((CallSiteScope)scope).getLineScope();
                			String filename = ls.getName();
                			return "Callsite at " + filename;
                		}
                	}
        	}
        } finally {
        	gc.dispose();
        }
    	if (colIndex == 0) {
    		String text = super.getText(event);
    		if (text != null && text.length() > 0) {
    			text = StringUtil.wrapScopeName(text, MAX_TOOLTIP_CHAR);
    		}
    		return text;
    	}
    	return null;
	}
}
