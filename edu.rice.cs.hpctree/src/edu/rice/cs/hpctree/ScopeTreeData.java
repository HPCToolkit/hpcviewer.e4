package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
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
	/** list of current data. The list is dynamic **/
	private final MutableList<Scope>    listScopes;	
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
	public ScopeTreeData(RootScope root, IMetricManager metricManager, boolean noEmptyColumns) {
		this.listScopes = FastList.newList();
		this.listScopes.add(root);
		
		this.root = root;
		List<BaseMetric> listVisibleMetrics = metricManager.getVisibleMetrics();
		listMetrics = new BasicEventList<>(listVisibleMetrics.size());
		
		for(BaseMetric metric: listVisibleMetrics) {
			// if we don't want to include empty metrics, we need to check the value
			// otherwise, just add blindly
			if (noEmptyColumns) {
				MetricValue mv = metric.getValue(root);
				if (mv == MetricValue.NONE) 
					continue;
			}
			listMetrics.add(metric);
		}		
		clear();
	}
	
	public ScopeTreeData(RootScope root, IMetricManager metricManager) {
		this(root, metricManager, true);
	}
	
	@Override
	public List<Scope> getList() {
		return listScopes;
	}
	
	
	@Override
	public void setRoot(Scope root) {
		listScopes.clear();		
		listScopes.add(root);
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
		this.sortedColumn  = 0;
		if (this.listMetrics != null && this.listMetrics.size()>0)
			this.sortedColumn = 1;
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
		synchronized (listScopes) {
			listScopes.sort(comparator);
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
	
	
	@Override
	public BaseMetric getMetric(int indexMetric) {
		if (listMetrics == null || listMetrics.size() <= indexMetric)
			return null;
		return listMetrics.get(indexMetric);
	}
	
	
	@Override
	public void addMetric(int index, BaseMetric metric) {
		listMetrics.add(index, metric);
		
		// TODO: this is a bit hack
		// we shift the sorted column to the right if the sorted column
		// is a metric column. 
		if (sortedColumn>0)
			sortedColumn++;
	}
	
	
	@Override
	public int getMetricIndex(BaseMetric metric) {
		return listMetrics.indexOf(metric);
	}
	
	
	@Override
	public void updateMetric(int index, BaseMetric metric) {
		if (index >=0  && index < listMetrics.size())
			listMetrics.set(index, metric);
	}
	
	
	@Override
	public void addMetric(BaseMetric metric) {
		addMetric(listMetrics.size(), metric);
	}
	
	
	@Override
	public int getMetricCount() {
		return listMetrics.size();
	}
	
	
	@Override
	public List<BaseMetric> getMetrics() {
		return listMetrics;
	}
	
	public void addListener( ListEventListener<BaseMetric> listener ) {
		listMetrics.addListEventListener(listener);
	}
	
	
	public void removeListener( ListEventListener<BaseMetric> listener ) {
		listMetrics.removeListEventListener(listener);
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Scope> convert(List<? extends TreeNode> list) {
		return (List<Scope>) list;
	}
	
	
	private boolean isRootScope(Scope scope) {
		return (scope == null) || 
			   (scope instanceof RootScope) || 
			   (scope.getClass() == root.getClass() &&
			   (scope.getCCTIndex() == root.getCCTIndex()) );
	}
	
	
	@Override
	public List<Scope> getPath(Scope node) {
		List<Scope> path = FastList.newList();
		Scope current = node;
		while(current != null  &&  !isRootScope(current)) {
			path.add(current);
			current = current.getParentScope();
		}
		return path;
	}
	
	@Override
	public int getDepthOfData(Scope object) {
		if (object == null || isRootScope(object)) return 0;
		
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
		Scope scope = listScopes.get(index);
		return scope;
	}

	
	@Override
	public int indexOf(Scope child) {
		return listScopes.indexOf(child);
	}

	
	@Override
	public int indexOfBasedOnCCT(int cctIndex) {
		for(int i=0; i<listScopes.size(); i++) {
			if (listScopes.get(i).getCCTIndex() == cctIndex)
				return i;
		}
		return -1;
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
		return listScopes.size();
	}

	
	@Override
	public boolean isValidIndex(int index) {
		return (index >= 0) && (index < listScopes.size());
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

	
	/********************************************************************
	 * 
	 * comparator for the table. 
	 * If it's a metric column, we compare the value.
	 * If it's a tree column, we compare the name and its line number
	 *
	 ********************************************************************/
	private static class ColumnComparator implements Comparator<TreeNode> 
	{
		private final BaseMetric metric;
		private final SortDirectionEnum dir;
		private final IScopeTreeData treeData;
		
		public ColumnComparator(IScopeTreeData treeData, int columnIndex, SortDirectionEnum dir) {
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

	
	/***
	 * Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 * @param o1
	 * @param o2
	 * @param metric
	 * @param dir
	 * @return
	 */
	protected static int compareNodes(Scope o1, Scope o2, BaseMetric metric, SortDirectionEnum dir) {
		// o1 and o2 are exactly the same object. This should return 0
		// no need to go further
		if (o1 == o2)
			return 0;
		
		int factor = dir == SortDirectionEnum.ASC ? 1 : -1;

		if (metric == null) {
			return compareNodeName(o1, o2, factor);
		}

		MetricValue mv1 = metric.getValue(o1);
		MetricValue mv2 = metric.getValue(o2);

		if (mv1.getValue() > mv2.getValue())
			return factor * 1;
		if (mv1.getValue() < mv2.getValue())
			return factor * -1;
		
		// ok. So far o1 looks the same as o2
		// we don't want returning 0 because it will cause the tree looks weird
		// let's try to compare with the name, and then with the hash code
		return compareNodeName(o1, o2, factor);
	}

	
	/****
	 * compare the name of the nodes and guaranteed to be unique (not zero).
	 * If the nodes have the same node, we compare with the line number.
	 * If it's still the same, we compare with the hash code. This can't be the same.
	 * @param o1
	 * @param o2
	 * @param factor
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 */
	protected static int compareNodeName(Scope o1, Scope o2, int factor) {
		int result = o1.getName().compareTo(o2.getName());
		if (result == 0) {
			// same name: compare the line number
			if (o1 instanceof CallSiteScope && o2 instanceof CallSiteScope) {
				LineScope ls1 = ((CallSiteScope) o1).getLineScope();
				LineScope ls2 = ((CallSiteScope) o2).getLineScope();
				result = ls1.getLineNumber() - ls2.getLineNumber();
			} else {
				result = o1.getFirstLineNumber() - o2.getFirstLineNumber();
			}
			
			// ok. So far o1 looks the same as o2
			// we don't want returning 0 because it will cause the tree looks weird
			// let's try to compare with the hash code
			if (result == 0) {
				result = o1.hashCode() - o2.hashCode();
			}
		}
		return factor * result;
	}
}
