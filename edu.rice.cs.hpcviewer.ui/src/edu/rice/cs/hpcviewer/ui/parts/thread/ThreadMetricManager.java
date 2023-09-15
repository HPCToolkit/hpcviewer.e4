package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;



public class ThreadMetricManager implements IMetricManager 
{
	private final String id;
	private final EventList<BaseMetric> rawMetrics;
	private final MutableIntObjectMap<BaseMetric> mapIntToMetric;
	
	public ThreadMetricManager(String id, List<BaseMetric> metrics, List<IdTuple> listThreads) {
		this.id = id;
		
		List<BaseMetric> listMetrics = FastList.newList(metrics.size());
		mapIntToMetric = new IntObjectHashMap<>();
		Map<String, MetricRaw> mapNameToMetric = new HashMap<>(listMetrics.size());

		for(BaseMetric m: metrics) {
			MetricRaw mr = MetricRaw.create(m);
			mr.setThread(listThreads);
			
			mapIntToMetric.put(m.getIndex(), mr);
			
			// reconstruct the metric partner
			var length = mr.getDisplayName().length();
			var baseName = mr.getDisplayName().substring(0, length-4);
			var partner  = mapNameToMetric.get(baseName);
			
			if (partner == null) {
				mapNameToMetric.put(baseName, mr);
				listMetrics.add(mr);
			} else {
				if (partner.getMetricPartner() == null) {
					partner.setPartner(mr.getIndex());
					mr.setPartner(partner.getIndex());
					
					partner.setMetricPartner(mr);
					mr.setMetricPartner(partner);
					
					listMetrics.add(mr);
				}
			}
		}
		rawMetrics = GlazedLists.eventList(listMetrics);
	}

	@Override
	public BaseMetric getMetric(String id) {
		return getMetric(Integer.valueOf(id));
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
		rawMetrics.add(0, objMetric);
	}

	@Override
	public List<Integer> getNonEmptyMetricIDs(Scope scope) {
		List<BaseMetric> metrics = getVisibleMetrics();
		List<Integer> listIDs = new ArrayList<>(metrics.size());

		for (var metric: metrics) {
			var value = scope.getMetricValue(metric);
			if (value != MetricValue.NONE)
				listIDs.add(metric.getIndex());
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

	@Override
	public String getID() {
		return id;
	}
}
