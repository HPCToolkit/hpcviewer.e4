package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

public class ScopeTreeData implements ITreeData<Scope> 
{
	private final RootScope root;
	private List<Scope> list;
	
	public ScopeTreeData(RootScope root) {
		this.root = root;
		this.list = new ArrayList<>();
		this.list.add(root);
	}
	
	
	public void expand(int index) {
		Scope scope = list.get(index);
		List<? extends TreeNode> children = scope.getListChildren();
		List<Scope> listScopes = convert(children);
		int n1 = list.size();
		list.addAll(index+1, listScopes);
		int n2 = list.size();
		System.out.println("expand " + index + " size " + n1 + " -> " + n2 + ", fk: " + listScopes.get(0).getName());
	}
	
	
	public void collapse(int index) {
		Scope scope = list.get(index);
		List<? extends TreeNode> children = scope.getListChildren();
		int n1 = list.size();
		list.subList(index+1, index+1+children.size()).clear();
		int n2 = list.size();
		System.out.println("collapse " + index + " size " + n1 + " -> " + n2);
	}
	
	
	private List<Scope> convert(List<? extends TreeNode> list) {
		return (List<Scope>) list;
	}
	
	
	private boolean isRootScope(Scope scope) {
		return (scope == root) || (scope instanceof RootScope);
	}
	
	@Override
	public int getDepthOfData(Scope object) {
		if (object == null) return 0;
		
		int depth = 0;
		Scope scope = object;
		while (scope.getParent() != null && !isRootScope(scope)) {
			depth++;
			scope = scope.getParentScope();
		}
		return depth;
	}

	@Override
	public int getDepthOfData(int index) {
		return getDepthOfData(getDataAtIndex(index));
	}

	@Override
	public Scope getDataAtIndex(int index) {
		Scope scope = list.get(index);
		if (scope == null) {
			throw new RuntimeException("index does not exist: " + index);
		}
		return scope;
	}

	@Override
	public int indexOf(Scope child) {
		return list.indexOf(child);
	}

	@Override
	public boolean hasChildren(Scope object) {
		return object.hasChildren();
	}

	@Override
	public boolean hasChildren(int index) {
		return hasChildren(getDataAtIndex(index));
	}

	@Override
	public List<Scope> getChildren(Scope object) {
		List<? extends TreeNode> list = object.getListChildren();
		List<Scope> listScopes = (List<Scope>) list;
		return listScopes;
	}

	@Override
	public List<Scope> getChildren(Scope object, boolean fullDepth) {
		return getChildren(object);
	}

	@Override
	public List<Scope> getChildren(int index) {
		Scope scope = getDataAtIndex(index);
		return getChildren(scope);
	}

	@Override
	public int getElementCount() {
		return list.size();
	}

	@Override
	public boolean isValidIndex(int index) {
		return (index >= 0) && (index < list.size());
	}

}
