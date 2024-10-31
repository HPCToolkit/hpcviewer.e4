// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.tree.ITreePath;

public interface IScopeTreeData extends ITreeData<Scope>, ITreePath<Scope> 
{
	/***
	 * Method to notify to sort the data based on certain column and direction
	 * @param columnIndex the column index. Must be greater or equal to 0
	 * @param sortDirection {@code SortDirectionEnum}
	 * @param accumulate
	 */
	void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate);

	
	/** 
	 * Reset the data
	 */
	void clear();
	
	
	/*****
	 * get the current list of nodes in the table.
	 * 
	 * @return
	 */
	List<Scope> getList();
	
	
	/*****
	 * Set the new root to the table. This will generate new list of nodes.
	 * @param root
	 */
	void setRoot(Scope root);
	
	/****
	 * Get the root of this tree data
	 * @return RootScope
	 */
	Scope getRoot();
	
	
	/****
	 * Get the current sorted column
	 * 
	 * @return
	 */
	int getSortedColumn();

	
	/****
	 * Get the current sort direction
	 * @return
	 */
	SortDirectionEnum getSortDirection();
	
	
	
	/****
	 * Retrieve the metric manager of this tree
	 * @return
	 */
	IMetricManager getMetricManager();
	
	/****
	 * Retrieve the metric of the current metric index (the index is based on table column index,
	 * not a metric ID or the experiment's index).
	 * 
	 * @param indexMetricColumn
	 * @return
	 */
	BaseMetric getMetric(int indexMetricColumn);

	
	/****
	 * Retrieve the total number of metrics, including current hidden metrics
	 * 
	 * @return
	 */
	int getMetricCount();


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
	 * 
	 * @param shift the number of indexes a column to be shifted
	 * 			If the value of {@code shift} is 1, then all column indexes
	 * 			will be shifted to 1. This is important to update the index of
	 *     		sorted column. 
	 */
	void refreshAndShift(int shift);
	
	
	/***
	 * Check if the source file of this scope is available and readable or not.
	 * @param scope
	 * @return
	 */
	boolean isSourceFileAvailable(Scope scope);
}
