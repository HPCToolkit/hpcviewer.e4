package edu.rice.cs.hpctree;

import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

public class ScopeTreeData implements ITreeData<Scope> 
{
	private MutableList<Scope> list;
	private int sortedColumn;
	private SortDirectionEnum sortDirection;

	/***
	 * Constructor to create a tree data based on the root
	 * @param root the root scope
	 */
	public ScopeTreeData(RootScope root) {
		this.list = FastList.newList();
		this.list.add(root);
		
		clear();
	}
	
	
	public void clear() {
		this.sortDirection = SortDirectionEnum.DESC;
		this.sortedColumn  = 1;
	}
	
	
	public MutableList<Scope> getList() {
		return list;
	}

	
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		sortedColumn = columnIndex;
		this.sortDirection = sortDirection;
		
		// We only allow 2 types of sort: ascending and descending
		// other than that (case for none), we have to convert it		
		if (sortDirection.equals(SortDirectionEnum.NONE)) {
			this.sortDirection = this.sortDirection.equals(SortDirectionEnum.ASC)?
								 SortDirectionEnum.DESC : SortDirectionEnum.ASC;
		}
		
		ColumnComparator comparator = getComparator(columnIndex, this.sortDirection);
		list.sort(comparator);
	}
	
	
	public ColumnComparator getComparator(int columnIndex, SortDirectionEnum sortDir) {
		ColumnComparator comparator = new ColumnComparator(this, columnIndex, sortDir);
		return comparator;
	}
	
	
	public int getSortedColumn() {
		return sortedColumn;
	}

	public SortDirectionEnum getSortDirection() {
		return sortDirection;
	}
	
	
	/****
	 * Expand a tree node.
	 * This method has to be called BEFORE calling tree data's expand
	 * @param index
	 */
	public void expand(int index) {
		Scope scope = list.get(index);
		if (!scope.hasChildren())
			return;
		
		List<? extends TreeNode> children = scope.getListChildren();
		List<Scope> listScopes = convert(children);
		
		ColumnComparator comparator = getComparator(sortedColumn, sortDirection);
		listScopes.sort(comparator);
		
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
	
	public static Scope getAncestor(Scope scope, int currentDepth, int targetDepth) {
		if (scope == null)
			return null;
		
		int depth=currentDepth-1;
		Scope current = scope.getParentScope();
		
		for (;depth>targetDepth && current != null; depth--) {
			current = current.getParentScope();
		}
		return current;
		
	}

	private static class ColumnComparator implements Comparator<Scope> 
	{
		private final int index;
		private final SortDirectionEnum dir;
		private final ScopeTreeData treeData;
		
		public ColumnComparator(ScopeTreeData treeData, int index, SortDirectionEnum dir) {
			this.treeData = treeData;
			this.index = index;
			this.dir = dir;
		}
		
		@Override
		public int compare(Scope o1, Scope o2) {
            int result = 0;

			if (o1.getParent() != null && o2.getParent() != null) {
				int d1 = this.treeData.getDepthOfData(o1);
				int d2 = this.treeData.getDepthOfData(o2);
				
				if (d1 > d2) {
					Scope ancestor1 = ScopeTreeData.getAncestor(o1, d1, d2);
					result = compare(ancestor1, o2, index, dir);
					if (result == 0) {
						return 1;
					}
				} else if (d1 < d2) {
					Scope ancestor2 = ScopeTreeData.getAncestor(o2, d2, d1);
					result = compare(o1, ancestor2, index, dir);
					if (result == 0) {
						return -1;
					}
					
				} else {
					result = compare(o1, o2, index, dir);
				}
			}
			return result;
		}
		
		private int compare(Scope o1, Scope o2, int index, SortDirectionEnum dir) {
			int factor = dir == SortDirectionEnum.ASC ? -1 : 1;

			if (index == 0) {
				return factor * o1.getName().compareTo(o2.getName());
			}
			MetricValue mv1 = o1.getMetricValue(index-1);
			MetricValue mv2 = o2.getMetricValue(index-1);

			if (mv1.getValue() > mv2.getValue())
				return factor * 1;
			if (mv1.getValue() < mv2.getValue())
				return factor * -1;
			return 0;
		}
	}

}
