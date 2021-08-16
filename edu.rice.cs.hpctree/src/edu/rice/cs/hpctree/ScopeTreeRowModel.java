package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	private final LinkedHashMap<Integer, SortDirectionEnum> sortedColumns;

	public ScopeTreeRowModel(ITreeData<Scope> treeData, IScopeTreeAction treeAction) {
		super(treeData);
		this.treeAction = treeAction;
		this.expandSet  = new IntHashSet();

		sortedColumns = new LinkedHashMap<>();
	    sortedColumns.put(1, SortDirectionEnum.DESC);
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
		final List<Integer> list = new ArrayList<>(sortedColumns.size());
		if (sortedColumns.size()>0) {
			sortedColumns.forEach( (col, dir) -> list.add(col));
		}
		return list;
	}

	@Override
	public boolean isColumnIndexSorted(int columnIndex) {
		return sortedColumns.containsKey(columnIndex);
	}

	@Override
	public SortDirectionEnum getSortDirection(int columnIndex) {
		SortDirectionEnum dir = sortedColumns.get(columnIndex);
		if (dir == null)
			dir = SortDirectionEnum.DESC;
		return dir;
	}

	@Override
	public int getSortOrder(int columnIndex) {
		if (sortedColumns.size() == 0)
			return 0;
		
		Iterator<Integer> iterator = sortedColumns.keySet().iterator();
		for (int i=0; i<sortedColumns.size(); i++) {
			int key = iterator.next();
			if (key == columnIndex)
				return i;
		}
		return 0;
	}

	@Override
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		Comparator<Scope> comparator = (Comparator<Scope>) getColumnComparator(columnIndex);
		return List.of(comparator);
	}

	@Override
	public Comparator<?> getColumnComparator(int columnIndex) {
		SortDirectionEnum dir = sortedColumns.get(columnIndex);
		ScopeTreeData treeData = (ScopeTreeData) getTreeData();
		return treeData.getComparator(columnIndex, dir);
	}

	@Override
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		sortedColumns.put(columnIndex, sortDirection);
		ScopeTreeData treedata = (ScopeTreeData) getTreeData();
		treedata.sort(columnIndex, sortDirection, accumulate);
	}

	@Override
	public void clear() {
		sortedColumns.clear();
	}

	
	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof StructuralRefreshEvent
                && ((StructuralRefreshEvent) event).isHorizontalStructureChanged()) {
			
		}
	}

	
}
