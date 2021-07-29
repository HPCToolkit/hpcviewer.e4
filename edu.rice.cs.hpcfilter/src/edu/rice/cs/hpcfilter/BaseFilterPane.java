package edu.rice.cs.hpcfilter;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.widgets.Composite;

public class BaseFilterPane extends AbstractFilterPane 
	implements IPropertyChangeListener, IFilterChangeListener
{
	private FilterDataProvider dataProvider;
	
	public BaseFilterPane(Composite parent, int style, FilterInputData inputData) {
		super(parent, style, inputData);
	}

	@Override
	public void changeEvent(Object data) {}

	@Override
	public void propertyChange(PropertyChangeEvent event) {}

	@Override
	protected void setLayerConfiguration(DataLayer datalayer) {
		datalayer.setColumnPercentageSizing(true);
		datalayer.setColumnWidthPercentageByPosition(0, 10);
		datalayer.setColumnWidthPercentageByPosition(1, 90);
	}

	@Override
	protected String[] getColumnHeaderLabels() {
		final String []LABELS = {"Visible", "Items"};
		return LABELS;
	}

	@Override
	protected FilterDataProvider getDataProvider() {
		if (dataProvider == null) {
			dataProvider = new FilterDataProvider(getFilterList(), this);
		}
		return dataProvider;
	}

	@Override
	protected int createAdditionalButton(Composite parent) {
		return 0;
	}

	@Override
	protected void selectionEvent(FilterDataItem item, int click) {}

	@Override
	protected void addConfiguration(NatTable table) {}

}
