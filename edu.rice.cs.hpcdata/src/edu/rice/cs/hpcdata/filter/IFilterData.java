package edu.rice.cs.hpcdata.filter;

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
	 * A simple version of {@code getFilterAttribute} method. 
	 * <p>If the element is not to be filtered, it returns true
	 * Otherwise, it returns false.
	 * </p>
	 * @param element
	 * 			element to be filtered
	 * @return true if the element can pass. false otherwise
	 * 
	 * @see getFilterAttribute
	 */
	public boolean select(String element);
	
	/***
	 * Check if this filter is enabled or not
	 * 
	 * @return true if it's enabled. False otherwise.
	 */
	public boolean isFilterEnabled();
	
	/*****
	 * Check the status of filter for a given string element.
	 * <p>If the element should be filtered, it returns the matched 
	 * {@code FilterAttribute} that corresponds to the element<br/>
	 * Otherwise, it returns null.
	 * 
	 * @param element
	 * 			String to be filtered
	 * @return FilterAttribute
	 * 			If the element is to be filtered, this is not null.
	 * 
	 * @see FilterAttribute
	 */
	public FilterAttribute getFilterAttribute(String element);
}
