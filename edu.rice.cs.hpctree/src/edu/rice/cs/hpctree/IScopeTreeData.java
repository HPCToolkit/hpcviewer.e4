package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
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
	
	
	/*****
	 * get the current list of nodes in the table.
	 * 
	 * @return
	 */
	public List<Scope> getList();
	
	
	/*****
	 * Set the new root to the table. This will generate new list of nodes.
	 * @param root
	 */
	public void setRoot(Scope root);
	
	/****
	 * Get the root of this tree data
	 * @return RootScope
	 */
	public Scope getRoot();
	
	
	/****
	 * Get the current sorted column
	 * 
	 * @return
	 */
	public int getSortedColumn();

	
	/****
	 * Get the current sort direction
	 * @return
	 */
	public SortDirectionEnum getSortDirection();
	
	
	
	/****
	 * Retrieve the metric manager of this tree
	 * @return
	 */
	public IMetricManager getMetricManager();
	
	/****
	 * Retrieve the metric of the current metric index (the index is based on table column index,
	 * not a metric ID or the experiment's index).
	 * 
	 * @param indexMetricColumn
	 * @return
	 */
	public BaseMetric getMetric(int indexMetricColumn);

	
	/****
	 * Retrieve the total number of metrics, including current hidden metrics
	 * 
	 * @return
	 */
	public int getMetricCount();

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


	/****
	 * Refresh the content of the tree data.
	 * This is used when there is a change in the metrics, 
	 * like new metrics have been added.
	 */
	void refresh();
}
