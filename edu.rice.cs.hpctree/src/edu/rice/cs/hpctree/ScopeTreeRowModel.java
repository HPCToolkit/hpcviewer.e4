package edu.rice.cs.hpctree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;

import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;

public class ScopeTreeRowModel extends TreeRowModel<Scope> 
{
	private final Set<Integer> expandedIndexes = new HashSet<>();
	private final IScopeTreeAction treeAction;
	
	public ScopeTreeRowModel(ITreeData<Scope> treeData, IScopeTreeAction treeAction) {
		super(treeData);
		this.treeAction = treeAction;
	}
	
	@Override
    public boolean isCollapsed(int index) {
		return (! expandedIndexes.contains(index));
	}
	
	@Override
    public List<Integer> collapse(int index) {
		ITreeData<Scope> treeData = getTreeData();
		ScopeTreeData tdata = (ScopeTreeData) treeData;
		tdata.collapse(index);
		expandedIndexes.remove(index);
		
		return super.collapse(index);
	}

	@Override
    public List<Integer> collapseAll() {
		expandedIndexes.clear();
		
		return super.collapseAll();
	}
	
	
	@Override
    public List<Integer> expand(int index) {
		ITreeData<Scope> treeData = getTreeData();
		ScopeTreeData tdata = (ScopeTreeData) treeData;
		tdata.expand(index);
		expandedIndexes.add(index);
		
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
	
	
}
