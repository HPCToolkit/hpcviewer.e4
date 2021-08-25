package edu.rice.cs.hpctree;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;
import edu.rice.cs.hpctree.internal.MetricTableRegistryConfiguration;


public class ScopeTreeBodyLayerStack extends AbstractLayerTransform 
{
	private final static float  FACTOR_BOLD_FONT   = 1.2f;
	private final static String TEXT_METRIC_COLUMN = "|8x88+88xx888x8%--";

	private final IDataProvider  bodyDataProvider;
    private final SelectionLayer selectionLayer;
    private final FreezeLayer    freezeLayer ;
    private final ViewportLayer  viewportLayer;
    private final DataLayer bodyDataLayer;

    private final CompositeFreezeLayer compositeFreezeLayer ;
    private final ScopeTreeRowModel    treeRowModel ;

	private Point columnSize;
	
    public ScopeTreeBodyLayerStack(IScopeTreeData treeData,
    							   IScopeTreeAction treeAction) {

        this.bodyDataProvider = new ScopeTreeDataProvider(treeData, treeData.getMetricManager()); 
        this.bodyDataLayer    = new DataLayer(this.bodyDataProvider);

        bodyDataLayer.setColumnsResizableByDefault(true);
        bodyDataLayer.setColumnWidthByPosition(0, 350);
        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        this.treeRowModel = new ScopeTreeRowModel(treeData, treeAction);

        this.selectionLayer = new SelectionLayer(bodyDataLayer);
        TreeLayer treeLayer = new TreeLayer(this.selectionLayer, treeRowModel);
        this.viewportLayer  = new ViewportLayer(treeLayer);
        this.freezeLayer     = new FreezeLayer(treeLayer);
        compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);
        
        if (treeData.getRoot().hasChildren())
        	treeLayer.expandTreeRow(0);
        
        setUnderlyingLayer(compositeFreezeLayer);
    }
    
    
    public void pack(Display display) {
    	this.columnSize = getMetricColumnSize(display);
    	int numColumns  = this.bodyDataProvider.getColumnCount();
    	for(int i=1; i<numColumns; i++) {
        	bodyDataLayer.setColumnWidthByPosition(i, columnSize.x);
    	}
    }

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public CompositeFreezeLayer getFreezeLayer() {
		return compositeFreezeLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

	public ScopeTreeRowModel getTreeRowModel() {
		return treeRowModel;
	}
	
	public Point getMetricColumnSize(Display display) {
		final GC gc = new GC(display);		
		
		gc.setFont(MetricTableRegistryConfiguration.getMetricFont());
		String text = TEXT_METRIC_COLUMN;
		if (OSValidator.isWindows()) {
			
			// FIXME: ugly hack to add some spaces for Windows
			// Somehow, Windows 10 doesn't allow to squeeze the text inside the table
			// we have to give them some spaces (2 spaces in my case).
			// A temporary fix for issue #37
			text += "xx";
		}
		Point extent = gc.stringExtent(text);
		Point size   = new Point((int) (extent.x * FACTOR_BOLD_FONT), extent.y + 2);
		
		// check the height if we use generic font (tree column)
		// if this font is higher, we should use this height as the standard.
		
		gc.setFont(MetricTableRegistryConfiguration.getGenericFont());
		extent = gc.stringExtent(text);
		size.y = Math.max(size.y, extent.y);
		
		gc.dispose();
		
		return size;
	}
}
