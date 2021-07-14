package edu.rice.cs.hpcviewer.ui.internal;

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpcviewer.ui.base.IViewItem;

public abstract class AbstractBaseViewItem extends CTabItem implements IViewItem {

	public AbstractBaseViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}
	
	
	
	
	protected static void updateColumnHideOrShowStatus(ScopeTreeViewer treeViewer, Object data) {
		if (data instanceof List<?>) {
			List<MetricFilterDataItem> list = (List<MetricFilterDataItem>) data;
			list.stream().forEach(item -> {
				setColumnHideOrShow(treeViewer, item);
			});
			
		} else if (data instanceof MetricFilterDataItem) {
			setColumnHideOrShow(treeViewer, (MetricFilterDataItem) data);
		}
	}
	
	
	protected static void setColumnHideOrShow(ScopeTreeViewer treeViewer, MetricFilterDataItem filterItem) {

		BaseMetric metric = (BaseMetric) filterItem.data;
		TreeColumn []columns = treeViewer.getTree().getColumns();
		TreeColumn column = null;
		for (int i=0; i<columns.length; i++) {
			if (columns[i].getData() == metric) {
				column = columns[i];
				break;
			}
		}
		if (column != null)
			treeViewer.setColumnsStatus(column, filterItem.checked);
	}


	public abstract ScopeTreeViewer getScopeTreeViewer();
}
