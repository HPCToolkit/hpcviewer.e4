package edu.rice.cs.hpctree.internal;

import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class ScopeAttributeMouseEventMatcher extends MouseEventMatcher 
{
	private final FastList<String> labels;
	private final List<ICellPainter> painters;
	
    public ScopeAttributeMouseEventMatcher(String body, int leftButton, List<ICellPainter> painters) {
    	super(body, leftButton);
    	
    	this.painters = painters;
    	
    	labels = new FastList<>();
    	labels.add(ScopeTreeLabelAccumulator.LABEL_CALLSITE);
    	labels.add(ScopeTreeLabelAccumulator.LABEL_CALLER);
	}

	@Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        boolean result = false;
    	if (super.matches(natTable, event, regionLabels)) {

            int columnPosition = natTable.getColumnPositionByX(event.x);
            int rowPosition = natTable.getRowPositionByY(event.y);

            LabelStack customLabels = natTable.getConfigLabelsByPosition(columnPosition, rowPosition);
            if (!labels.stream().anyMatch(customLabels::contains))
            	return false;

            
            ILayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);
            if (cell != null) {
                IConfigRegistry configRegistry = natTable.getConfigRegistry();
                ICellPainter cellPainter = cell.getLayer().getCellPainter(
                        columnPosition,
                        rowPosition,
                        cell,
                        configRegistry);

                GC gc = new GC(natTable.getDisplay());
                try {
                    Rectangle adjustedCellBounds = natTable.getLayerPainter().adjustCellBounds(
                            columnPosition,
                            rowPosition,
                            cell.getBounds());

                    ICellPainter clickedCellPainter = cellPainter.getCellPainterAt(
                            event.x,
                            event.y,
                            cell,
                            gc,
                            adjustedCellBounds,
                            configRegistry);
                    
                    if (clickedCellPainter != null) {
                    	if (painters.contains(clickedCellPainter)) {
                        	System.out.println("clickedCellPainter: " + clickedCellPainter.getClass());
                        	result = true;
                    	}
                    }
                } finally {
                    gc.dispose();
                }
            }
    	}
    	
        return result;
    }
}
