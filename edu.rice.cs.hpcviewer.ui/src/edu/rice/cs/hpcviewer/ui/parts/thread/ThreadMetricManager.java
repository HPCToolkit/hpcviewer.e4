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
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;



public class ThreadMetricManager implements IMetricManager 
{
	private final String ID;
	private final EventList<BaseMetric> rawMetrics;
	private final MutableIntObjectMap<BaseMetric> mapIntToMetric;
	
	public ThreadMetricManager(String ID, List<BaseMetric> metrics, List<IdTuple> listThreads) {
		this.ID = ID;
		
		List<BaseMetric> listMetrics = FastList.newList(metrics.size());
		mapIntToMetric = new IntObjectHashMap<>();
		Map<String, MetricRaw> mapNameToMetric = new HashMap<>(listMetrics.size());

		for(BaseMetric m: metrics) {
			MetricRaw mr = MetricRaw.create(m);
			mr.setThread(listThreads);
			
			listMetrics.add(mr);
			mapIntToMetric.put(m.getIndex(), mr);
			
			// reconstruct the metric partner
			var length = mr.getDisplayName().length();
			var baseName = mr.getDisplayName().substring(0, length-4);
			var partner  = mapNameToMetric.get(baseName);
			
			if (partner == null) {
				mapNameToMetric.put(baseName, mr);
			} else {
				partner.setPartner(mr.getIndex());
				mr.setPartner(partner.getIndex());
				
				partner.setMetricPartner(mr);
				mr.setMetricPartner(partner);
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
		getRawMetrics().add(0, objMetric);
	}

	@Override
	public List<Integer> getNonEmptyMetricIDs(Scope scope) {
		List<BaseMetric> metrics = getVisibleMetrics();
		List<Integer> listIDs = new ArrayList<>(metrics.size());
		
		for(BaseMetric m: metrics) {
			// fix issue #221 (do not show empty metrics)
			// in some versions, the root value of exclusive metric is always empty
			// in this case we need to get the value from its partner.
			// yuck.
			BaseMetric metric = m;
			if (metric.getMetricType() == MetricType.EXCLUSIVE &&
				scope instanceof RootScope) 
				metric = ((MetricRaw)m).getMetricPartner();
			
			if (metric != null && metric.getValue(scope) != MetricValue.NONE)
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

	@Override
	public MetricRaw getCorrespondentMetricRaw(BaseMetric metric) {
		if (metric instanceof MetricRaw)
			return (MetricRaw) metric;
		
		if (rawMetrics == null || rawMetrics.isEmpty())
			return null;
		
		var rawMetric = rawMetrics.stream().filter(m -> metric.getDisplayName().
														startsWith(m.getDisplayName().substring(0, m.getDisplayName().length()-3))
													)
											.findAny();
		if (rawMetric.isPresent())
			return (MetricRaw) rawMetric.get();
		
		return null;
	}

	@Override
	public String getID() {
		return ID;
	}
}
