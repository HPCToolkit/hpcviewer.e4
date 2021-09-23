package edu.rice.cs.hpctree;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultRowSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.internal.RemoveHeaderSelectionConfiguration;



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
    private final DataLayer      bodyDataLayer;
    private final ScopeTreeLayer treeLayer ;
    private final ColumnHideShowLayer hideShowLayer;

    private final CompositeFreezeLayer compositeFreezeLayer ;
    private final ScopeTreeRowModel    treeRowModel ;

	public ScopeTreeBodyLayerStack(IScopeTreeData treeData, 
								   ScopeTreeDataProvider  bodyDataProvider) {

		// initialize the stack of layers for the tree table
		// careful: the order is important. 
		// we have to ensure the data layer is the bottom and the column reorder
		// has to be before the column hide show layer. Otherwise we can't hide/
		// show columns properly
		
        this.bodyDataLayer = new DataLayer(bodyDataProvider);
        this.bodyDataLayer.setColumnsResizableByDefault(true);

        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
        this.hideShowLayer  = new ColumnHideShowLayer(reorderLayer);
        this.selectionLayer = new SelectionLayer(this.hideShowLayer, false);
        
        this.treeRowModel   = new ScopeTreeRowModel(treeData);
        this.treeLayer      = new ScopeTreeLayer(this.selectionLayer, this.treeRowModel);
        this.viewportLayer  = new ViewportLayer(this.treeLayer);
      
        this.freezeLayer    = new FreezeLayer(this.treeLayer);
        this.compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);
        
        final IRowIdAccessor<Scope> rowIdAccessor = new IRowIdAccessor<Scope>() {

			@Override
			public Serializable getRowId(Scope rowObject) {
				return rowObject.hashCode();
			}
		};
		
        RowSelectionModel<Scope> selectionModel = new RowSelectionModel<>(this.selectionLayer, 
        																  bodyDataProvider, 
        																  rowIdAccessor );
        this.selectionLayer.setSelectionModel(selectionModel);
        this.selectionLayer.addConfiguration(new DefaultRowSelectionLayerConfiguration());
        this.selectionLayer.addConfiguration(new RemoveHeaderSelectionConfiguration());
        
        setUnderlyingLayer(compositeFreezeLayer);
    }
    
	public void expand(int parentIndex) {
    	treeLayer.expandTreeRow(parentIndex);
	}
	

    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}

    public ColumnHideShowLayer getColumnHideShowLayer() {
    	return hideShowLayer;
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
