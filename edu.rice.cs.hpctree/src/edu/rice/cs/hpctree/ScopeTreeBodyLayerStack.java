package edu.rice.cs.hpctree;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;

public class ScopeTreeBodyLayerStack extends AbstractLayerTransform 
{
    private final IDataProvider  bodyDataProvider;
    private final SelectionLayer selectionLayer;
    private final TreeLayer      treeLayer;
    private final FreezeLayer    freezeLayer ;
    private final CompositeFreezeLayer compositeFreezeLayer ;
    private final ScopeTreeRowModel    treeRowModel ;

    public ScopeTreeBodyLayerStack(RootScope root,
    					  		   ITreeData<Scope> treeData,
    							   Experiment experiment,
    							   IScopeTreeAction treeAction) {

        this.bodyDataProvider   = new ScopeTreeDataProvider(treeData, experiment); 
        DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        treeRowModel = new ScopeTreeRowModel(treeData, treeAction);

        this.selectionLayer = new SelectionLayer(bodyDataLayer);
        this.treeLayer      = new TreeLayer(this.selectionLayer, treeRowModel);
        ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);
        this.freezeLayer     = new FreezeLayer(treeLayer);
        compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);
        
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

	public ScopeTreeRowModel getTreeRowModel() {
		return treeRowModel;
	}

	public IDataProvider getBodyDataProvider() {
        return this.bodyDataProvider;
    }
}
