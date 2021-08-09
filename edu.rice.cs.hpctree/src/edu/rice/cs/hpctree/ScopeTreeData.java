package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.MutableListFactoryImpl;

import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

public class ScopeTreeData implements ITreeData<Scope> 
{
	//private ArrayList<Scope> list;
	private MutableList<Scope> list;
	
	/***
	 * Constructor to create a tree data based on the root
	 * @param root the root scope
	 */
	public ScopeTreeData(RootScope root) {
		this.list = MutableListFactoryImpl.INSTANCE.empty();
		this.list.add(root);
	}
	
	
	/****
	 * Expand a tree node.
	 * This method has to be called BEFORE calling tree data's expand
	 * @param index
	 */
	public void expand(int index) {
		Scope scope = list.get(index);
		List<? extends TreeNode> children = scope.getListChildren();
		List<Scope> listScopes = convert(children);
		list.addAll(index+1, listScopes);
	}
	
	
	/****
	 * Collapse a tree node. 
	 * This methid has to be called BEFORE calling the tree data
	 * @param index element index
	 */
	public void collapse(int index) {
		Scope scope = list.get(index);
		List<? extends TreeNode> children = scope.getListChildren();
		list.subList(index+1, index+1+children.size()).clear();
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Scope> convert(List<? extends TreeNode> list) {
		return (List<Scope>) list;
	}
	
	
	private boolean isRootScope(Scope scope) {
		return (scope == null) || (scope instanceof RootScope);
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
		return convert(list);
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
