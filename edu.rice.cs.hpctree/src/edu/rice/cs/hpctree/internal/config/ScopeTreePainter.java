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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;

public class ScopeTreePainter 
{
	// light mode: when the nodes are not selected (normal mode)
	// dark mode:   when the nodes are selected (select mode)
	private static final String []IMG_COLLAPSED = {"right_120_120", "right_144_144"};
	private static final String []IMG_EXPANDED  = {"right_down_120_120", "right_down_144_144" };
	
	// light mode: when the nodes are selected (select mode)
	// dark mode: when the node are not selected (normal mode)
	private static final String []IMG_INV_COLLAPSED = {"right_inv_120_120", "right_inv_144_144"};
	private static final String []IMG_INV_EXPANDED  = {"right_down_inv_120_120", "right_down_inv_144_144" };
	
	public static int getZoomFactor() {
		int zoom = DPIUtil.getDeviceZoom();
		Point p = Display.getDefault().getDPI();
		return (int)zoom / 100;
	}
	
    public static ICellPainter getTreeStructurePainter() {
    	int zoom = (getZoomFactor()-1) % 2;
    	
        TreeImagePainter treeImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage(IMG_COLLAPSED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_EXPANDED[zoom]),  //$NON-NLS-1$
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
    	int zoom = getZoomFactor()-1;

        TreeImagePainter treeSelectionImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage(IMG_INV_COLLAPSED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_INV_EXPANDED[zoom]), //$NON-NLS-1$
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
