package edu.rice.cs.hpc.data.filter;

/**********************************************
 * 
 * Interface to filter a scope
 * This interface is designed to be used by a scope visitor
 * whether it needs to exclude a scope or not
 *
 **********************************************/
public interface IFilterData 
{
	/****
	 * Filter data given an element. If the element is not to be filtered, it returns true
	 * @param element : element to be filtered
	 * 
	 * @return true if the element can pass. false otherwise
	 */
	public boolean select(String element);
	
	public boolean isFilterEnabled();
	
	public FilterAttribute getFilterAttribute(String element);
}
