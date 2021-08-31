package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;


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
	private final IScopeTreeAction treeAction;

	public ScopeTreeRowModel(IScopeTreeData treeData, IScopeTreeAction treeAction) {
		super(treeData);
		this.treeAction = treeAction;
	}
	
	@Override
    public boolean isCollapsed(int index) {
		IScopeTreeData tdata = (IScopeTreeData) getTreeData();
		Scope scope = tdata.getDataAtIndex(index);
		if (!scope.hasChildren()) {
			return false;
		}
		Scope child = scope.getSubscope(0);
		int childIndex = tdata.indexOf(child);

		return (childIndex < 0);
	}
	
	@Override
    public List<Integer> collapse(int index) {
		// remove the collapsed children of the scope before 
		// calculating the child indexes
		IScopeTreeData tdata = (IScopeTreeData) getTreeData();

		// calculate the children indexes, including all the 
		// expanded descendants
		List<Integer> listIndexes = super.collapse(index);
		tdata.collapse(index, listIndexes);
		
		return listIndexes;
	}

	@Override
    public List<Integer> collapseAll() {
		return super.collapseAll();
	}
	
	
	@Override
    public List<Integer> expand(int index) {
		// first, create the children 
		IScopeTreeData tdata = (IScopeTreeData) getTreeData();
		tdata.expand(index);
		
		// calculate the children indexes, including all the 
		// indirect descendants
		List<Integer> list = super.expand(index);
		
		if (index == 0) {
			// TODO: hack by refresh the table. Otherwise there is no change
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
		IScopeTreeData treeData = (IScopeTreeData) getTreeData();
		list.add(treeData.getSortedColumn());
		return list;
	}

	@Override
	public boolean isColumnIndexSorted(int columnIndex) {
		IScopeTreeData treeData = (IScopeTreeData) getTreeData();
		return columnIndex == treeData.getSortedColumn();
	}

	@Override
	public SortDirectionEnum getSortDirection(int columnIndex) {
		IScopeTreeData treeData = (IScopeTreeData) getTreeData();
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
		IScopeTreeData treedata = (IScopeTreeData) getTreeData();
		treedata.sort(columnIndex, sortDirection, accumulate);
	}

	@Override
	public void clear() {
		IScopeTreeData treedata = (IScopeTreeData) getTreeData();
		treedata.clear();
	}

	
	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof StructuralRefreshEvent
                && ((StructuralRefreshEvent) event).isHorizontalStructureChanged()) {
			System.out.println("table layer event: " + event.toString());
		}
	}

	public void setRoot(Scope root) {
		IScopeTreeData treedata = (IScopeTreeData) getTreeData();
		treedata.setRoot(root);
	}

	public Scope getRoot() {
		IScopeTreeData treedata = (IScopeTreeData) getTreeData();
		return treedata.getRoot();
	}
}
