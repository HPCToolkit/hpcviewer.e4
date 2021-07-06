package edu.rice.cs.hpcviewer.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcviewer.ui.base.IViewItem;

public abstract class AbstractBaseViewItem extends CTabItem implements IViewItem {

	public AbstractBaseViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}
	
	

	
	public List<BaseMetric> getVisibleMetrics() {
		
		final List<BaseMetric> list = new ArrayList<BaseMetric>();
		final ScopeTreeViewer treeViewer = getScopeTreeViewer();
		
		for(TreeColumn column: treeViewer.getTree().getColumns()) {
			if (column.getData() == null)
				continue;
			
			if (column.getWidth()>0) {
				list.add((BaseMetric) column.getData());
			}
		}
		return list;
	}

	public abstract ScopeTreeViewer getScopeTreeViewer();

}
