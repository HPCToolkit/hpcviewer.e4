package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IScopeTreeData extends ITreeData<Scope> 
{
	/***
	 * Method to notify to sort the data based on certain column and direction
	 * @param columnIndex the column index. Must be greater or equal to 0
	 * @param sortDirection {@code SortDirectionEnum}
	 * @param accumulate
	 */
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate);

	
	/** 
	 * Reset the data
	 */
	public void clear();
		
	public List<Scope> getList();
	
	public void setRoot(Scope root);
	
	/****
	 * Get the root of this tree data
	 * @return RootScope
	 */
	public Scope getRoot();
	
	public int getSortedColumn();
	public SortDirectionEnum getSortDirection();
	
	public BaseMetric getMetric(int indexMetric);
	public List<BaseMetric> getMetrics();
	public void addMetric(int index, BaseMetric metric);
	public void addMetric(BaseMetric metric);
	public int getMetricCount();


	/****
	 * Set a metric to the specified index.
	 * @param index
	 * @param metric
	 */
	void updateMetric(int index, BaseMetric metric);


	/*****
	 * Get the index of a metric. 
	 * The index is usually the position of the metric from the table.
	 * 
	 * @param metric
	 * @return the index from the list
	 */
	int getMetricIndex(BaseMetric metric);

	/****
	 * Retrieve the path (list of nodes) from the current node to the current "root".
	 * The current root can be the main root of the zoomed root.
	 * 
	 * @param node
	 * @return
	 */
	List<Scope> getPath(Scope node);


	/***
	 * Get the index of node based on CCT index
	 * @param cctIndex
	 * @return
	 */
	int indexOfBasedOnCCT(int cctIndex);
}