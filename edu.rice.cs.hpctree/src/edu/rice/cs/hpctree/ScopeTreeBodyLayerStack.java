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
    private final IDataProvider  bodyDataProvider;
    private final SelectionLayer selectionLayer;
    private final TreeLayer      treeLayer;
    private final FreezeLayer    freezeLayer ;
    private final ViewportLayer  viewportLayer;

    private final CompositeFreezeLayer compositeFreezeLayer ;
    private final ScopeTreeRowModel    treeRowModel ;

    public ScopeTreeBodyLayerStack(IScopeTreeData treeData,
    							   IScopeTreeAction treeAction) {

        this.bodyDataProvider   = new ScopeTreeDataProvider(treeData, treeData.getMetricManager()); 
        DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

        bodyDataLayer.setColumnsResizableByDefault(true);
        bodyDataLayer.setColumnWidthByPosition(0, 350);
        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        treeRowModel = new ScopeTreeRowModel(treeData, treeAction);

        this.selectionLayer = new SelectionLayer(bodyDataLayer);
        this.treeLayer      = new TreeLayer(this.selectionLayer, treeRowModel);
        this.viewportLayer  = new ViewportLayer(this.treeLayer);
        this.freezeLayer     = new FreezeLayer(treeLayer);
        compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);
        
        if (treeData.getRoot().hasChildren())
        	treeLayer.expandTreeRow(0);
        
        setUnderlyingLayer(compositeFreezeLayer);
    }

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public TreeLayer getTreeLayer() {
        return this.treeLayer;
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
}
