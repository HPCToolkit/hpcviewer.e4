package edu.rice.cs.hpcviewer.ui.internal;

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcviewer.ui.base.IViewItem;

public abstract class AbstractView extends CTabItem implements IViewItem 
{
	public static enum ViewType {COLLECTIVE, INDIVIDUAL};

	public AbstractView(CTabFolder parent, int style) {
		super(parent, style);
	}

	public abstract List<FilterDataItem<BaseMetric>> getFilterDataItems();
	public abstract IMetricManager getMetricManager();
	public abstract ViewType       getViewType();
}
