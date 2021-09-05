package edu.rice.cs.hpctree;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;



/********************************************************************
 * 
 * Main body layer class 
 *
 ********************************************************************/
public class ScopeTreeBodyLayerStack extends AbstractLayerTransform 
{
    private final SelectionLayer selectionLayer;
    private final FreezeLayer    freezeLayer ;
    private final ViewportLayer  viewportLayer;
    private final DataLayer bodyDataLayer;
    private final ScopeTreeLayer treeLayer ;

    private final CompositeFreezeLayer compositeFreezeLayer ;
    private final ScopeTreeRowModel    treeRowModel ;

	public ScopeTreeBodyLayerStack(IScopeTreeData treeData, 
								   IDataProvider  bodyDataProvider,
    							   IScopeTreeAction treeAction) {

        this.bodyDataLayer = new DataLayer(bodyDataProvider);
        this.bodyDataLayer.setColumnsResizableByDefault(true);

        this.treeRowModel   = new ScopeTreeRowModel(treeData, treeAction);
        this.selectionLayer = new SelectionLayer(new ColumnReorderLayer(bodyDataLayer));
        this.treeLayer      = new ScopeTreeLayer(this.selectionLayer, treeRowModel);
        this.viewportLayer  = new ViewportLayer(treeLayer);
      
        this.freezeLayer     = new FreezeLayer(treeLayer);
        compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);
        
        if (treeData.getRoot().hasChildren())
        	treeLayer.expandTreeRow(0);
        
        setUnderlyingLayer(compositeFreezeLayer);
    }
    
	public void expand(int parentIndex) {
    	treeLayer.expandTreeRow(parentIndex);
	}
	

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}

    public AbstractLayerTransform getTreeLayer() {
    	return treeLayer;
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
