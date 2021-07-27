package edu.rice.cs.hpcfilter;

import java.util.List;


public class FilterInputData 
{
	private final List<FilterDataItem> listItems;

	public FilterInputData(List<FilterDataItem> list) {
		this.listItems = list;
	}

	public List<FilterDataItem> getListItems() {
		return listItems;
	}
}
