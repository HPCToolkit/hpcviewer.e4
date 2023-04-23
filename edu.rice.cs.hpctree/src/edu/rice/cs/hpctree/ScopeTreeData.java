package edu.rice.cs.hpctree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.tree.ScopeFlatComparator;
import edu.rice.cs.hpcdata.tree.ScopeTreeComparator;
import edu.rice.cs.hpcdata.tree.ScopeTreePath;


/******************************************************
 * 
 * Default ITreeData of hpcviewer tree 
 *
 ******************************************************/
public class ScopeTreeData extends ScopeTreePath implements IScopeTreeData
{
	/** list of current data. The list is dynamic **/
	private final MutableList<Scope>  listScopes;

	private List<Integer> indexesNonEmptyMetrics;
	private IMetricManager metricManager;
	
	// attributes to handle sorting
	private int sortedColumn;
	private SortDirectionEnum sortDirection;

	
	/***
	 * Constructor to create a tree data based on the root
	 * @param root the root scope
	 * @param metricManager the metric manager of the experiment or database
	 */
	public ScopeTreeData(RootScope root, IMetricManager metricManager) {
		super(root);
		this.listScopes = FastList.newList();
		this.listScopes.add(root);
		this.metricManager = metricManager;
		
		// fix issue #277: use the root to get the metrics
		// if we use the scope, some exclusive metrics will disappear
		this.indexesNonEmptyMetrics = metricManager.getNonEmptyMetricIDs(root);
		
		clear();
	}
	
	
	@Override
	public void refreshAndShift(int shift) {
		this.sortedColumn += 1;
		
		// fix issue #277: use the root to get the metrics
		// if we use the scope, some exclusive metrics will disappear
		this.indexesNonEmptyMetrics = metricManager.getNonEmptyMetricIDs(getRoot().getRootScope());
	}
	
	
	@Override
	public List<Scope> getList() {
		return listScopes;
	}
	
	
	@Override
	public void setRoot(Scope root) {		
		super.setRoot(root);

		listScopes.clear();		
		listScopes.add(root);
	}
	
	
	
	/** 
	 * Reset the data
	 */
	@Override
	public void clear() {
		this.sortDirection = SortDirectionEnum.DESC;
		this.sortedColumn  = 0;
		if (this.indexesNonEmptyMetrics != null && !this.indexesNonEmptyMetrics.isEmpty())
			this.sortedColumn = 1;
		
		// fix issue #277: use the root to get the metrics
		// if we use the scope, some exclusive metrics will disappear
		this.indexesNonEmptyMetrics = metricManager.getNonEmptyMetricIDs(getRoot().getRootScope());
	}

	
	/***
	 * Method to notify to sort the data based on certain column and direction
	 * @param columnIndex the column index. Must be greater or equal to 0
	 * @param sortDirection {@code SortDirectionEnum}
	 * @param accumulate
	 */
	@Override
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

		var comparator = getComparator(columnIndex, this.sortDirection);
		synchronized (listScopes) {
			try {
				listScopes.sort(comparator);
			} catch (IllegalArgumentException e) {
				// error in sorting the column
				LoggerFactory.getLogger(getClass()).error("column: " + columnIndex + ", direction: " + sortDirection, e);
			}
		}
	}
	
	
	/******
	 * Retrieve a new tree comparator
	 * 
	 * @param columnIndex
	 * 			The column index, either the tree column or metric column
	 * @param sortDir
	 * 			The sorting direction
	 * 
	 * @return Comparator object
	 * 
	 * @see SortDirectionEnum
	 */
	protected Comparator<Scope> getComparator(int columnIndex, SortDirectionEnum sortDir) {
		assert(sortDir != SortDirectionEnum.NONE);
		
		var metric = convertSortColumnToMetric(columnIndex);
		
		ScopeFlatComparator.SortDirectionEnum direction = convertSortDirection(sortDir);
		
		return new ScopeTreeComparator(this, metric, direction);
	}
	
	
	/***
	 * Convert from NatTable sort direction to hpcdata sort direction.
	 * Yuck.
	 * 
	 * @param sortDir
	 * @return
	 */
	private ScopeFlatComparator.SortDirectionEnum convertSortDirection(SortDirectionEnum sortDir) {
		return sortDir == SortDirectionEnum.ASC ? 
				ScopeFlatComparator.SortDirectionEnum.ASCENDING : 
				ScopeFlatComparator.SortDirectionEnum.DESCENDING;
	}
	
	
	private BaseMetric convertSortColumnToMetric(int columnIndex) {
		return  columnIndex == 0 ? null : getMetric(columnIndex - 1);
	}
	

	@Override
	public int getSortedColumn() {
		return sortedColumn;
	}


	@Override
	public SortDirectionEnum getSortDirection() {
		return sortDirection;
	}
	
	
	@Override
	public IMetricManager getMetricManager() {
		return metricManager;
	}
	
	@Override
	public BaseMetric getMetric(int indexMetricColumn) {
		int id = indexesNonEmptyMetrics.get(indexMetricColumn);
		return metricManager.getMetric(id);
	}
	
			
	@Override
	public int getMetricCount() {
		return indexesNonEmptyMetrics.size();
	}
		

	
	protected boolean isRootScope(Scope scope) {
		return (scope == null) || 
			   (scope instanceof RootScope) || 
			   (scope.getClass() == getRoot().getClass() &&
			   (scope.getCCTIndex() == getRoot().getCCTIndex()) );
	}
	
	
	@Override
	public List<Scope> getPath(Scope node) {
		FastList<Scope> path = FastList.newList();
		Scope current = node;
		while(current != null  &&  !isRootScope(current)) {
			path.add(current);
			current = current.getParentScope();
		}
		return path.reverseThis();
	}
	
	@Override
	public int getDepthOfData(Scope object) {
		if (object == null || isRootScope(object)) return 0;
		
		int depth = 0;
		Scope scope = object;
		while (scope.getParentScope() != null && !isRootScope(scope)) {
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
		return listScopes.get(index);
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
		return object.getSubscopeCount()>0;
	}

	
	@Override
	public boolean hasChildren(int index) {
		return hasChildren(getDataAtIndex(index));
	}

	
	@Override
	public List<Scope> getChildren(Scope scope) {
		
		// if the node doesn't exist, it should be a critical error
		// should we throw an exception?		
		if (scope.getSubscopeCount() == 0)
			return new ArrayList<>(0);
		
		// get the children from the original tree, and sort them
		// based on the sorted column (either metric or tree column)
		var metric = convertSortColumnToMetric(sortedColumn);
		
		// use flat comparator since we compare nodes within the same hierarchy
		// Using ScopeTreeComparator (hierarchy-based comparison) will be overkill
		// that's why we don't use getComparator() here.
		var comparator = new ScopeFlatComparator(metric, convertSortDirection(sortDirection));
		
		List<Scope> children = scope.getChildren();
		children.sort(comparator);
		
		return children;
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
}
