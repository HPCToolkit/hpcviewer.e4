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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.DPIUtil;

import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctree.internal.CallSiteArrowPainter;
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

    
    /***
     * Retrieve the tree cell painter based on the background color.
     * If the background color is dark, it will pick the light painter automatically.
     * 
     * @param dataProvider
     * @param defaultBgColor
     * @return
     */
    public static ICellPainter getTreeStructurePainter(ScopeTreeDataProvider dataProvider, Color defaultBgColor) {
    	Color fg = ColorManager.getTextFg(defaultBgColor);
    	if (fg == ColorManager.COLOR_WHITE)
    		return ScopeTreePainter.getInvTreeStructurePainter(dataProvider);

    	return ScopeTreePainter.getTreeStructurePainter(dataProvider);
    }
	
    /****
     * Retrieve the tree cell painter for a given data provider
     * @param dataProvider
     * @return
     */
    public static ICellPainter getTreeStructurePainter(ScopeTreeDataProvider dataProvider) {
    	int zoom = (getZoomFactor() <= 1) ? 0 : 1;
    	
        TreeImagePainter treeImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage(IMG_COLLAPSED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_EXPANDED[zoom]),  //$NON-NLS-1$
                        GUIHelper.getImage(IMG_LEAF));
        
        return getCallSitePainter(dataProvider, treeImagePainter);
    }

    
    /****
     * Retrieve the selected (inverted) tree painter for a given tree data provider
     * @param dataProvider
     * @return
     */
    public static ICellPainter getInvTreeStructurePainter(ScopeTreeDataProvider dataProvider) {
    	int zoom = (getZoomFactor() <= 1) ? 0 : 1;

        TreeImagePainter treeSelectionImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage(IMG_INV_COLLAPSED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_INV_EXPANDED[zoom]), //$NON-NLS-1$
                        GUIHelper.getImage(IMG_LEAF));
        
        return getCallSitePainter(dataProvider, treeSelectionImagePainter);
    }
    
    
    /****
     * Retrieve the zoom factor (1, 2, ...)
     * @return
     */
	private static int getZoomFactor() {
		@SuppressWarnings("restriction")
		int zoom = DPIUtil.getDeviceZoom();
		return (int)zoom / 100;
	}
	

	/****
	 * Retrieve the cell painter of the table row
	 * @param dataProvider
	 * @param treeImagePainter
	 * @return
	 */
    private static ICellPainter getCallSitePainter(ScopeTreeDataProvider dataProvider, TreeImagePainter treeImagePainter) {    	
        // call site image and text
        // Partial fix issue #134: put the text on the left of the icon
        CallSiteArrowPainter csArrowPainter = new CallSiteArrowPainter(dataProvider);
        
        // combining tree and call site info
        CellPainterDecorator decorator = new CellPainterDecorator(treeImagePainter, CellEdgeEnum.RIGHT, csArrowPainter);

        BackgroundPainter treePainter =
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
                                0, 0, 0, 0, false));
        
        return treePainter;
    }
}
