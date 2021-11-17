package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class ThreadMetricManager implements IMetricManager 
{
	private final EventList<BaseMetric> rawMetrics;
	private final MutableIntObjectMap<BaseMetric> mapIntToMetric;
	
	public ThreadMetricManager(List<BaseMetric> metrics, List<Integer> listThreads) {
		List<BaseMetric> listMetrics = FastList.newList(metrics.size());
		mapIntToMetric = new IntObjectHashMap<BaseMetric>();

		for(BaseMetric m: metrics) {
			MetricRaw mr = MetricRaw.create(m);
			mr.setThread(listThreads);
			listMetrics.add(mr);
			mapIntToMetric.put(m.getIndex(), mr);
		}
		rawMetrics = GlazedLists.eventList(listMetrics);
	}

	@Override
	public BaseMetric getMetric(String ID) {
		return getMetric(Integer.valueOf(ID));
	}

	@Override
	public BaseMetric getMetric(int index) {
		return mapIntToMetric.get(index);
	}

	@Override
	public BaseMetric getMetricFromOrder(int order) {
		// not supported in raw metric
		// should we return null or exception or ??
		throw new UnsupportedOperationException("getMetricFromOrder");
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
		mapIntToMetric.put(objMetric.getIndex(), objMetric);
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

	@Override
	public void addMetricListener(ListEventListener<BaseMetric> listener) {
		rawMetrics.addListEventListener(listener);
	}

	@Override
	public void removeMetricListener(ListEventListener<BaseMetric> listener) {
		rawMetrics.removeListEventListener(listener);
	}
}
