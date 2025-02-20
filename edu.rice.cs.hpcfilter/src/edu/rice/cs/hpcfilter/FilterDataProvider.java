// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter;

import java.util.List;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;

import edu.rice.cs.hpcfilter.internal.IConstants;

/********************************************************************************
 * 
 * Basic data provider for filter table. This class has a basic methods to:
 * <ul>
 * <li> {@link checkAll} to check all the items
 * <li> {@link uncheckAll} to uncheck all the items
 * </ul>
 *
 * @param <T> the data item class
 * 
 ********************************************************************************/
public class FilterDataProvider<T> implements IRowDataProvider<FilterDataItem<T>> 
{
	private final IFilterChangeListener changeListener;
	private List<FilterDataItem<T>> list;
	
	public FilterDataProvider(List<FilterDataItem<T>> list, IFilterChangeListener changeListener) {
		this.list = list;
		this.changeListener = changeListener;
	}
	

	public void checkAll() {
		getList().stream().filter(item-> item.data != null && item.enabled)
					 .forEach(item-> {
						 item.setChecked(true);
					 });

		getChangeListener().changeEvent(getList());
	}

	public void uncheckAll() {
		getList().stream().filter(item-> item.data != null && item.enabled)
		 		     .forEach(item-> { 
		 		    	 item.setChecked(false);
		 		       });

		getChangeListener().changeEvent(getList());
	}
	


	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		FilterDataItem<T> item = list.get(rowIndex);
		
		switch (columnIndex) {
		case IConstants.INDEX_VISIBILITY: 	
			return item.isChecked(); 
		case IConstants.INDEX_NAME: 		
			return item.getLabel();
		}
		assert (false);
		return null;
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		FilterDataItem<T> item = list.get(rowIndex);
		Object data = item.getData();

		if (data == null || !item.enabled)
			return;

		switch(columnIndex) {
		case IConstants.INDEX_VISIBILITY:
			boolean newCheck = (boolean) newValue;
			if (newCheck != item.checked) {
				item.setChecked((boolean) newValue);
				changeListener.changeEvent(item);
			}
			break;
			
		case IConstants.INDEX_NAME:
			item.setLabel((String) newValue);
			break;
		default:
			assert(false);
		}
	}
	
	protected List<FilterDataItem<T>> getList() {
		return list;
	}

	
	public void setList(List<FilterDataItem<T>> list) {
		this.list = list;
	}


	protected IFilterChangeListener getChangeListener() {
		return changeListener;
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public FilterDataItem<T> getRowObject(int rowIndex) {
		return list.get(rowIndex);
	}

	@Override
	public int indexOfRowObject(FilterDataItem<T> rowObject) {
		return list.indexOf(rowObject);
	}		

}
