package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortIconPainter;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.internal.DPIUtil;

import edu.rice.cs.hpctree.internal.CallSiteImagePainter;
import edu.rice.cs.hpctree.internal.CallSiteTextPainter;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;

public class ScopeTreePainter 
{
	// Issue #137: need a spacer for the lead nodes
	private static final String IMG_LEAF = "leaf";
	
	// light mode: when the nodes are not selected (normal mode)
	// dark mode:   when the nodes are selected (select mode)
	private static final String []IMG_COLLAPSED = {"right_120_120", "right_144_144"};
	private static final String []IMG_EXPANDED  = {"right_down_120_120", "right_down_144_144" };
	
	// light mode: when the nodes are selected (select mode)
	// dark mode: when the node are not selected (normal mode)
	private static final String []IMG_INV_COLLAPSED = {"right_inv_120_120", "right_inv_144_144"};
	private static final String []IMG_INV_EXPANDED  = {"right_down_inv_120_120", "right_down_inv_144_144" };
	
	public static int getZoomFactor() {
		@SuppressWarnings("restriction")
		int zoom = DPIUtil.getDeviceZoom();
		return (int)zoom / 100;
	}
	
    public static ICellPainter getTreeStructurePainter(ScopeTreeDataProvider dataProvider) {
    	int zoom = (getZoomFactor() <= 1) ? 0 : 1;
    	
        TreeImagePainter treeImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage(IMG_COLLAPSED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_EXPANDED[zoom]),  //$NON-NLS-1$
                        GUIHelper.getImage(IMG_LEAF));
    	
        // call site image and text
        // Partial fix issue #134: put the text on the left of the icon
        CallSiteImagePainter callsitePainter = new CallSiteImagePainter();
        CallSiteTextPainter  textPainter = new CallSiteTextPainter(dataProvider);
        CellPainterDecorator decoratorCS = new CellPainterDecorator(textPainter, CellEdgeEnum.RIGHT, callsitePainter);
        
        // combining tree and call site info
        CellPainterDecorator decorator = new CellPainterDecorator(treeImagePainter, CellEdgeEnum.RIGHT, decoratorCS);
        
        BackgroundPainter treeStructurePainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        decorator,
                                        false,
                                        2,
                                        true),
                                0, 5, 0, 5, false));
        return treeStructurePainter;
    }

    
    public static ICellPainter getInvTreeStructurePainter(ScopeTreeDataProvider dataProvider) {
    	int zoom = (getZoomFactor() <= 1) ? 0 : 1;

        TreeImagePainter treeSelectionImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage(IMG_INV_COLLAPSED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_INV_EXPANDED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_LEAF));
    	
        // call site image and text
        // Partial fix issue #134: put the text on the left of the icon
        CallSiteImagePainter callsitePainter = new CallSiteImagePainter();
        CallSiteTextPainter  textPainter = new CallSiteTextPainter(dataProvider);
        CellPainterDecorator decoratorCS = new CellPainterDecorator(textPainter, CellEdgeEnum.RIGHT, callsitePainter);
        
        // combining tree and call site info
        CellPainterDecorator decorator = new CellPainterDecorator(treeSelectionImagePainter, CellEdgeEnum.RIGHT, decoratorCS);

        BackgroundPainter treeStructureSelectionPainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        decorator,
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
