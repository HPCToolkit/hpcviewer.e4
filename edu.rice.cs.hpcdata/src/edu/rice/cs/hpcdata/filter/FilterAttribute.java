package edu.rice.cs.hpcdata.filter;

import java.io.Serializable;

/****************************************************
 * 
 * Attribute of a filter.
 * A filter has two attributes:<br/>
 * <ul>
 *  <li> enable: flag for enable/disable
 *  <li> filterType: type of filtering (inclusive or exclusive)
 * </ul>
 ****************************************************/
public class FilterAttribute implements Serializable
{
	/**
	 * dummy id for serialization
	 */
	private static final long serialVersionUID = 1399047856674915771L;

	/****
	 * 
	 * Enumeration for type of filter: 
	 * inclusive: filter the nodes and its descendants
	 * exclusive: filter only the node
	 */
	static public enum Type {Self_And_Descendants, Self_Only, Descendants_Only};
	
	/***
	 * Flag true: the filter is enabled
	 * Flag false: disabled
	 */
	public boolean enable  = true;
	
	/*****
	 * @see Type
	 */
	public Type filterType = Type.Self_And_Descendants;
	
	/*****
	 * get the name of the filter
	 * 
	 * @return
	 */
	public String getFilterType() 
	{
		return filterType.name();
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if (!(other instanceof FilterAttribute))
			return false;
		
		FilterAttribute otherAttribute = (FilterAttribute) other;
		return (filterType == otherAttribute.filterType && enable == otherAttribute.enable);
	}
	
	/*****
	 * retrieve the names of filter attributes
	 * 
	 * @return
	 */
	static public String[] getFilterNames()
	{
		FilterAttribute.Type []types = FilterAttribute.Type.values();
		String []items = new String[types.length];
		for(int i=0; i<types.length; i++)
		{
			items[i] = types[i].name();
		}
		return items;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + enable + "," + filterType + ")";
	}
	
	public String getDescription() 
	{
		String text;
		switch(filterType) {
		case Self_Only:
			text = "Self only: only the matched nodes will be omitted from the tree"; break;
		case Descendants_Only:
			text = "Descendants only: all the descendants of the matched nodes will be omitted from the tree"; break;
		case Self_And_Descendants:
			text = "Self and descendants: the matched nodes and its descendants will be omitted from the tree"; break;
		default:
			text = "Unknown filter type";
		}
		return text;
	}
}
