// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.internal;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


/*********************************************************************
 * 
 * Generic class to match mouse event with a region painter in the cell.
 * If the cell has an attribute (like {@code ImagePainter} of interest,
 * it will return true so that the class can do an action.
 * <p>
 * This class is specifically designed to be used in {@code UiBindingRegistry}
 * and matched with any IMouseAction.
 * 
 *********************************************************************/
public class ScopeAttributeMouseEventMatcher extends MouseEventMatcher 
{
	private final List<String> labels;
	private final List<Class<?>> painters;
	
	/***
	 * Constructor to specify which painters need to be matched.
	 * 
	 * @param body the region in the table (mostly {@code GridRegion.BODY})
	 * @param leftButton the mouse button (like {@code MouseEventMatcher.LEFT_BUTTON})
	 * @param labels {@code List} of {@code String} of labels to matched against this event
	 *  
	 *  @see GridRegion
	 *  @see MouseEventMatcher 
	 */
    public ScopeAttributeMouseEventMatcher(String body, 
    									   int leftButton, 
    									   List<String> labels,
    									   List<Class<?>> listPainters) {
    	super(body, leftButton);    	
    	this.labels = labels;
    	this.painters = listPainters;
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
            if (cell == null)
            	return false;
            
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
                	result = painters.stream().anyMatch(c -> clickedCellPainter.getClass() == c);
                }
            } finally {
                gc.dispose();
            }
    	}    	
        return result;
    }
}
