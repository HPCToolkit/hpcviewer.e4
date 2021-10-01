package edu.rice.cs.hpcfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

public class FilterDataItemSortModel<T> implements ISortModel, Comparator<FilterDataItem<T>> 
{
    protected int currentSortColumn = -1;
    protected SortDirectionEnum currentSortDirect = SortDirectionEnum.NONE;

    private List<FilterDataItem<T>> list;
    
    public FilterDataItemSortModel(List<FilterDataItem<T>> list) {
    	setList(list);
	}    
    
    
	public void setList(List<FilterDataItem<T>> list) {
		this.list = list;
	}


	@Override
	public List<Integer> getSortedColumnIndexes() {
		List<Integer> list = new ArrayList<Integer>(1);
		if (currentSortColumn >= 0) 
			list.add(currentSortColumn);
		
		return list;
	}

	@Override
	public boolean isColumnIndexSorted(int columnIndex) {
		return (currentSortColumn == columnIndex);
	}

	@Override
	public SortDirectionEnum getSortDirection(int columnIndex) {
		return currentSortDirect;
	}

	@Override
	public int getSortOrder(int columnIndex) {
		return 0;
	}

	@Override
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		return null;
	}

	@Override
	public Comparator<?> getColumnComparator(int columnIndex) {
		return null;
	}

	@Override
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		if (currentSortColumn != columnIndex) {
			clear();
		}
		if (sortDirection.equals(SortDirectionEnum.NONE)) {
			sortDirection = SortDirectionEnum.ASC;
		}
		currentSortColumn = columnIndex;
		currentSortDirect = sortDirection;

		Collections.sort(list, this);
	}

	@Override
	public void clear() {
		currentSortColumn = -1;
		currentSortDirect = SortDirectionEnum.NONE;
	}

	@Override
	public int compare(FilterDataItem<T> o1, FilterDataItem<T> o2) {
		int factor = 1;
		if (currentSortDirect == SortDirectionEnum.DESC) {
			factor = -1;
		}
		switch(currentSortColumn) {
		case 0:
			return factor * Boolean.compare(o1.checked, o2.checked);
		case 1:
			return factor * o1.compareTo(o2);
		}
		return 0;
	}

}
