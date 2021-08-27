package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

/******************************************************
 * 
 * Default ITreeData of hpcviewer tree 
 *
 ******************************************************/
public class ScopeTreeData implements IScopeTreeData
{
	private final IMetricManager metricManager;

	/** list of current data. The list is dynamic, always incremental **/
	private final MutableList<Scope> list;
	/** map of the collapsed nodes. The key is the parent index. 
	 *  The  value is the list of collapsed nodes of the parent */
	private final IntObjectHashMap<List<? extends TreeNode>> mapCollapsedScopes;

	private Scope root;
	
	// attributes to handle sorting
	private int sortedColumn;
	private SortDirectionEnum sortDirection;

	
	/***
	 * Constructor to create a tree data based on the root
	 * @param root the root scope
	 * @param metricManager the metric manager of the experiment or database
	 */
	public ScopeTreeData(RootScope root, IMetricManager metricManager) {
		this.list = FastList.newList();
		this.list.add(root);
		
		this.root = root;
		this.metricManager = metricManager;
		
		this.mapCollapsedScopes = IntObjectHashMap.newMap();
		
		clear();
	}
	
	
	@Override
	public void setRoot(Scope root) {
		list.clear();
		mapCollapsedScopes.clear();
		
		list.add(root);
		this.root = root;
	}
	
	/****
	 * Get the root of this tree data
	 * @return RootScope
	 */
	@Override
	public Scope getRoot() {
		return root;
	}
	
	/***
	 * Get the metric manager of this tree data
	 * @return
	 */
	@Override
	public IMetricManager getMetricManager() {
		return metricManager;
	}
	
	
	/** 
	 * Reset the data
	 */
	@Override
	public void clear() {
		this.sortDirection = SortDirectionEnum.DESC;
		this.sortedColumn  = 1;
	}
	
	
	/***
	 * Get the current list of data.
	 * Recall that the list is created and added dynamically depending if the tree
	 * has been expanded or not.
	 * 
	 * @return {@code MutableList<Scope>} list of scope data
	 */
	@Override
	public MutableList<Scope> getList() {
		return list;
	}

	/***
	 * Method to notify to sort the data based on certain column and direction
	 * @param columnIndex the column index. Must be greater or equal to 0
	 * @param sortDirection {@code SortDirectionEnum}
	 * @param accumulate
	 */
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		this.sortDirection = sortDirection;
		
		// We only allow 2 types of sort: ascending and descending
		// other than that (case for none), we have to convert it		
		if (sortDirection.equals(SortDirectionEnum.NONE)) {
			if (columnIndex == sortedColumn) {
				this.sortDirection = this.sortDirection.equals(SortDirectionEnum.ASC)?
						 SortDirectionEnum.DESC : SortDirectionEnum.ASC;
			} else {
				this.sortDirection = SortDirectionEnum.DESC;
			}
		}
		sortedColumn = columnIndex;

		ColumnComparator comparator = getComparator(columnIndex, this.sortDirection);
		synchronized (list) {
			list.sort(comparator);
		}
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
	 * This method has to be called BEFORE calling tree model's {@code expand}
	 * @param index
	 */
	public void expand(int index) {
		synchronized (list) {
			Scope scope = list.get(index);
			if (!scope.hasChildren())
				return;
			
			List<? extends TreeNode> children = mapCollapsedScopes.remove(index);
			if (children != null) {
				return;
			}
			List<Scope> listScopes = convert(scope.getListChildren());
			
			ColumnComparator comparator = getComparator(sortedColumn, sortDirection);
			listScopes.sort(comparator);
			
			list.addAll(index+1, listScopes);
		}
	}
	
		
	/****
	 * Collapse a tree node. 
	 * This method has to be called AFTER calling the tree data
	 * @param index element index
	 * @param listCollapsedIndexes list of collapsed indexes from {@code TreeRowModel}
	 */
	public void collapse(int parentIndex, List<Integer> listCollapsedIndexes) {
		List<Scope> collapsedChildren = FastList.newList();
		synchronized (list) {
			Collections.sort(listCollapsedIndexes, Collections.reverseOrder());
			listCollapsedIndexes.forEach(index-> {
				collapsedChildren.add(list.get(index.intValue()));
			});
			mapCollapsedScopes.put(parentIndex, collapsedChildren);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Scope> convert(List<? extends TreeNode> list) {
		return (List<Scope>) list;
	}
	
	
	private boolean isRootScope(Scope scope) {
		return (scope == null) || (scope instanceof RootScope) || (scope == root);
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
		if (child == null || isCollapsed(child))
			return -1;
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
		if (list == null)
			return new ArrayList<>(0);
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
	
	private boolean isCollapsed(Scope scope) {
		int parentIndex = indexOf(scope.getParentScope());
		List<? extends TreeNode> listChildren = mapCollapsedScopes.get(parentIndex);
		if (listChildren == null)
			return false;
		
		return listChildren.contains(scope);
	}
	
	
	/***
	 * Retrieve the ancestor of specific depth from a scope  
	 * @param scope the current scope
	 * @param currentDepth the depth of the current scope
	 * @param targetDepth the depth of the ancestor
	 * @return the ancestor if exists, {@code null} otherwise
	 */
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
			int factor = dir == SortDirectionEnum.ASC ? 1 : -1;

			if (index == 0) {
				return factor * o1.getName().compareTo(o2.getName());
			}
			BaseMetric metric = treeData.getMetricManager().getVisibleMetrics().get(index-1);
			int metricIndex = metric.getIndex();
			MetricValue mv1 = o1.getMetricValue(metricIndex);
			MetricValue mv2 = o2.getMetricValue(metricIndex);

			if (mv1.getValue() > mv2.getValue())
				return factor * 1;
			if (mv1.getValue() < mv2.getValue())
				return factor * -1;
			return 0;
		}
	}

}
