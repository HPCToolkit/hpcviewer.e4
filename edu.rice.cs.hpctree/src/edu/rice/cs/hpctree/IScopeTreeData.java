package edu.rice.cs.hpctree;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

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
		
	
	public void setRoot(Scope root);
	
	/****
	 * Get the root of this tree data
	 * @return RootScope
	 */
	public Scope getRoot();
	
	public int getSortedColumn();
	
	public SortDirectionEnum getSortDirection();
}
