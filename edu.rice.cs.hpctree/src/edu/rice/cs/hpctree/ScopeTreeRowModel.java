package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;


/***********************************************************
 * 
 * Specific tree model for tree Scope.
 * The class basically inherits attributes from {@link TreeRowModel}
 * but it stores the list of items that have been expanded.
 * The reason is 
 *
 ***********************************************************/
public class ScopeTreeRowModel extends TreeRowModel<Scope> implements ISortModel, ILayerListener
{
	private IntHashSet expandSet;
	private final IScopeTreeAction treeAction;

	public ScopeTreeRowModel(ITreeData<Scope> treeData, IScopeTreeAction treeAction) {
		super(treeData);
		this.treeAction = treeAction;
		this.expandSet  = new IntHashSet();
	}
	
	@Override
    public boolean isCollapsed(int index) {
		
		return (! expandSet.contains(index));
	}
	
	@Override
    public List<Integer> collapse(int index) {
		ITreeData<Scope> treeData = getTreeData();
		ScopeTreeData tdata = (ScopeTreeData) treeData;
		tdata.collapse(index);
		expandSet.remove(index);
		
		return super.collapse(index);
	}

	@Override
    public List<Integer> collapseAll() {
		expandSet.clear();
		
		return super.collapseAll();
	}
	
	
	@Override
    public List<Integer> expand(int index) {
		ITreeData<Scope> treeData = getTreeData();
		ScopeTreeData tdata = (ScopeTreeData) treeData;
		tdata.expand(index);
		expandSet.add(index);
		
		List<Integer> list = super.expand(index);
		if (index == 0) {
			// refresh the table. Otherwise there is no change
			treeAction.refresh();
		}
		return list;
	}
	
	@Override
    public List<Integer> expandAll() {
		System.err.println("NOT SUPPORTED");
		return super.expandAll();
	}

	@Override
	public List<Integer> getSortedColumnIndexes() {
		final List<Integer> list = new ArrayList<>(1);
		ScopeTreeData treeData = (ScopeTreeData) getTreeData();
		list.add(treeData.getSortedColumn());
		return list;
	}

	@Override
	public boolean isColumnIndexSorted(int columnIndex) {
		ScopeTreeData treeData = (ScopeTreeData) getTreeData();
		return columnIndex == treeData.getSortedColumn();
	}

	@Override
	public SortDirectionEnum getSortDirection(int columnIndex) {
		ScopeTreeData treeData = (ScopeTreeData) getTreeData();
		return treeData.getSortDirection();
	}

	@Override
	public int getSortOrder(int columnIndex) {
		return 0;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		return null;
	}

	@Override
	public Comparator<?> getColumnComparator(int columnIndex) {
		return null;
	}

	@Override
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		ScopeTreeData treedata = (ScopeTreeData) getTreeData();
		treedata.sort(columnIndex, sortDirection, accumulate);
	}

	@Override
	public void clear() {
		ScopeTreeData treedata = (ScopeTreeData) getTreeData();
		treedata.clear();
	}

	
	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof StructuralRefreshEvent
                && ((StructuralRefreshEvent) event).isHorizontalStructureChanged()) {
			
		}
	}
}
