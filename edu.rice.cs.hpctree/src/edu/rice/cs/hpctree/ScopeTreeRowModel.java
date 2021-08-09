package edu.rice.cs.hpctree;

import java.util.List;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
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
public class ScopeTreeRowModel extends TreeRowModel<Scope> 
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
	
	
}
