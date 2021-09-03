package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.AbstractMutableMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
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
	
	/** list of current data. The list is dynamic **/
	private final MutableList<Scope> list;
	
	/** map of the collapsed nodes. The key is the parent node. 
	 *  The  value is the list of collapsed nodes of the parent */
	private final AbstractMutableMap<Scope, List<Integer>> mapCollapsedScopes;

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
		this.mapCollapsedScopes   = new UnifiedMap<>();
		
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
		Scope scope = list.get(index);
		if (!scope.hasChildren())
			return new ArrayList<>(0);

		List<Scope> children;
		List<Integer> childrenIndexes = mapCollapsedScopes.remove(scope);
		if (childrenIndexes == null) {
			children = convert(scope.getListChildren()); 
			ColumnComparator comparator = getComparator(sortedColumn, sortDirection);
			children.sort(comparator);			
			list.addAll(index+1, children);
		}
		return null;
	}
	
	
		
	/****
	 * Collapse a tree node. 
	 * This method has to be called AFTER calling the tree data
	 * @param index element index
	 * @param listCollapsedIndexes list of collapsed indexes from {@code TreeRowModel}
	 */
	public void collapse(int parentIndex, List<Integer> listCollapsedIndexes) {
		Scope parent = getDataAtIndex(parentIndex);

		// move the children to another variable
		List<Integer> collapsedChildren = FastList.newList(listCollapsedIndexes);
		mapCollapsedScopes.put(parent, collapsedChildren);
	}
	
	
	public boolean isCollapsed(int index) {
		Scope scope = getDataAtIndex(index);
		if (!scope.hasChildren())
			return false;
		
		// it is collapsed if the node is in the list of collapsed
		if (mapCollapsedScopes.containsKey(scope))
			return true;
		
		// if the child doesn't exist in the list, it means the node is collapsed
		Scope child = scope.getSubscope(0);
		return (indexOf(child) < 0);
	}
	
	/***
	 * Check if a scope should call expand to traverse the child.
	 * If the scope is not expanded or the children are not visible, it
	 * returns true.
	 * It returns false if the scope is already expanded or it has no children.
	 * 
	 * @param scope
	 * @return {@code boolean}
	 */
	public boolean shouldExpand(Scope scope) {
		// check if it's collapsed
		if (mapCollapsedScopes.containsKey(scope))
			return true;
		
		// check if the children are not visible
		if (scope.hasChildren()) {
			int indexFirstChild = indexOf(scope.getSubscope(0));
			return indexFirstChild < 0;
		}
		return false;
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
		if (child == null || mapCollapsedScopes.containsKey(child.getParentScope()))
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
	public List<Scope> getChildren(Scope scope) {
		
		// if the node doesn't exist, it should be a critical error
		// should we throw an exception?		
		int numChildren = scope.getChildCount();
		if (numChildren == 0)
			return new ArrayList<Scope>(0);
		
		if (mapCollapsedScopes.get(scope) != null) {
			// the node is collapsed. The children is invisible
			return new ArrayList<Scope>(0);
		}
		
		// get the children from the original tree, and sort them
		// based on the sorted column (either metric or tree column)
		List<TreeNode> children = scope.getListChildren();
		final BaseMetric metric = sortedColumn == 0 ? null : getMetric(sortedColumn-1);
		Comparator<TreeNode> comparator = new Comparator<TreeNode>() {

			@Override
			public int compare(TreeNode o1, TreeNode o2) {
				Scope s1 = (Scope) o1;
				Scope s2 = (Scope) o2;				
				return compareNodes(s1, s2, metric, sortDirection);
			}
		};
		children.sort(comparator);
		
		return convert(children);
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
		private final BaseMetric metric;
		private final SortDirectionEnum dir;
		private final ScopeTreeData treeData;
		
		public ColumnComparator(ScopeTreeData treeData, int columnIndex, SortDirectionEnum dir) {
			this.treeData = treeData;
			this.dir = dir;
			if (columnIndex == 0)
				metric = null;
			else
				metric = treeData.getMetric(columnIndex-1);
		}
		
		@Override
		public int compare(TreeNode o1, TreeNode o2) {
            int result = 0;
			if (o1.getParent() != null && o2.getParent() != null) {
				int d1 = this.treeData.getDepthOfData((Scope) o1);
				int d2 = this.treeData.getDepthOfData((Scope) o2);
				
				if (d1 > d2) {
					TreeNode ancestor1 = ScopeTreeData.getAncestor(o1, d1, d2);
					result = ScopeTreeData.compareNodes((Scope) ancestor1, (Scope) o2, metric, dir);
					if (result == 0) {
						return 1;
					}
				} else if (d1 < d2) {
					TreeNode ancestor2 = ScopeTreeData.getAncestor(o2, d2, d1);
					result = ScopeTreeData.compareNodes((Scope) o1, (Scope) ancestor2, metric, dir);
					if (result == 0) {
						return -1;
					}
					
				} else {
					result = ScopeTreeData.compareNodes((Scope) o1, (Scope) o2, metric, dir);
				}
			}
			return result;
		}		
	}

	
	protected static int compareNodes(Scope o1, Scope o2, BaseMetric metric, SortDirectionEnum dir) {
		// o1 and o2 are exactly the same object. This should return 0
		// no need to go further
		if (o1 == o2)
			return 0;
		
		int factor = dir == SortDirectionEnum.ASC ? 1 : -1;

		if (metric == null) {
			return factor * o1.getName().compareTo(o2.getName());
		}

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
		return factor * result;
	}

}
