package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class ThreadMetricManager implements IMetricManager 
{
	private final List<BaseMetric> rawMetrics;
	
	public ThreadMetricManager(List<BaseMetric> metrics, List<Integer> listThreads) {
		this.rawMetrics = FastList.newList(metrics.size());
		for(BaseMetric m: metrics) {
			MetricRaw mr = MetricRaw.create(m);
			mr.setThread(listThreads);
			rawMetrics.add(mr);
		}
	}

	@Override
	public BaseMetric getMetric(String ID) {
		for(BaseMetric m: rawMetrics) {
			if (m.getShortName().equals(ID))
				return m;
		}
		return null;
	}

	@Override
	public BaseMetric getMetric(int index) {
		return getRawMetrics().get(index);
	}

	@Override
	public BaseMetric getMetricFromOrder(int order) {
		// not supported in raw metric
		// should we return null or exception or ??
		return getMetric(order);
	}

	@Override
	public int getMetricCount() {
		return getRawMetrics().size();
	}

	@Override
	public List<BaseMetric> getMetricList() {
		return getRawMetrics();
	}

	@Override
	public List<BaseMetric> getVisibleMetrics() {
		return getRawMetrics();
	}

	@Override
	public List<BaseMetric> getRawMetrics() {
		return rawMetrics;
	}

	@Override
	public void addDerivedMetric(DerivedMetric objMetric) {
		getRawMetrics().add(0, objMetric);
	}

	@Override
	public List<Integer> getNonEmptyMetricIDs(Scope scope) {
		List<BaseMetric> metrics = getVisibleMetrics();
		List<Integer> listIDs = new ArrayList<>(metrics.size());
		for(BaseMetric m: metrics) {
			listIDs.add(m.getIndex());
		}
		return listIDs;
	}

}
