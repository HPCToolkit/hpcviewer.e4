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
import edu.rice.cs.hpctree.internal.IScopeTreeAction;


public class ScopeTreeBodyLayerStack extends AbstractLayerTransform 
{
    private final SelectionLayer selectionLayer;
    private final FreezeLayer    freezeLayer ;
    private final ViewportLayer  viewportLayer;
    private final DataLayer bodyDataLayer;

    private final CompositeFreezeLayer compositeFreezeLayer ;
    private final ScopeTreeRowModel    treeRowModel ;

	public ScopeTreeBodyLayerStack(IScopeTreeData treeData, 
								   IDataProvider  bodyDataProvider,
    							   IScopeTreeAction treeAction) {

        this.bodyDataLayer    = new DataLayer(bodyDataProvider);

        bodyDataLayer.setColumnsResizableByDefault(true);
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
    

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}


	public FreezeLayer getFreezeLayer() {
		return freezeLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

	public ScopeTreeRowModel getTreeRowModel() {
		return treeRowModel;
	}
}
