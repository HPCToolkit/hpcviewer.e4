package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IScopeTreeData extends ITreeData<Scope> 
{
	/****
	 * Expand a tree node.
	 * This method has to be called BEFORE calling tree model's {@code expand}
	 * @param index
	 */
	public void expand(int index);
	
	/****
	 * Collapse a tree node. 
	 * This method has to be called AFTER calling the tree data
	 * @param index element index
	 * @param listCollapsedIndexes list of collapsed indexes from {@code TreeRowModel}
	 */
	public void collapse(int parentIndex, List<Integer> listCollapsedIndexes);
	
	
	/***
	 * Method to notify to sort the data based on certain column and direction
	 * @param columnIndex the column index. Must be greater or equal to 0
	 * @param sortDirection {@code SortDirectionEnum}
	 * @param accumulate
	 */
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate);
	
	/***
	 * Get the current list of data.
	 * Recall that the list is created and added dynamically depending if the tree
	 * has been expanded or not.
	 * 
	 * @return {@code MutableList<Scope>} list of scope data
	 */
	public MutableList<Scope> getList();
	
	/** 
	 * Reset the data
	 */
	public void clear();
	
	/***
	 * Get the metric manager of this tree data
	 * @return
	 */
	public IMetricManager getMetricManager();
	
	
	public void setRoot(Scope root);
	
	/****
	 * Get the root of this tree data
	 * @return RootScope
	 */
	public Scope getRoot();
	
	public int getSortedColumn();
	
	public SortDirectionEnum getSortDirection();
}
