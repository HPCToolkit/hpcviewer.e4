package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortIconPainter;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

public class ScopeTreePainter 
{
    public static ICellPainter getTreeStructurePainter() {

        TreeImagePainter treeImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_144_144"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_144_144"), //$NON-NLS-1$
                        null);
        BackgroundPainter treeStructurePainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        treeImagePainter,
                                        false,
                                        2,
                                        true),
                                0, 5, 0, 5, false));
	
        return treeStructurePainter;
    }

    
    public static ICellPainter getTreeStructureSelectionPainter() {

        TreeImagePainter treeSelectionImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_inv_144_144"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_inv_144_144"), //$NON-NLS-1$
                        null);
        BackgroundPainter treeStructureSelectionPainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        treeSelectionImagePainter,
                                        false,
                                        2,
                                        true),
                                0, 5, 0, 5, false));
        
        return treeStructureSelectionPainter;
    }
    
    
    public static ICellPainter getSelectedSortHeaderCellPainter() {
    	BackgroundPainter selectedSortHeaderCellPainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new SortableHeaderTextPainter(
                                        new TextPainter(false, false),
                                        CellEdgeEnum.RIGHT,
                                        new SortIconPainter(false),
                                        false,
                                        0,
                                        false),
                                0, 2, 0, 5, false));
    	
    	return selectedSortHeaderCellPainter;
    }
}
