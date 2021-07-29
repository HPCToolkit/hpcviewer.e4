package edu.rice.cs.hpcfilter;

import java.util.List;


public class FilterInputData<T> 
{
	private final List<FilterDataItem<T>> listItems;

	public FilterInputData(List<FilterDataItem<T>> list) {
		this.listItems = list;
	}

	public List<FilterDataItem<T>> getListItems() {
		return listItems;
	}
}
