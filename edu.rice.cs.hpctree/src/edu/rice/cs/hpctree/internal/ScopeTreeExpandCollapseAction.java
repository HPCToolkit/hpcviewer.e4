package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;

public class ScopeTreeExpandCollapseAction implements IMouseAction {

	@Override
	public void run(NatTable natTable, MouseEvent event) {
        int c = natTable.getColumnPositionByX(event.x);
        int r = natTable.getRowPositionByY(event.y);
        ILayerCell cell = natTable.getCellByPosition(c, r);
        if (cell != null) {
            int rowIndex = cell.getLayer().getRowIndexByPosition(cell.getOriginRowPosition());
            int columnIndex = cell.getLayer().getColumnIndexByPosition(cell.getOriginColumnPosition());
            TreeExpandCollapseCommand command = new TreeExpandCollapseCommand(rowIndex, columnIndex);
            natTable.doCommand(command);
        }
	}

}
