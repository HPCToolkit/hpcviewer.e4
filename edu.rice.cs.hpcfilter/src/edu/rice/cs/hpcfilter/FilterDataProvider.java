package edu.rice.cs.hpcfilter;

import java.util.List;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;

public class FilterDataProvider implements IRowDataProvider<FilterDataItem> 
{
	private final IFilterChangeListener changeListener;
	private final List<FilterDataItem> list;
	
	public FilterDataProvider(List<FilterDataItem> list, IFilterChangeListener changeListener) {
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
		FilterDataItem item = list.get(rowIndex);
		
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
		FilterDataItem item = list.get(rowIndex);
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
			item.setLabel((String) newValue);;				
			BaseMetric metric = (BaseMetric) data;
			metric.setDisplayName((String) newValue);
			break;
		default:
			assert(false);
		}
	}
	
	protected List<FilterDataItem> getList() {
		return list;
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
	public FilterDataItem getRowObject(int rowIndex) {
		return list.get(rowIndex);
	}

	@Override
	public int indexOfRowObject(FilterDataItem rowObject) {
		return list.indexOf(rowObject);
	}		

}
