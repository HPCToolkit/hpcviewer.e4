package edu.rice.cs.hpcviewer.ui.internal;

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.base.IViewItem;

public abstract class AbstractBaseViewItem extends CTabItem implements IViewItem {

	public AbstractBaseViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}
	
	
	/***
	 * Retrieve the list user visible metrics. 
	 * This doesn include the list marked as "invisible"
	 * 
	 * @return {@code List<BaseMetric>}
	 */
	public List<BaseMetric> getVisibleMetrics() {
		final ScopeTreeViewer treeViewer = getScopeTreeViewer();
		RootScope root = (RootScope) treeViewer.getInput();
		IMetricManager metricManager = (IMetricManager) root.getExperiment();
		return metricManager.getVisibleMetrics();
	}
	
	
	/****
	 * Retrieve the list of all metrics, including the invisible ones.
	 * Warning: do not call this method unless you know what you're doing.
	 * Call {@link getVisibleMetrics} instead.
	 * @return
	 */
	public List<BaseMetric> getMetrics() {
		final ScopeTreeViewer treeViewer = getScopeTreeViewer();
		RootScope root = (RootScope) treeViewer.getInput();
		IMetricManager metricManager = (IMetricManager) root.getExperiment();
		return metricManager.getMetricList();
	}

	public abstract ScopeTreeViewer getScopeTreeViewer();

}
