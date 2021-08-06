package edu.rice.cs.hpctree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class ScopeTreeRowModel extends TreeRowModel<Scope> 
{
	private final Set<Integer> expandedScopes = new HashSet<>();
	
	public ScopeTreeRowModel(ITreeData<Scope> treeData) {
		super(treeData);
	}

	@Override
    public List<Integer> expand(int index) {
		ITreeData<Scope> treeData = getTreeData();
		TreeData tdata = (TreeData) treeData;
		tdata.expand(index);
		return super.expand(index);
	}
}
