package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventListener;
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
	private final static boolean UNIT_TEST = true;
	
	/** list of current data. The list is dynamic, always incremental **/
	private final MutableList<Scope> list;
	/** map of the collapsed nodes. The key is the parent index. 
	 *  The  value is the list of collapsed nodes of the parent */
	private final IntObjectHashMap<List<? extends TreeNode>> mapCollapsedScopes;

	private final EventList<BaseMetric> listMetrics;

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
		listMetrics = new BasicEventList<>();
		List<BaseMetric> listVisibleMetrics = metricManager.getVisibleMetrics();
		
		for(BaseMetric metric: listVisibleMetrics) {
			if (root.getMetricValue(metric) != MetricValue.NONE) {
				listMetrics.add(metric);
			}
		}
		
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
	
	
	
	/** 
	 * Reset the data
	 */
	@Override
	public void clear() {
		this.sortDirection = SortDirectionEnum.DESC;
		this.sortedColumn  = 1;
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
		mapCollapsedScopes.forEach((children) -> {
			children.sort(comparator);
		});
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
	
	public BaseMetric getMetric(int indexMetric) {
		return listMetrics.get(indexMetric);
	}
	
	public int getMetricCount() {
		return listMetrics.size();
	}
	
	
	public void addListener( ListEventListener<BaseMetric> listener ) {
		listMetrics.addListEventListener(listener);
	}
	
	
	public void removeListener( ListEventListener<BaseMetric> listener ) {
		listMetrics.removeListEventListener(listener);
	}
	

	
	/****
	 * Expand a tree node.
	 * This method has to be called BEFORE calling tree model's {@code expand}
	 * @param index
	 */
	@Override
	public List<? extends TreeNode> expand(int index) {
		synchronized (list) {
			Scope scope = list.get(index);
			if (!scope.hasChildren())
				return null;

			ColumnComparator comparator = getComparator(sortedColumn, sortDirection);
			
			List<? extends TreeNode> children = mapCollapsedScopes.remove(index);
			if (children != null) {
				children.sort(comparator);
				return children;
			}
			List<Scope> listScopes = convert(scope.getListChildren());
			
			listScopes.sort(comparator);			
			list.addAll(index+1, listScopes);
			
			return listScopes;
		}
	}
	
	
	/****
	 * Expand a scope and return the sorted children
	 * @param scope the parent scope
	 * @return {@code List} the list of sorted children
	 */
	/*
	public List<? extends TreeNode> expand(Scope scope) {
		int index = indexOf(scope);
		if (index < 0 || index >= list.size()) {
			// the scope doesn't exist. 
			return new ArrayList<>(0);
		}
		List<? extends TreeNode> children = mapCollapsedScopes.get(index);
		if (children == null) {
			// the scope hasn't been expanded
			// we need to force to expand it and grab the children
			return expand(index);
		}
		if (children.size() ==0)
			return children;
		
		// case where the scope has been expanded, we need to copy
		// the children to the list
		int numChildren = scope.getChildCount();		
		List<TreeNode> copyChildren = new FastList<>(numChildren);
		
		for(int i=index+1; i<index+1+numChildren; i++) {
			copyChildren.add(list.get(i));
		}
		
		return copyChildren;
	} */
		
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
		int index = list.indexOf(child);
		if (UNIT_TEST && index >= 0) {
			Scope s = list.get(index);
			if (child.getCCTIndex() != s.getCCTIndex()) {
				throw new RuntimeException("====WARNING " + child.getName() + " " + child.hashCode() + "/" + child.getCCTIndex() + " vs " + s.hashCode() + " / " + s.getCCTIndex());
			}
		}
		return index;
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
		int index = indexOf(object);
		return getChildren(index);
	}

	@Override
	public List<Scope> getChildren(Scope object, boolean fullDepth) {
		return getChildren(object);
	}

	@Override
	public List<Scope> getChildren(int index) {
		Scope scope = getDataAtIndex(index);
		
		// if the node doesn't exist, it should be a critical error
		// should we throw an exception?		
		int numChildren = scope.getChildCount();
		if (numChildren == 0)
			return new ArrayList<Scope>(0);
		
		if (mapCollapsedScopes.get(index) != null) {
			// the node is collapsed. The children is invisible
			return new ArrayList<Scope>(0);
		}
		// the node has children. Check if the children are expanded or not
		boolean isExpanded = list.indexOf(scope.getChildAt(0)) >= 0;
		if (!isExpanded) {
			return new ArrayList<Scope>(0);
		}
		
		// copy the children in the list to the new list
		// List<Scope> children = ((FastList<Scope>)list).subList(index+1, index+1+numChildren);
		// System.out.println(index + " has " + children.size() + ": " + children);
		return convert(scope.getListChildren());
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
	public static TreeNode getAncestor(TreeNode scope, int currentDepth, int targetDepth) {
		if (scope == null)
			return null;
		
		int depth=currentDepth-1;
		TreeNode current = scope.getParent();
		
		for (;depth>targetDepth && current != null; depth--) {
			current = current.getParent();
		}
		return current;		
	}

	private static class ColumnComparator implements Comparator<TreeNode> 
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
		public int compare(TreeNode o1, TreeNode o2) {
            int result = 0;
			int factor = dir == SortDirectionEnum.ASC ? 1 : -1;

			if (o1.getParent() != null && o2.getParent() != null) {
				int d1 = this.treeData.getDepthOfData((Scope) o1);
				int d2 = this.treeData.getDepthOfData((Scope) o2);
				
				if (d1 > d2) {
					TreeNode ancestor1 = ScopeTreeData.getAncestor(o1, d1, d2);
					result = compare((Scope) ancestor1, (Scope) o2, index, dir);
					if (result == 0) {
						return 1;
					}
				} else if (d1 < d2) {
					TreeNode ancestor2 = ScopeTreeData.getAncestor(o2, d2, d1);
					result = compare((Scope) o1, (Scope) ancestor2, index, dir);
					if (result == 0) {
						return -1;
					}
					
				} else {
					result = compare((Scope) o1, (Scope) o2, index, dir);
				}
			}
			return result;
		}
		
		private int compare(Scope o1, Scope o2, int index, SortDirectionEnum dir) {
			// o1 and o2 are exactly the same object. This should return 0
			// no need to go further
			if (o1 == o2)
				return 0;
			
			int factor = dir == SortDirectionEnum.ASC ? 1 : -1;

			if (index == 0) {
				return factor * o1.getName().compareTo(o2.getName());
			}
			BaseMetric metric = treeData.getMetric(index-1);
			int metricIndex = metric.getIndex();
			MetricValue mv1 = o1.getMetricValue(metricIndex);
			MetricValue mv2 = o2.getMetricValue(metricIndex);

			if (mv1.getValue() > mv2.getValue())
				return factor * 1;
			if (mv1.getValue() < mv2.getValue())
				return factor * -1;
			
			// ok. So far o1 looks the same as o2
			// we don't want returning 0 because it will cause the tree looks weird
			// let's try to compare with the name, and then with the hash code
			int result = o1.getName().compareTo(o2.getName());
			if (result == 0) {
				result = o1.hashCode() - o2.hashCode();
			}
			return result;
		}
	}

}
