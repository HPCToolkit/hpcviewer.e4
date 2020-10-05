package edu.rice.cs.hpcviewer.ui.metric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

/********************************************************
 * 
 * Class to handle metric raw (metric-db from hpcprof) 
 * including adding it with derived metrics
 *
 ********************************************************/
public class MetricRawManager implements IMetricManager 
{
	final private ScopeTreeViewer treeViewer;
	
	public MetricRawManager(ScopeTreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}
	
	@Override
	public BaseMetric getMetric(String ID) {

		List<BaseMetric> list = getVisibleMetrics();
		if (list != null) {
			for (BaseMetric m : list) {
				if (m.getShortName().equals(ID)) 
					return m;
			}
		}
		return null;
	}

	@Override
	public BaseMetric getMetric(int index) {

		List<BaseMetric> list = getVisibleMetrics();
		if (list != null)
			return list.get(index);
		return null;
	}

	@Override
	public int getMetricCount() {

		List<BaseMetric> list = getVisibleMetrics();
		if (list != null)
			return list.size();
		return 0;
	}

	@Override
	public List<BaseMetric> getMetricList() {

		return getVisibleMetrics();
	}

	@Override
	public void addDerivedMetric(DerivedMetric objMetric) {
	}

	@Override
	public BaseMetric getMetricFromOrder(int order) {

		return getMetric(order);
	}

	@Override
	public List<BaseMetric> getVisibleMetrics() {
		TreeColumn []columns = treeViewer.getTree().getColumns();
		if (columns != null && columns.length > 1) {
			ArrayList<BaseMetric> listMetrics = new ArrayList<BaseMetric>(columns.length);
			for(TreeColumn col : columns) {
				Object obj = col.getData();
				if (obj instanceof BaseMetric) {
					if ( ((BaseMetric)obj).getVisibility() != VisibilityType.INVISIBLE )
						listMetrics.add((BaseMetric) obj);
				}
			}
			return listMetrics;
		}
		return null;
	}
}
