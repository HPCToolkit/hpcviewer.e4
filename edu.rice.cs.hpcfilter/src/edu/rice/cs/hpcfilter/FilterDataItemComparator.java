package edu.rice.cs.hpcfilter;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

public class FilterDataItemComparator<T> implements Comparator<FilterDataItem<T>> 
{
	private SortDirectionEnum sortDirection;
	private short columnIndex;
	
	public FilterDataItemComparator(short index, SortDirectionEnum sortDirection) {
		this.columnIndex = index;
		this.sortDirection = sortDirection;
	}
	
	@Override
	public int compare(FilterDataItem<T> o1, FilterDataItem<T> o2) {
		int factor = 1;
		if (sortDirection == SortDirectionEnum.DESC) {
			factor = -1;
		}
		switch(columnIndex) {
		case 0:
			return factor * Boolean.compare(o1.checked, o2.checked);
		case 1:
			return factor * o1.compareTo(o2);
		}
		return 0;
	}

	public void setSortDirection(short columnIndex, SortDirectionEnum sortDirection) {
		this.sortDirection = sortDirection;
		this.columnIndex   = columnIndex;
	}

}
